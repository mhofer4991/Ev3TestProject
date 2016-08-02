package calibrating;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CalibratingData implements java.io.Serializable {
	private final static int DEFAULT_MOTOR_DEGREES = 1000;
	
	private final static float DEFAULT_DEVICE_DEGREES = 190;
			
	private final static float DEFAULT_DEVICE_DISTANCE = 0.3F;
	
	public int motorDegrees;
	
	public float deviceDegrees;
	
	public float deviceDistance;
		
	public CalibratingData(int motorDegrees, float deviceDegrees, float deviceDistance)
	{
		this.motorDegrees = motorDegrees;
		this.deviceDegrees = deviceDegrees;
		this.deviceDistance = deviceDistance;
	}
	
	/*
	 * Much thanks to http://www.tutorialspoint.com/java/java_serialization.htm
	 */
	
	public void SavePersistent()
	{
		try {
			FileOutputStream fileOut = new FileOutputStream("/calibration/default.cd");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
	        out.writeObject(this);
	        
	        out.close();	        
	        fileOut.close();	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static CalibratingData LoadPersistent()
	{
		CalibratingData e = null;
		try
		{
		   FileInputStream fileIn = new FileInputStream("/calibration/default.cd");
		   ObjectInputStream in = new ObjectInputStream(fileIn);
		   e = (CalibratingData) in.readObject();
		   
		   in.close();
		   fileIn.close();
		} catch(IOException i)		
		{
		   i.printStackTrace();		   
		} catch(ClassNotFoundException c)
		{
		   c.printStackTrace();
		}
		
		if (e == null)
		{
			new File("/calibration/").mkdir();
			
			e = new CalibratingData(DEFAULT_MOTOR_DEGREES, DEFAULT_DEVICE_DEGREES, DEFAULT_DEVICE_DISTANCE);
			
			e.SavePersistent();
		}
		  
		return e;
	}
}
