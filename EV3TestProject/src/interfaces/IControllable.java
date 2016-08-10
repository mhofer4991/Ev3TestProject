package interfaces;

import lejos.robotics.geometry.Point;

/**
 * 
 * @author Markus
 * Interface used for internal logic like driving some distance or turning some degrees.
 */
public interface IControllable {
	void DriveDistanceForward(float distance);
	void DriveDistanceBackward(float distance);
	void TurnLeftByDegrees(float degrees);
	void TurnRightByDegrees(float degrees);

	void DriveForward();
	void DriveBackward();
	void TurnRight();
	void TurnLeft();
	
	boolean IsMoving();
	void RotateToDegrees(float degrees);
	Point GetPosition();
	// Returning the distance is just a temporary solution
	//float DriveToPosition(Point position);
	float ScanDistance();

	void Stop();
}
