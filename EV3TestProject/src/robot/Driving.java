package robot;

import lejos.robotics.RegulatedMotor;

public class Driving {
	private RegulatedMotor left;
	
	private RegulatedMotor right;
	
	public Driving(RegulatedMotor left, RegulatedMotor right)
	{
		this.left = left;
		this.right = right;
		
		this.left.synchronizeWith(new RegulatedMotor[] { right});
	}
	
	public void TurnRight(int motorDegrees)
	{
		this.left.startSynchronization();
		
		this.left.rotate(motorDegrees);
		this.right.rotate(motorDegrees);
		
		this.left.endSynchronization();
	}
}
