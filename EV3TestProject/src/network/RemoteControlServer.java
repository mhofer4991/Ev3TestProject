package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Serialize.RoboStatus;
import interfaces.RemoteControlListener;

public class RemoteControlServer extends Thread {
	public final static int PORT = 4001;
	
	private List<RemoteControlListener> listeners;
	
	private boolean running;
	
	private Socket clientSocket;
	
	public RemoteControlServer()
	{
		this.listeners = new ArrayList<RemoteControlListener>();
		
		this.running = true;
	}
	
	public void AddListener(RemoteControlListener listener)
	{
		this.listeners.add(listener);
	}
	
	public void Stop()
	{
		running = false;
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
                    clientSocket = serverSocket.accept();

                    RoboStatus s = new RoboStatus();
                    s.X = 20;
                    s.Y = 40;
                    s.Rotation = 700;
                    
                    this.SendRoboStatus(s);
                    
                    this.HandleClient(clientSocket);
                } catch (IOException e) {
                }
            }       
            
        	serverSocket.close();     
        } catch (IOException e) {
        	// TODO:
        	// Error handling
        }
	}
	
	/*
	 * Much thanks to http://stackoverflow.com/questions/1936857/convert-integer-into-byte-array-java
	 */
	public void SendRoboStatus(RoboStatus status)
	{		
		try {
			String data = Helper.GetObjectAsString(status);
			byte[] bd = data.getBytes();
			
			clientSocket.getOutputStream().write(new byte[] { 5 });
			clientSocket.getOutputStream().write(BigInteger.valueOf(bd.length).toByteArray());
			clientSocket.getOutputStream().write(bd);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void HandleClient(Socket client) throws IOException
	{
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
        client.close();
	}
}
