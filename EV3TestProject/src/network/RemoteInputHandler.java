package network;

import Serialize.ControlInput;
import interfaces.RemoteControlListener;

public class RemoteInputHandler {
	public final static int CODE_FORWARD = 1;
	
	public final static int CODE_BACKWARD = 2;
	
	public final static int CODE_RIGHT = 3;
	
	public final static int CODE_LEFT = 4;
	
	public final static int CODE_STOP = 5;
	
	public final static int CODE_SCAN = 6;
	
	private RemoteControlListener listener;
	
	private ControlInput previousInput;
	
	public RemoteInputHandler(RemoteControlListener listener)
	{
		this.listener = listener;
		this.previousInput = new ControlInput(-1, false, false, 0);
	}
	
	public void Reset()
	{
		this.previousInput = new ControlInput(-1, false, false, 0);
	}
	
	public void HandleInput(ControlInput input)
	{
		if (input.UseValue)
		{
			switch (input.Code)
			{
			case CODE_LEFT:
				listener.TurnRobotLeft(input.Value);
				break;
			case CODE_RIGHT:
				listener.TurnRobotRight(input.Value);
				break;
			case CODE_SCAN:
				listener.ScanArea();
				break;
			}
		}
		else if (previousInput.Code == input.Code || input.Code == CODE_STOP || previousInput.Code == -1)
		{
			switch (input.Code)
			{
			// Forward
			case CODE_FORWARD:
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
			case CODE_BACKWARD:
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
			case CODE_RIGHT:
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
			case CODE_LEFT:
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
			case CODE_STOP:
				listener.StopRobot();	
				previousInput.Code = -1;
				break;
			}	
		}
	}
}
