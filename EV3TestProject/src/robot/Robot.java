package robot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.print.attribute.standard.DateTimeAtCompleted;

import Serialize.Map;
import Serialize.RoboStatus;
import Serialize.Route;
import calibrating.CalibratingUtil;
import interfaces.CollisionListener;
import interfaces.IControllable;
import interfaces.ILogger;
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

public class Robot implements IControllable, RegulatedMotorListener, CollisionListener {
	public final static float MAX_ULTRA_SONIC_DISTANCE = 2.5F; // meters
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
	
	private boolean awaitsObstacle;
	
	private boolean obstacleMoved;
	
	// listeners
	private List<RobotStatusListener> listeners;
	
	// Navigation	
	private PlannedMovement plannedMove;

	private CalibratingUtil calibratingUtil;
	
	// Logging
	private ILogger logger;
		
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
	
	public Driving GetDriving()
	{
		return this.driving;
	}
	
	public void SetCollisionCheck(boolean check)
	{
		this.checkForCollisions = check;
	}
	
	public void AddListener(RobotStatusListener listener)
	{
		this.listeners.add(listener);
	}
	
	public void SetLogger(ILogger logger)
	{
		this.logger = logger;
	}
	
	public void ObstacleGone()
	{
		this.awaitsObstacle = false;
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
	public float GetRotation()
	{
		float[] data = new float[1];
		
		gyro.getAngleMode().fetchSample(data, 0);
		
		return data[0];
	}
	
	@Override
	public Point GetPosition()
	{
		return this.position;
	}
	
	//
	// Synchronized methods
	//

	@Override
	public synchronized void DriveDistanceForward(float distance) {
		this.currentMovement = MovementMode.Drive;
		Log("drive forward: " + distance);
		
		this.plannedMove = new PlannedMovement(this.currentMovement, distance);
		
		this.driving.DriveDistanceForward(distance, false);
		
		if (awaitsObstacle)
		{
			// If driving forward, it could happen that
			// we have to wait for an obstacle to move away
			while (awaitsObstacle)
			{
				//this.driving.GetLeft().waitComplete();
				//this.driving.GetRight().waitComplete();
				Delay.msDelay(1000);
			}
			
			if (obstacleMoved)
			{
				if (this.plannedMove != null)
				{
					obstacleMoved = false;
					
					this.DriveDistanceForward(plannedMove.Value);
				}
			}
		}
	}

	@Override
	public synchronized void DriveDistanceBackward(float distance) {
		this.currentMovement = MovementMode.Drive;
		Log("drive backward: " + distance);
		
		this.plannedMove = new PlannedMovement(this.currentMovement, distance);
		
		this.driving.DriveDistanceBackward(distance, false);
	}

	@Override
	public synchronized void TurnLeftByDegrees(float degrees) {
		degrees = degrees % 360;
		
		if ((int)degrees == 0)
		{
			return;
		}
		
		if (Math.abs(degrees) > 180.0F)
		{
			if (degrees < 0)
			{
				this.TurnLeftByDegrees(degrees + 360.0F);
			}
			else
			{
				this.TurnLeftByDegrees(degrees - 360.0F);
			}
		}
		else
		{
			if (degrees < 0)
			{
				this.TurnRightByDegrees(degrees * -1.0F);
			}
			else
			{
				this.currentMovement = MovementMode.Rotate;
				Log("rotate left: " + degrees);
				
				this.plannedMove = new PlannedMovement(this.currentMovement, degrees);
				
				this.driving.TurnLeftByDegrees(degrees, false);	
			}
		}
	}

	@Override
	public synchronized void TurnRightByDegrees(float degrees) {
		degrees = degrees % 360;
		
		if ((int)degrees == 0)
		{
			return;
		}
		
		if (Math.abs(degrees) > 180.0F)
		{
			if (degrees < 0)
			{
				this.TurnRightByDegrees(degrees + 360.0F);	
			}
			else
			{
				this.TurnRightByDegrees(degrees - 360.0F);
			}
		}
		else
		{
			if (degrees < 0)
			{
				this.TurnLeftByDegrees(degrees * -1.0F);
			}
			else
			{
				this.currentMovement = MovementMode.Rotate;
				Log("rotate right: " + degrees);
				
				this.plannedMove = new PlannedMovement(this.currentMovement, degrees);
				
				this.driving.TurnRightByDegrees(degrees, false);
			}
		}
	}

	@Override
	public synchronized void RotateToDegrees(float degrees) {
		float[] data = new float[1];
		
		gyro.getAngleMode().fetchSample(data, 0);
		
		data[0] = data[0] % 360;
		
		degrees = degrees % 360;
		
		float diff = data[0] - degrees;
		
		this.TurnLeftByDegrees(diff);
	}
	
	//
	// End of synchronized methods
	//

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
		Log("stop");
		this.driving.Stop();
	}

	@Override
	public boolean IsMoving() {
		//return (driving.GetLeft().isMoving() || driving.GetRight().isMoving());
		return this.driving.IsMoving();
	}

	// Returning the distance is a temporary solution
	// TODO:
	// Move it to travelthread because of better checking 
	// if route is cancelled
	/*@Override
	public float DriveToPosition(Point position) {
		float a = position.x - this.position.x;
		float g = position.y - this.position.y;
		float h = (float)Math.sqrt(Math.pow(a, 2) + Math.pow(g, 2));
		
		float angle = (float) (Math.acos(g / h) * (180 / Math.PI));
		*/
		/*if (g < 0)
		{
			angle = 180 - angle;
		}*/
		
