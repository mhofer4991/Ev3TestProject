package interfaces;

import Serialize.Map;
import Serialize.Route;
import Serialize.TravelRequest;

public interface RemoteControlListener {
	// Connection handling
	void ConnectedToRemote();
	void DisconnectedFromRemote();
	
	// 
	void CalibratingRequested();
	void TravelRouteRequested(TravelRequest request);
	
	// Remote controlling the robot
	void DriveRobotForward();
	void DriveRobotBackward();
	void TurnRobotLeft();
	void TurnRobotLeft(float degrees);
	void TurnRobotRight();
	void TurnRobotRight(float degrees);
	void StopRobot();
}
