package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import Serialize.ControlInput;
import Serialize.RoboStatus;
import interfaces.RemoteControlListener;
import interfaces.StatusListener;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class RemoteControlServer extends Thread implements StatusListener {
	public final static int PORT = 4001;
	
	private RemoteControlListener listener;
	
	private RemoteInputHandler inputHandler;
	
	private boolean running;
	
	private Socket clientSocket;
	
	private boolean isConnected;
	
	public RemoteControlServer(RemoteControlListener listener)
	{		
		this.running = true;
        this.isConnected = false;
		this.listener = listener;
		this.inputHandler = new RemoteInputHandler(listener);
	}
	
	public void Stop()
	{
		running = false;
	}
	
	public boolean IsConnected()
	{
		return this.isConnected;
	}
	
	@Override
	public void run()
	{
		ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);

            while (running)
            {
                this.clientSocket = null;
                
                try {
                	System.out.println("wait for re");
                	
        			this.isConnected = false;
        			this.inputHandler.Reset();
        			
                    clientSocket = serverSocket.accept();

                	System.out.println("connected");
                    
                    this.isConnected = true;
                    listener.ConnectedToRemote();

                    /*RoboStatus s = new RoboStatus();
                    s.X = 20;
                    s.Y = 40;
                    s.Rotation = 700;
                    
                    System.out.println("send pack!");
                    
                    this.SendRoboStatus(s);
                    
                    System.out.println("pack sent!");*/
                    
                    this.HandleClient(clientSocket);
                } catch (IOException e) {
        			this.isConnected = false;
                	listener.DisconnectedFromRemote();
                }
            }       
            
        	serverSocket.close();     
        } catch (IOException e) {
        	// TODO:
        	// Error handling
        }
	}
	
	/*
	 * Much thanks to 
	 * http://stackoverflow.com/questions/5865728/bitconverter-for-java
	 * http://stackoverflow.com/questions/5918133/different-results-when-converting-int-to-byte-array-net-vs-java
	 */
	public void SendRoboStatus(RoboStatus status)
	{		
			String data = Helper.GetObjectAsString(status);
			byte[] bd = data.getBytes();
			
			this.SendData((byte)5, bd);
	}
	
	private void SendData(byte code, byte[] data)
	{		
		try {
			ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN); // ByteOrder.nativeOrder());
		    buffer.putInt(data.length);
		    
			clientSocket.getOutputStream().write(new byte[] { code });
			clientSocket.getOutputStream().write(buffer.array());
			clientSocket.getOutputStream().write(data);
			
		} catch (IOException e) {
			this.isConnected = false;
        	listener.DisconnectedFromRemote();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void HandleClient(Socket client) throws IOException
	{
		InputStream in = clientSocket.getInputStream();

		while (running)
		{
			byte[] code = new byte[1];
			
			// Get code
			if (in.read(code, 0, code.length) == code.length)
			{
				byte[] length = new byte[4];
				
				// Get size of data
				if (in.read(length, 0, length.length) == length.length)
				{				
					ByteBuffer buffer = ByteBuffer.wrap(length);
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					int size = buffer.getInt();
					
					// Get data
					byte[] data = new byte[size];
					int bytesRead = 0;
					
					while (bytesRead < data.length)
					{
						int read = in.read(data, bytesRead, data.length - bytesRead);
						bytesRead += read;
						
						if (read == 0)
						{
							break;
						}
					}
					
					HandleData(code[0], data);
				}
			}
		}
		
		/*
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

		String code = in.readLine();
		boolean invalid = false;
		
		while (!code.equals("x") && !invalid)
		{
			for (RemoteControlListener listener : listeners)
			{
				if (code.equals("l"))
				{
					listener.TurnRobotLeft();
				}
				else if (code.equals("r"))
				{
					listener.TurnRobotRight();
				}
				else if (code.equals("f"))
				{
					listener.DriveRobotForward();
				}
				else if (code.equals("b"))
				{
					listener.DriveRobotBackward();
				}
				else if (code.equals("s"))
				{
					listener.StopRobot();
				}
				else
				{
					invalid = true;
				}
			}
			
			code = in.readLine();
		}

    	in.close();
        client.close();*/
	}
	
	private void HandleData(byte code, byte[] data)
	{	
		// Handle data
		String text = new String(data, StandardCharsets.UTF_8);
		
		if (code == 4)
		{
			RoboStatus rs = Helper.GetObjectFromString(text, RoboStatus.class);
			
			//LCD.drawString(Float.toString(rs.Rotation), 0, 0);
		}
		else if (code == 1)
		{
			ControlInput in = Helper.GetObjectFromString(text, ControlInput.class);
			
			//System.out.println(in.Code);
			this.HandleControlInput(in);
		}
	}
	
	private void HandleControlInput(ControlInput input)
	{
		System.out.println("msg " + Integer.toString(input.Code) + " " + Boolean.toString(input.Released) + " rcvd");
		
		this.inputHandler.HandleInput(input);
	}
	
	//
	//
	//

	@Override
	public void StatusUpdated(RoboStatus status) {
		if (this.isConnected)
		{
			this.SendRoboStatus(status);
		}
	}
}
