package interfaces;

import lejos.robotics.geometry.Point;

/**
 * 
 * @author Markus
 * Interface used for internal logic like driving some distance or turning some degrees.
 */
public interface Controllable {
	void DriveDistanceForward(float distance);
	void DriveDistanceBackward(float distance);
	void TurnLeftByDegrees(float degrees);
	void TurnRightByDegrees(float degrees);

	void DriveForward();
	void DriveBackward();
	void TurnRight();
	void TurnLeft();
	
	void DriveToPosition(Point position);

	void Stop();
}
