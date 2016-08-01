package calibrating;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;

public class CalibratingUtil {
	
	public CalibratingUtil()
	{
		
	}
	
	public float GetDeviceDegreesByMotorDegrees(float motorDegrees)
	{
		EV3GyroSensor gy = new EV3GyroSensor(SensorPort.S4);
		float[] oldData = new float[1];
		float[] newData = new float[1];
		
		gy.getAngleMode().fetchSample(oldData, 0);
		
		
		
		
		gy.close();
		
		return 2;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
