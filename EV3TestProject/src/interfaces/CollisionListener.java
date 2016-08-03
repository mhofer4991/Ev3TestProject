package interfaces;

public interface CollisionListener {
	void ObstacleDetected(float remainingDistance);
	
	void UnexpectedRotationDetected();
	
	void BumpedIntoObstacle();
}
