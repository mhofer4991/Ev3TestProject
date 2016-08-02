package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import interfaces.RemoteControlListener;

public class ProtocolServer extends Thread {
	public final static int PORT = 4001;
	
	private List<RemoteControlListener> listeners;
	
	private boolean running;
	
	public ProtocolServer()
	{
		this.listeners = new ArrayList<RemoteControlListener>();
		
		this.running = true;
	}
	
	public void AddListener(RemoteControlListener listener)
	{
		this.listeners.add(listener);
	}
	
	@Override
	public void run()
	{
		ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);

            while (running)
            {
                Socket clientSocket = null;
                
                try {
                    clientSocket = serverSocket.accept();
                    
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
					listener.TurnLeft();
				}
				else if (code.equals("r"))
				{
					listener.TurnRight();
				}
				else if (code.equals("f"))
				{
					listener.DriveForward();
				}
				else if (code.equals("b"))
				{
					listener.DriveBackward();
				}
				else if (code.equals("s"))
				{
					listener.Stop();
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
