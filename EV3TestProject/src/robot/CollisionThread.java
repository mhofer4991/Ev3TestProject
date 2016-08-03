package robot;

import interfaces.CollisionListener;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.HiTechnicAccelerometer;
import lejos.utility.Delay;

/**
 * 
 * @author Markus
 * Use this class if you want to detect obstacles while driving.
 */
public class CollisionThread extends Thread {
	private final static int CHECK_INTERVAL = 50; // ms
	
	private final static float MIN_DISTANCE = 0.5F; // m
	
	private final static float MAX_ROTATION_CHANGE = 10; // degrees
	
	private EV3GyroSensor gyro;
	
	private EV3UltrasonicSensor ultra;
	
	private HiTechnicAccelerometer acc;
	
	private EV3TouchSensor touch;
	
	private boolean watchForObstacles;
	
	private boolean running;
	
	private CollisionListener listener;

	private float lastRotation;
	
	private int updates;
	
	public CollisionThread(EV3GyroSensor gyro, EV3UltrasonicSensor ultra, EV3TouchSensor touch)
	{
		this.gyro = gyro;
		this.ultra = ultra;
		//this.acc = acc;
		this.touch = touch;
		
		this.watchForObstacles = false;
		this.running = true;
	}
	
	public void SetListener(CollisionListener listener)
	{
		this.listener = listener;
	}
	
	public void WatchForObstacles(boolean watch)
	{
		this.watchForObstacles = watch;

		if (watch)
		{
			float[] data = new float[1];
			gyro.getAngleMode().fetchSample(data, 0);
			
			lastRotation = data[0];
			updates = 0;
		}
	}
	
	public void Stop()
	{
		running = false;
	}
	
	@Override
	public void run()
	{
		float[] ultraData = new float[1];
		float[] gyroData = new float[1];
		float[] touchData = new float[1];
		
		while (running)
		{
			if (this.watchForObstacles)
			{
				// analyse ultra sensor
				ultra.getDistanceMode().fetchSample(ultraData, 0);
				
				if (ultraData[0] < MIN_DISTANCE)
				{
					listener.ObstacleDetected(ultraData[0]);
				}
				
				// analyse gyro sensor
				gyro.getAngleMode().fetchSample(gyroData, 0);
				
				if (updates % 10 == 0)
				{
					if (Math.abs(lastRotation - gyroData[0]) >= MAX_ROTATION_CHANGE)
					{
						listener.UnexpectedRotationDetected();
					}
				}
				
				// analyse touch sensor
				touch.getTouchMode().fetchSample(touchData, 0);
				
				if (touchData[0] == 1)
				{
					listener.BumpedIntoObstacle();
				}
				
				Delay.msDelay(CHECK_INTERVAL);
				updates++;
			}
			else
			{
				Delay.msDelay(1000);
			}
		}
	}
}
