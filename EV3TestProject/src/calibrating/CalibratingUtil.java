package calibrating;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import robot.Driving;

public class CalibratingUtil {
	private Driving driving;
	
	public CalibratingUtil(Driving driving)
	{
		this.driving = driving;
	}
	
	public float GetDeviceDegreesByMotorDegrees(int motorDegrees)
	{
		EV3GyroSensor gy = new EV3GyroSensor(SensorPort.S4);
		float[] oldData = new float[1];
		float[] newData = new float[1];
		
		gy.getAngleMode().fetchSample(oldData, 0);
		
		driving.TurnRight(motorDegrees, false);
		
		gy.getAngleMode().fetchSample(newData, 0);
		
		gy.close();
		
		return Math.abs(oldData[0] - newData[0]);
	}
	
	public float GetDeviceDistanceByMotorDegrees(int motorDegrees)
	{
		EV3UltrasonicSensor us = new EV3UltrasonicSensor(SensorPort.S2);
		float[] oldDistance = new float[1];
		float[] newDistance = new float[1];		
		
		us.getDistanceMode().fetchSample(oldDistance, 0);
		
		driving.DriveForward(motorDegrees, false);
		
		us.getDistanceMode().fetchSample(newDistance, 0);
		
		return oldDistance[0] - newDistance[0];
	}
	
	public void Calibrate(int motorDegrees)
	{
		float deviceDegrees = GetDeviceDegreesByMotorDegrees(motorDegrees);
		float deviceDistance = GetDeviceDistanceByMotorDegrees(motorDegrees);
		
		CalibratingData data = new CalibratingData(motorDegrees, deviceDegrees, deviceDistance);
		
		data.SavePersistent();
	}
	
	public static int ConvertDeviceDegreesToMotorDegrees(float deviceDegrees)
	{
		CalibratingData data = CalibratingData.LoadPersistent();
		int ret = (int)((deviceDegrees / data.deviceDegrees) * data.motorDegrees);
		
		return ret;
	}
	
	public static int ConvertDeviceDistanceToMotorDegrees(float deviceDistance)
	{
		CalibratingData data = CalibratingData.LoadPersistent();
		int ret = (int)((deviceDistance / data.deviceDistance) * data.motorDegrees);
		
		return ret;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
