package interfaces;

public interface CollisionListener {
	void ObstacleDetected(float distance);
	
	void UnexpectedRotationDetected();
	
	void BumpedIntoObstacle();
}
