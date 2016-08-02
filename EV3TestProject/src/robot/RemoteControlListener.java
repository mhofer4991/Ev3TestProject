package robot;

public interface RemoteControlListener {
	void DriveForward();
	void DriveBackward();
	void TurnRight();
	void TurnLeft();
	void Stop();
}
