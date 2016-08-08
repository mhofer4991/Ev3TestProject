package robot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Serialize.Map;
import Serialize.RoboStatus;
import Serialize.Route;
import calibrating.CalibratingUtil;
import interfaces.CollisionListener;
import interfaces.IControllable;
import interfaces.RemoteControlListener;
import interfaces.RobotStatusListener;
import Serialize.TravelRequest;
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
import lejos.utility.Delay;

public class Robot implements IControllable, RegulatedMotorListener, CollisionListener, RemoteControlListener {
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
	
	// listeners
	private List<RobotStatusListener> listeners;
	
	// Navigation	
	private PlannedMovement plannedMove;

	private CalibratingUtil calibratingUtil;
		
	public Robot()
	{
		this.touch = new EV3TouchSensor(SensorPort.S1);
		//this.acc = new HiTechnicAccelerometer(SensorPort.S1);
		this.ultra = new EV3UltrasonicSensor(SensorPort.S2);
		this.color = new EV3ColorSensor(SensorPort.S3);
		this.gyro = new EV3GyroSensor(SensorPort.S4);
		
		this.listeners = new ArrayList<RobotStatusListener>();
		this.plannedMove = null;
		
		this.position = new Point(0, 0);
		
		EV3LargeRegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.C);
		EV3LargeRegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.D);
		
		left.addListener(this);
		
		this.driving = new Driving(left, right);
		
		this.collisionThread = new CollisionThread(gyro, ultra, touch);
		this.collisionThread.SetListener(this);
		
		this.collisionThread.start();
		
		this.calibratingUtil = new CalibratingUtil(this.GetDriving(), gyro, ultra);
	}
	
	public Point GetPosition()
	{
		return this.position;
	}
	
	public Driving GetDriving()
	{
		return this.driving;
	}
	
	public float GetRotation()
	{
		float[] data = new float[1];
		
		gyro.getAngleMode().fetchSample(data, 0);
		
		return data[0];
	}
	
	public void SetCollisionCheck(boolean check)
	{
		this.checkForCollisions = check;
	}
	
	public void AddListener(RobotStatusListener listener)
	{
		this.listeners.add(listener);
	}
	
	public RoboStatus GetStatus()
	{
		RoboStatus status = new RoboStatus();
		status.X = position.x;
		status.Y = position.y;
		
		float[] data = new float[1];
		
		gyro.getAngleMode().fetchSample(data, 0);
		
		status.Rotation = data[0];
		
		return status;
	}
	
	public void Calibrate(int motorDegrees)
	{
		this.calibratingUtil.Calibrate(motorDegrees);
	}
	
	//
	// Controllable interface
	//

	@Override
	public void DriveDistanceForward(float distance) {
		this.currentMovement = MovementMode.Drive;
		
		this.plannedMove = new PlannedMovement(this.currentMovement, distance);
		
		this.driving.DriveDistanceForward(distance, false);
	}

	@Override
	public void DriveDistanceBackward(float distance) {
		this.currentMovement = MovementMode.Drive;
		
		this.plannedMove = new PlannedMovement(this.currentMovement, distance);
		
		this.driving.DriveDistanceBackward(distance, false);
	}

	@Override
	public void TurnLeftByDegrees(float degrees) {
		this.currentMovement = MovementMode.Rotate;
		
		this.plannedMove = new PlannedMovement(this.currentMovement, degrees);
		
		this.driving.TurnLeftByDegrees(degrees, false);
	}

	@Override
	public void TurnRightByDegrees(float degrees) {
		this.currentMovement = MovementMode.Rotate;
		
		this.plannedMove = new PlannedMovement(this.currentMovement, degrees);
		
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

	@Override
	public void DriveToPosition(Point position) {
		float a = position.x - this.position.x;
		float g = position.y - this.position.y;
		float h = (float)Math.sqrt(Math.pow(a, 2) + Math.pow(g, 2));
		
		float angle = (float) (Math.acos(g / h) * (180 / Math.PI));
		
		/*if (g < 0)
		{
			angle = 180 - angle;
		}*/
		
		float[] gyroData = new float[1];
		
		gyro.getAngleMode().fetchSample(gyroData, 0);		
		
		if (a < 0)
		{
			// Consider robots current position
			angle = angle + gyroData[0];
			
			this.TurnLeftByDegrees(angle);
		}
		else
		{
			// Consider robots current position
			angle = angle - gyroData[0];
			System.out.println(angle);
			
			this.TurnRightByDegrees(angle);
		}
		
		this.DriveDistanceForward(h);
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
					this.collisionThread.ResetValues();
					this.collisionThread.WatchForObstacles(true);
				}
				
				lastTachoCount = tachoCount;
			}
			
			lastRotation = GetRotation();
		}
	}

	@Override
	public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
		if (this.currentMovement == MovementMode.Drive || this.currentMovement == MovementMode.Rotate)
		{
			if (this.currentMovement == MovementMode.Drive)
			{		
				// TODO:
				// Is this safe?
				if (this.checkForCollisions)
				{
					this.collisionThread.WatchForObstacles(false);
				}	
				
				// TODO:
				// What to do with very small deltas?
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
				
				//System.out.println(gyroData[0]);
				//System.out.println(distance);
				//System.out.println(delta);
				//System.out.println(this.position.x + " - " + this.position.y);
			}
			
			RoboStatus status = this.GetStatus();
			
			for (RobotStatusListener listener : listeners)
			{
				listener.RobotStatusUpdated(status);
			}
			
			this.currentMovement = MovementMode.Idle;
		}
	}
	
	//
	// Collision listener
	//
	
	@Override
	public void ObstacleDetected(float distance) {
		if (this.currentMovement == MovementMode.Drive)
		{
			int delta = Math.abs(driving.GetLeft().getTachoCount() - lastTachoCount);
			float leftDistance = 0;
			
			if (this.plannedMove != null)
			{
				leftDistance = this.plannedMove.Value - CalibratingUtil.ConvertMotorDegreesToDeviceDistance(delta);
			}
			
			if (leftDistance < distance)
			{
				// Just drive along
			}
			else
			{
				this.Stop();
				
				// Wait for the obstacle to move away.
				float[] data = new float[1];
				boolean rescued = false;
				
				for (int i = 0; i < 5 && !rescued; i++)
				{
					Delay.msDelay(1000);
					System.out.println("look...");
					
					ultra.getDistanceMode().fetchSample(data, 0);
					
					if (leftDistance < data[0] || data[0] > CollisionThread.MIN_DISTANCE * 2)
					{
						// TODO:
						// Better solution?
						this.currentMovement = MovementMode.Drive;
						
						this.plannedMove = new PlannedMovement(this.currentMovement, leftDistance);
						
						this.driving.DriveDistanceForward(leftDistance, true);
					}
				}
				
				if (!rescued)
				{
					
				}
			}
		}
	}

	@Override
	public void UnexpectedRotationDetected() {
		if (this.currentMovement == MovementMode.Drive)
		{
			this.Stop();
			
			// rotate back			
			this.DriveDistanceBackward(0.15F);
			
			float[] data = new float[1];
			
			gyro.getAngleMode().fetchSample(data, 0);	
			
			float diff = data[0] - lastRotation;
			
			/*if (diff < 0)
			{
				this.TurnRightByDegrees(Math.abs(diff));
			}
			else
			{
				this.TurnLeftByDegrees(Math.abs(diff));
			}*/
			
			if (data[0] > lastRotation)
			{
				// robot turned right. correct it by turning left
				this.TurnLeftByDegrees(Math.abs(diff));
			}
			else
			{
				// robot turned left. correct it by turning right
				this.TurnRightByDegrees(Math.abs(diff));
			}
			
			System.out.println("unexpected rotation detected!");
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

	@Override
	public void CalibratingRequested() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void TravelRouteRequested(Serialize.TravelRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void TurnRobotLeft(float degrees) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void TurnRobotRight(float degrees) {
		// TODO Auto-generated method stub
		
	}
}
