package interfaces;

/**
 * 
 * @author Markus
 * Interface used for remote controlling the robot by some input device like
 * xbox controller.
 */
public interface RemoteControlListener {
	void DriveForward();
	void DriveBackward();
	void TurnRight();
	void TurnLeft();
	void Stop();
}
