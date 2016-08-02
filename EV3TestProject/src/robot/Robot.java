package robot;

import calibrating.CalibratingUtil;
import interfaces.Controllable;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
import lejos.robotics.geometry.Point;

public class Robot implements Controllable, RegulatedMotorListener {
	private EV3TouchSensor touch;
	private EV3UltrasonicSensor ultra;
	private EV3ColorSensor color;
	private EV3GyroSensor gyro;
	
	private Driving driving;
	
	private Point position;
	
	// tracking
	private MovementMode currentMovement;
	
	private int lastTachoCount;
		
	public Robot()
	{
		this.touch = new EV3TouchSensor(SensorPort.S1);
		this.ultra = new EV3UltrasonicSensor(SensorPort.S2);
		this.color = new EV3ColorSensor(SensorPort.S3);
		this.gyro = new EV3GyroSensor(SensorPort.S4);
		
		this.position = new Point(0, 0);
		
		EV3LargeRegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.C);
		EV3LargeRegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.D);
		
		left.addListener(this);
		
		this.driving = new Driving(left, right);
	}
	
	public Point GetPosition()
	{
		return this.position;
	}
	
	public Driving GetDriving()
	{
		return this.driving;
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
	public void TurnLuftByDegrees(float degrees) {
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
		this.currentMovement = MovementMode.Drive;
		
		this.driving.DriveForward();
	}

	@Override
	public void DriveBackward() {
		this.currentMovement = MovementMode.Drive;
		
		this.driving.DriveBackward();
	}

	@Override
	public void TurnRight() {
		this.currentMovement = MovementMode.Rotate;
		
		this.driving.TurnRight();
	}

	@Override
	public void TurnLeft() {
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
				lastTachoCount = tachoCount;
			}
		}
	}

	@Override
	public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
		if (this.currentMovement == MovementMode.Drive || this.currentMovement == MovementMode.Rotate)
		{
			if (this.currentMovement == MovementMode.Drive)
			{
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
}
