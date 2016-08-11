package interfaces;

import Serialize.RoboStatus;
import lejos.robotics.geometry.Point;

public interface RobotStatusListener {
	void RobotStatusUpdated(RoboStatus status);
	
	void RobotStoppedDueToObstacle(RoboStatus status, Point obstaclePosition);
}
