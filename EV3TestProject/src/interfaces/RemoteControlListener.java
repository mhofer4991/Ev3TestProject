package interfaces;

import Serialize.Map;
import Serialize.Route;
import Serialize.TravelRequest;

public interface RemoteControlListener {
	// Connection handling
	void ConnectedToRemote();
	void DisconnectedFromRemote();
	
	// auto <--> manual
	void ManualScanModeStarted();
	void ManualScanModeExited();
	void AutomaticScanModeStarted();
	void AutomaticScanModeExited();
	
	// 
	void CalibratingRequested();
	void TravelRouteRequested(TravelRequest request);
	void CancelRouteRequested();
	void MapRequested();
	
	// Remote controlling the robot
	void DriveRobotForward();
	void DriveRobotBackward();
	void TurnRobotLeft();
	void TurnRobotLeft(float degrees);
	void TurnRobotRight();
	void TurnRobotRight(float degrees);
	void StopRobot();
	void ScanArea();
}
