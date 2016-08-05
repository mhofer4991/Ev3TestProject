package calibrating;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import robot.Driving;

public class CalibratingUtil {
	private Driving driving;
	
	private EV3GyroSensor gyro;
	
	private EV3UltrasonicSensor ultra;
	
	public CalibratingUtil(Driving driving, EV3GyroSensor gyro, EV3UltrasonicSensor ultra)
	{
		this.driving = driving;
		this.gyro = gyro;
		this.ultra = ultra;
	}
	
	public float GetDeviceDegreesByMotorDegrees(int motorDegrees)
	{
		float[] oldData = new float[1];
		float[] newData = new float[1];
		
		gyro.getAngleMode().fetchSample(oldData, 0);
		
		driving.TurnRight(motorDegrees, false);
		
		gyro.getAngleMode().fetchSample(newData, 0);
		
		return Math.abs(oldData[0] - newData[0]);
	}
	
	public float GetDeviceDistanceByMotorDegrees(int motorDegrees)
	{
		float[] oldDistance = new float[1];
		float[] newDistance = new float[1];		
		
		ultra.getDistanceMode().fetchSample(oldDistance, 0);
		
		driving.DriveForward(motorDegrees, false);
		
		ultra.getDistanceMode().fetchSample(newDistance, 0);
		
		return oldDistance[0] - newDistance[0];
	}
	
	public void Calibrate(int motorDegrees)
	{
		float deviceDistance = GetDeviceDistanceByMotorDegrees(motorDegrees);
		float deviceDegrees = GetDeviceDegreesByMotorDegrees(motorDegrees);
		
		CalibratingData data = new CalibratingData(motorDegrees, deviceDegrees, deviceDistance);
		
		System.out.println("di: " + Float.toString(deviceDistance));
		System.out.println("de: " + Float.toString(deviceDegrees));
		
		data.SavePersistent();
	}
	
	public static int ConvertDeviceDegreesToMotorDegrees(float deviceDegrees)
	{
		CalibratingData data = CalibratingData.LoadPersistent();
		int ret = (int)((deviceDegrees / data.deviceDegrees) * data.motorDegrees);
		
		return ret;
	}
	
	public static float ConvertMotorDegreesToDeviceDegrees(int motorDegrees)
	{
		CalibratingData data = CalibratingData.LoadPersistent();
		float ret = ((float)motorDegrees / (float)data.motorDegrees) * data.deviceDegrees;
		
		return ret;
	}
	
	public static int ConvertDeviceDistanceToMotorDegrees(float deviceDistance)
	{
		CalibratingData data = CalibratingData.LoadPersistent();
		int ret = (int)((deviceDistance / data.deviceDistance) * data.motorDegrees);
		
		return ret;
	}
	
	public static float ConvertMotorDegreesToDeviceDistance(int motorDegrees)
	{
		CalibratingData data = CalibratingData.LoadPersistent();
		float ret = ((float)motorDegrees / (float)data.motorDegrees) * data.deviceDistance;
		
		return ret;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
