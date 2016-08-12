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

import org.freedesktop.DBus.Local.Disconnected;

import Serialize.ControlInput;
import Serialize.Map;
import Serialize.RoboStatus;
import Serialize.Route;
import Serialize.TravelRequest;
import Serialize.TravelResponse;
import interfaces.RemoteControlListener;
import interfaces.RobotStatusListener;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class RemoteControlServer extends Thread {
	public final static int PORT = 4001;
	
	public final static byte MSGCODE_REMOTE_CONTROL_INPUT = 1;
	
	public final static byte MSGCODE_ROBOT_MAP_REQUEST = 2;
	
	public final static byte MSGCODE_ROBOT_MAP_RESPONSE = 3;
	
	public final static byte MSGCODE_ROBOT_CALIBRATE_FINISHED = 4;
	
	public final static byte MSGCODE_ROBOT_STATUS_UPDATE = 5;
	
	public final static byte MSGCODE_ROBOT_CALIBRATE_REQUEST = 6;
	
	public final static byte MSGCODE_ROBOT_TRAVEL_ROUTE_REQUEST = 7;
	
	public final static byte MSGCODE_ROBOT_TRAVEL_ROUTE_RESPONSE = 8;
	
	public final static byte MSGCODE_ROBOT_SCAN_MAP_UPDATE = 9;

    public final static byte MSGCODE_ROBOT_CANCEL_ROUTE_REQUESET = 10;

    public final static byte MSGCODE_ROBOT_START_MANUAL_SCAN_MODE = 11;

    public final static byte MSGCODE_ROBOT_EXIT_MANUAL_SCAN_MODE = 12;

    public final static byte MSGCODE_ROBOT_START_AUTO_SCAN_MODE = 13;

    public final static byte MSGCODE_ROBOT_EXIT_AUTO_SCAN_MODE = 14;
    
    public final static byte MSGCODE_ROBOT_LOG = 15;
    
    public final static byte MSGCODE_AUTO_SCAN_FINISHED = 16;
	
	private RemoteControlListener listener;
	
	private RemoteInputHandler inputHandler;
	
	private boolean running;
	
	private Socket clientSocket;
	
	private boolean isConnected;
	
	private boolean listenToRemoteControlInput;
	
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
                }
                
    			this.isConnected = false;
            	listener.DisconnectedFromRemote();
            }       
            
        	serverSocket.close();     
        } catch (IOException e) {
        	// TODO:
        	// Error handling
        }
	}
	
	//
	// Send methods
	//
	
	public void SendCalibrationFinished()
	{
		this.SendMessage(MSGCODE_ROBOT_CALIBRATE_FINISHED, MSGCODE_ROBOT_CALIBRATE_FINISHED);
	}
	
	public void SendMapResponse(Map map)
	{
		this.SendMessage(MSGCODE_ROBOT_MAP_RESPONSE, map);
	}
	
	public void SendRoboStatus(RoboStatus status)
	{
		this.SendMessage(MSGCODE_ROBOT_STATUS_UPDATE, status);
	}
	
	public void SendTravelResponse(TravelResponse response)
	{
		this.SendMessage(MSGCODE_ROBOT_TRAVEL_ROUTE_RESPONSE, response);
	}
	
	public void SendMapUpdate(Map map)
	{
		this.SendMessage(MSGCODE_ROBOT_SCAN_MAP_UPDATE, map);
	}
	
	public void SendLog(String text)
	{
		this.SendMessage(MSGCODE_ROBOT_LOG, text);
	}
	
	public void SendAutoScanFinished()
	{
		this.SendMessage(MSGCODE_AUTO_SCAN_FINISHED, MSGCODE_AUTO_SCAN_FINISHED);
	}
	
	public void SendMessage(byte code, Object msg)
	{
		String data = Helper.GetObjectAsString(msg);
		byte[] bd = data.getBytes();
		
		this.SendData(code, bd);
	}
	
	private void SendData(byte code, byte[] data)
	{
		if (this.clientSocket != null)
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
	}
	
	//
	// Handle methods
	//
	
	private void HandleClient(Socket client) throws IOException
	{
		InputStream in = clientSocket.getInputStream();
		boolean okay = true;

		while (running && okay)
		{
			byte[] code = new byte[1];
			
			int result = in.read(code, 0, code.length);
			
			// Get code
			if (result < 0)
			{
				// It seems that the connection has been closed.
				okay = false;
			}
			else if (result == code.length)
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
					
					if (!HandleData(code[0], data))
					{
						// Handling data was not successful.
						okay = false;
					}
				}
			}
		}
	}
	
	private boolean HandleData(byte code, byte[] data)
	{	
		// Handle data
		String text = new String(data, StandardCharsets.UTF_8);
		
		// TODO:
		// Change it
		if (code == 4)
		{
			RoboStatus rs = Helper.GetObjectFromString(text, RoboStatus.class);
			
			//LCD.drawString(Float.toString(rs.Rotation), 0, 0);
		}
		else if (code == MSGCODE_REMOTE_CONTROL_INPUT)
		{
			if (this.listenToRemoteControlInput)
			{
				ControlInput in = Helper.GetObjectFromString(text, ControlInput.class);
				
				//System.out.println(in.Code);
				this.HandleControlInput(in);
			}
		}
		else if (code == MSGCODE_ROBOT_MAP_REQUEST)
		{
			this.listener.MapRequested();
		}
		else if (code == MSGCODE_ROBOT_CALIBRATE_REQUEST)
		{
			this.listener.CalibratingRequested();
		}
		else if (code == MSGCODE_ROBOT_TRAVEL_ROUTE_REQUEST)
		{
			TravelRequest request = Helper.GetObjectFromString(text, TravelRequest.class);
			
			this.listener.TravelRouteRequested(request);
		}
		else if (code == MSGCODE_ROBOT_CANCEL_ROUTE_REQUESET)
		{
			this.listener.CancelRouteRequested();
		}
		else if (code == MSGCODE_ROBOT_START_MANUAL_SCAN_MODE)
		{
			this.listenToRemoteControlInput = true;
			
			this.listener.ManualScanModeStarted();
		}
		else if (code == MSGCODE_ROBOT_EXIT_MANUAL_SCAN_MODE)
		{
			this.listenToRemoteControlInput = false;
			
			this.listener.ManualScanModeExited();
		}
		else if (code == MSGCODE_ROBOT_START_AUTO_SCAN_MODE)
		{
			this.listener.AutomaticScanModeStarted();
		}
		else if (code == MSGCODE_ROBOT_EXIT_AUTO_SCAN_MODE)
		{
			this.listener.AutomaticScanModeExited();
		}
		else
		{
			return false;
		}
		
		return true;
	}
	
	private void HandleControlInput(ControlInput input)
	{
		//System.out.println("msg " + Integer.toString(input.Code) + " " + Boolean.toString(input.Released) + " rcvd");
		
		this.inputHandler.HandleInput(input);
	}
}
