package robot;

import calibrating.CalibratingUtil;
import interfaces.CollisionListener;
import interfaces.Controllable;
import interfaces.RemoteControlListener;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.HiTechnicAccelerometer;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
import lejos.robotics.geometry.Point;

public class Robot implements Controllable, RegulatedMotorListener, CollisionListener, RemoteControlListener {
	private EV3TouchSensor touch;
	private HiTechnicAccelerometer acc;
	private EV3UltrasonicSensor ultra;
	private EV3ColorSensor color;
	private EV3GyroSensor gyro;
	
	private Driving driving;
	
	private Point position;
	
	// tracking
	private MovementMode currentMovement;
	
	private int lastTachoCount;
	
	private float lastRotation;
	
	// collision detection
	private CollisionThread collisionThread;
	
	private boolean checkForCollisions;
		
	public Robot()
	{
		this.touch = new EV3TouchSensor(SensorPort.S1);
		//this.acc = new HiTechnicAccelerometer(SensorPort.S1);
		this.ultra = new EV3UltrasonicSensor(SensorPort.S2);
		this.color = new EV3ColorSensor(SensorPort.S3);
		this.gyro = new EV3GyroSensor(SensorPort.S4);
		
		this.position = new Point(0, 0);
		
		EV3LargeRegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.C);
		EV3LargeRegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.D);
		
		left.addListener(this);
		
		this.driving = new Driving(left, right);
		
		this.collisionThread = new CollisionThread(gyro, ultra, touch);
		this.collisionThread.SetListener(this);
		
		this.collisionThread.start();
	}
	
	public Point GetPosition()
	{
		return this.position;
	}
	
	public Driving GetDriving()
	{
		return this.driving;
	}
	
	public void SetCollisionCheck(boolean check)
	{
		this.checkForCollisions = check;
	}
	
	//
	// Controllable interface
	//

	@Override
	public void DriveDistanceForward(float distance) {
		this.currentMovement = MovementMode.Drive;
		
		this.driving.DriveDistanceForward(distance, false);
	}

	@Override
	public void DriveDistanceBackward(float distance) {
		this.currentMovement = MovementMode.Drive;
		
		this.driving.DriveDistanceBackward(distance, false);
	}

	@Override
	public void TurnLeftByDegrees(float degrees) {
		this.currentMovement = MovementMode.Rotate;
		
		this.driving.TurnLeftByDegrees(degrees, false);
	}

	@Override
	public void TurnRightByDegrees(float degrees) {
		this.currentMovement = MovementMode.Rotate;
		
		this.driving.TurnRightByDegrees(degrees, false);
	}

	@Override
	public void DriveForward() {
		if (this.currentMovement == MovementMode.Rotate)
		{
			this.Stop();
		}
		
		this.currentMovement = MovementMode.Drive;
		
		this.driving.DriveForward();
	}

	@Override
	public void DriveBackward() {
		if (this.currentMovement == MovementMode.Rotate)
		{
			this.Stop();
		}
		
		this.currentMovement = MovementMode.Drive;
		
		this.driving.DriveBackward();
	}

	@Override
	public void TurnRight() {
		if (this.currentMovement == MovementMode.Drive)
		{
			this.Stop();
		}
		
		this.currentMovement = MovementMode.Rotate;
		
		this.driving.TurnRight();
	}

	@Override
	public void TurnLeft() {
		if (this.currentMovement == MovementMode.Drive)
		{
			this.Stop();
		}
		
		this.currentMovement = MovementMode.Rotate;
		
		this.driving.TurnLeft();
	}

	@Override
	public void Stop() {
		this.driving.Stop();
	}
	
	//
	// motor listener
	//

	@Override
	public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
		if (this.currentMovement == MovementMode.Drive || this.currentMovement == MovementMode.Rotate)
		{
			if (this.currentMovement == MovementMode.Drive)
			{
				if (this.checkForCollisions)
				{
					this.collisionThread.WatchForObstacles(true);
				}
				
				lastTachoCount = tachoCount;
				
				float[] data = new float[1];
				
				gyro.getAngleMode().fetchSample(data, 0);
				lastRotation = data[0];
			}
		}
	}

	@Override
	public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
		if (this.currentMovement == MovementMode.Drive || this.currentMovement == MovementMode.Rotate)
		{
			if (this.currentMovement == MovementMode.Drive)
			{		
				if (this.checkForCollisions)
				{
					this.collisionThread.WatchForObstacles(false);
				}		
				
				int delta = tachoCount - lastTachoCount;
				float distance = CalibratingUtil.ConvertMotorDegreesToDeviceDistance(delta);
				Point newPosition = this.position;
				float[] gyroData = new float[1];
				
				gyro.getAngleMode().fetchSample(gyroData, 0);
				
				float g = (float)Math.sin(Math.toRadians(gyroData[0])) * distance;
				float a = (float)Math.cos(Math.toRadians(gyroData[0])) * distance;
				
				this.position = new Point(
						newPosition.x + g, 
						newPosition.y + a);
			}
		}
	}
	
	//
	// Collision listener
	//
	
	@Override
	public void ObstacleDetected(float remainingDistance) {
		if (this.currentMovement == MovementMode.Drive)
		{
			int delta = Math.abs(driving.GetLeft().getTachoCount() - lastTachoCount);
			float distance = CalibratingUtil.ConvertMotorDegreesToDeviceDistance(delta);
			
			if (distance < remainingDistance)
			{
				// Just drive along
			}
			else
			{
				this.Stop();
			}
		}
	}

	@Override
	public void UnexpectedRotationDetected() {
		if (this.currentMovement == MovementMode.Drive)
		{
			this.Stop();
			
			// rotate back
			float[] data = new float[1];
			
			this.DriveDistanceBackward(0.15F);
			
			gyro.getAngleMode().fetchSample(data, 0);	
			
			float diff = data[0] - lastRotation;
			
			if (data[0] < 0)
			{
				this.TurnRightByDegrees(diff);
			}
			else
			{
				this.TurnLeftByDegrees(diff);
			}
			
			//System.out.println("unexpected rotation detected!");
		}
	}

	@Override
	public void BumpedIntoObstacle() {
		if (this.currentMovement == MovementMode.Drive)
		{
			this.Stop();
			
			this.DriveDistanceBackward(0.15F);
		}
	}
	
	//
	//
	//

	@Override
	public void ConnectedToRemote() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DisconnectedFromRemote() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DriveRobotForward() {
		this.DriveForward();
	}

	@Override
	public void DriveRobotBackward() {
		this.DriveBackward();
	}

	@Override
	public void TurnRobotLeft() {
		this.TurnLeft();
	}

	@Override
	public void TurnRobotRight() {
		this.TurnRight();
	}

	@Override
	public void StopRobot() {
		this.Stop();
	}
}
