package interfaces;

public interface RemoteControlListener {
	// 
	void ConnectedToRemote();
	void DisconnectedFromRemote();
	
	// Remote controlling the robot
	void DriveRobotForward();
	void DriveRobotBackward();
	void TurnRobotLeft();
	void TurnRobotRight();
	void StopRobot();
}
