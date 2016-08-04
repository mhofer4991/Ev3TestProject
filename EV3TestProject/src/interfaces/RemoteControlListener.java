package interfaces;

public interface RemoteControlListener {
	// Connection handling
	void ConnectedToRemote();
	void DisconnectedFromRemote();
	
	// 
	void CalibratingRequested();
	
	// Remote controlling the robot
	void DriveRobotForward();
	void DriveRobotBackward();
	void TurnRobotLeft();
	void TurnRobotRight();
	void StopRobot();
}
