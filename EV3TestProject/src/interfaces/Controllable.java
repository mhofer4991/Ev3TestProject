package interfaces;

/**
 * 
 * @author Markus
 * Interface used for internal logic like driving some distance or turning some degrees.
 */
public interface Controllable {
	public void DriveDistanceForward(float distance);
	public void DriveDistanceBackward(float distance);
	public void TurnLuftByDegrees(float degrees);
	public void TurnRightByDegrees(float degrees);
	public void CancelDriving();
}
