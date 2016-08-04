package network;

import Serialize.ControlInput;
import interfaces.RemoteControlListener;

public class RemoteInputHandler {
	private RemoteControlListener listener;
	
	private ControlInput previousInput;
	
	public RemoteInputHandler(RemoteControlListener listener)
	{
		this.listener = listener;
		this.previousInput = new ControlInput(-1, false);
	}
	
	public void Reset()
	{
		this.previousInput = new ControlInput(-1, false);
	}
	
	public void HandleInput(ControlInput input)
	{
		switch (input.Code)
		{
		// Forward
		case 1:
			if (input.Released)
			{
				if (previousInput.Code == input.Code)
				{
					listener.StopRobot();
					
					previousInput.Code = -1;
				}
			}
			else
			{				
				if (previousInput.Code == -1 || previousInput.Code == input.Code)
				{
					listener.DriveRobotForward();
					
					previousInput = input;
				}
			}
			break;
		// Forward
		case 2:
			if (input.Released)
			{
				if (previousInput.Code == input.Code)
				{
					listener.StopRobot();
					
					previousInput.Code = -1;
				}
			}
			else
			{		
				if (previousInput.Code == -1 || previousInput.Code == input.Code)
				{
					listener.DriveRobotBackward();
					
					previousInput = input;
				}
			}
			break;
		// Right
		case 3:
			if (input.Released)
			{
				if (previousInput.Code == input.Code)
				{
					listener.StopRobot();
					
					previousInput.Code = -1;
				}
			}
			else
			{
				if (previousInput.Code == -1 || previousInput.Code == input.Code)
				{
					listener.TurnRobotRight();
					
					previousInput = input;
				}
			}
			break;
		// Left
		case 4:
			if (input.Released)
			{
				if (previousInput.Code == input.Code)
				{
					listener.StopRobot();
					
					previousInput.Code = -1;
				}
			}
			else
			{
				if (previousInput.Code == -1 || previousInput.Code == input.Code)
				{
					listener.TurnRobotLeft();
					
					previousInput = input;
				}
			}
			break;
		case 5:
			listener.StopRobot();	
			break;
		}
	}
}
