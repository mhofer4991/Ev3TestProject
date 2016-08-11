package robot;

import javax.management.ImmutableDescriptor;

import calibrating.CalibratingData;
import calibrating.CalibratingUtil;
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
	
	public RegulatedMotor GetLeft()
	{
		return left;
	}
	
	public RegulatedMotor GetRight()
	{
		return right;
	}
	
	public void DriveForward()
	{
		this.left.startSynchronization();

		this.left.forward();
		this.right.forward();
		
		this.left.endSynchronization();
	}
	
	public void DriveBackward()
	{
		this.left.startSynchronization();

		this.left.backward();
		this.right.backward();
		
		this.left.endSynchronization();
	}
	
	public void DriveForward(int motorDegrees, boolean immediateReturn)
	{
		this.left.startSynchronization();
		
		this.left.rotate(motorDegrees, true);
		this.right.rotate(motorDegrees);
		
		this.left.endSynchronization();
		
		if (!immediateReturn)
		{
			this.left.waitComplete();
			this.right.waitComplete();
		}
	}
	
	public void DriveBackward(int motorDegrees, boolean immediateReturn)
	{
		this.DriveForward(motorDegrees * -1, immediateReturn);
	}
	
	public void DriveDistanceForward(float distance, boolean immediateReturn)
	{
		this.DriveForward(CalibratingUtil.ConvertDeviceDistanceToMotorDegrees(distance), immediateReturn);
	}
	
	public void DriveDistanceBackward(float distance, boolean immediateReturn)
	{
		this.DriveBackward(CalibratingUtil.ConvertDeviceDistanceToMotorDegrees(distance), immediateReturn);
	}
	
	public void TurnRight()
	{
		this.left.startSynchronization();

		this.left.forward();
		this.right.backward();
		
		this.left.endSynchronization();
	}
	
	public void TurnLeft()
	{
		this.left.startSynchronization();

		this.left.backward();
		this.right.forward();
		
		this.left.endSynchronization();
	}
	
	public void TurnRight(int motorDegrees, boolean immediateReturn)
	{
		this.left.startSynchronization();
		
		this.left.rotate(motorDegrees, true);
		this.right.rotate(motorDegrees * -1);
		
		this.left.endSynchronization();
		
		if (!immediateReturn)
		{
			this.left.waitComplete();
			this.right.waitComplete();
		}
	}
	
	public void TurnLeft(int motorDegrees, boolean immediateReturn)
	{
		this.TurnRight(motorDegrees * -1, immediateReturn);
	}
	
	public void TurnRightByDegrees(float deviceDegrees, boolean immediateReturn)
	{
		this.TurnRight(CalibratingUtil.ConvertDeviceDegreesToMotorDegrees(deviceDegrees), immediateReturn);
	}
	
	public void TurnLeftByDegrees(float deviceDegrees, boolean immediateReturn)
	{
		this.TurnLeft(CalibratingUtil.ConvertDeviceDegreesToMotorDegrees(deviceDegrees), immediateReturn);
	}
	
	public void Stop()
	{
		this.left.startSynchronization();

		//this.left.flt(true);
		//this.right.flt(false);
		
		this.left.stop(true);
		this.right.stop(false);
		
		this.left.endSynchronization();
		
		this.left.waitComplete();
		this.right.waitComplete();
	}
	
	public boolean IsMoving()
	{
		return (this.left.isMoving() || this.right.isMoving());
	}
}