		/*float[] gyroData = new float[1];
		
		gyro.getAngleMode().fetchSample(gyroData, 0);		
		
		if (a < 0)
		{
			// Consider robots current position
			angle = angle + gyroData[0];
			angle = angle % 360.0F;
			
			this.TurnLeftByDegrees(angle);
		}
		else
		{
			// Consider robots current position
			angle = angle - gyroData[0];
			angle = angle % 360.0F;
			
			this.TurnRightByDegrees(angle);
		}*/
		/*if (a < 0)
		{
			angle *= -1.0F;
		}

		this.RotateToDegrees(angle);

		Log("drive to pos: (" + position.x + " | " + position.y + ")");
		//this.DriveDistanceForward(h);
		return h;
	}*/

	@Override
	public float ScanDistance() {
		if (!this.IsMoving())
		{
			float[] data = new float[1];
			
			ultra.getDistanceMode().fetchSample(data, 0);
			
			Log("scanned distance: " + data[0]);
			
			if (Float.isInfinite(data[0]))
			{
				data[0] = MAX_ULTRA_SONIC_DISTANCE;
			}
			else if (Float.isNaN(data[0]))
			{
				data[0] = 0;
			}
			
			return data[0];
		}
		
		return 0;
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
			if (this.checkForCollisions)
			{
				this.collisionThread.WatchForObstacles(false);
			}
			
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
			else if (!awaitsObstacle)
			{
				// Only go on if not already waiting for obstacle
				this.awaitsObstacle = true;
				this.Stop();
				
				// Wait for the obstacle to move away.
				float scannedDistance = 0.0F;
				boolean rescued = false;
				obstacleMoved = false;
				
				Log("wait for rescue");
				
				for (int i = 0; i < 5 && !rescued; i++)
				{
					Delay.msDelay(1000);
					System.out.println("look...");
					
					//ultra.getDistanceMode().fetchSample(data, 0);
					scannedDistance = this.ScanDistance();
					
					if (leftDistance < scannedDistance || scannedDistance > CollisionThread.MIN_DISTANCE * 2)
					{
						rescued = true;
						obstacleMoved = true;
						this.plannedMove = new PlannedMovement(MovementMode.Drive, leftDistance);
						
						awaitsObstacle = false;
						/*
						// TODO:
						// Better solution?
						rescued = true;
						this.waitObstacle = false;
						this.currentMovement = MovementMode.Drive;
						
						this.plannedMove = new PlannedMovement(this.currentMovement, leftDistance);
						
						// True = do not wait for finish, because it would
						// block the collision watch thread.
						this.driving.DriveDistanceForward(leftDistance, true);*/
					}
				}
				
				if (!rescued)
				{
					Log("obstacle detected");
					
					this.NotifyStopEvent();
					
					// Calculate position of obstacle
					Point obstaclePosition = this.position;
					float[] gyroData = new float[1];
					
					gyro.getAngleMode().fetchSample(gyroData, 0);
					
					// 0.25 = 0.5 (cellsize) / 2
					// TODO: constant value or setter / getter
					//data[0] = data[0] + 0.25F;
					scannedDistance += 0.25F;
					
					float g = (float)Math.sin(Math.toRadians(gyroData[0])) * scannedDistance;
					float a = (float)Math.cos(Math.toRadians(gyroData[0])) * scannedDistance;
					
					obstaclePosition = new Point(
							obstaclePosition.x + g, 
							obstaclePosition.y + a);
					
					for (RobotStatusListener listener : listeners)
					{
						listener.RobotStoppedDueToObstacle(this.GetStatus(), obstaclePosition);
					}

					//this.awaitsObstacle = false;
				}
			}
		}
	}

	@Override
	public void UnexpectedRotationDetected() {
		if (this.currentMovement == MovementMode.Drive)
		{
			Log("unexpected rotation");
			this.awaitsObstacle = true;
			this.Stop();
			
			// rotate back			
			//this.DriveDistanceBackward(0.15F);
			this.driving.DriveDistanceBackward(0.15F, false);
			
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
				//this.TurnLeftByDegrees(Math.abs(diff));
				this.driving.TurnLeftByDegrees(Math.abs(diff), false);
			}
			else
			{
				// robot turned left. correct it by turning right
				//this.TurnRightByDegrees(Math.abs(diff));
				this.driving.TurnRightByDegrees(Math.abs(diff), false);
			}
			
			this.NotifyStopEvent();
			
			for (RobotStatusListener listener : listeners)
			{
				listener.RobotStoppedDueToObstacle(this.GetStatus(), this.GetPosition());
			}
			
			//this.awaitsObstacle = false;
			
			//System.out.println("unexpected rotation detected!");
		}
	}

	@Override
	public void BumpedIntoObstacle() {
		if (this.currentMovement == MovementMode.Drive)
		{
			Log("bumped into");
			this.awaitsObstacle = true;
			this.Stop();
			
			this.NotifyStopEvent();
			
			for (RobotStatusListener listener : listeners)
			{
				listener.RobotStoppedDueToObstacle(this.GetStatus(), this.GetPosition());
			}

			//this.awaitsObstacle = false;
		}
	}
	
	private void Log(String text)
	{
		if (this.logger != null)
		{
			this.logger.Log(text);
		}
	}
	
	private void NotifyStopEvent()
	{		
		this.rotationStopped(this.driving.GetLeft(), 
				this.driving.GetLeft().getTachoCount(), 
				this.driving.GetLeft().isStalled(), System.currentTimeMillis());
	}
}
