package interfaces;

public interface RemoteControlListener {
	// 
	
	// Remote controlling the robot
	void DriveRobotForward();
	void DriveRobotBackward();
	void TurnRobotLeft();
	void TurnRobotRight();
	void StopRobot();
}
