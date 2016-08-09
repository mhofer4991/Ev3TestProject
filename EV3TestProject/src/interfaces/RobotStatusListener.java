package interfaces;

import Serialize.RoboStatus;

public interface RobotStatusListener {
	void RobotStatusUpdated(RoboStatus status);
	
	void RobotStoppedDueToObstacle(RoboStatus status);
}
