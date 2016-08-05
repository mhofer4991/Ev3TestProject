package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.rmi.server.RemoteServer;

import calibrating.CalibratingUtil;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.HiTechnicAccelerometer;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.geometry.Point;
import lejos.utility.Delay;
import network.RemoteControlServer;
import robo2016.Manager;
import robot.Driving;
import robot.Robot;

public class Program {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		/*RegulatedMotor rem = new EV3LargeRegulatedMotor(MotorPort.D);
		RegulatedMotor rem2 = new EV3LargeRegulatedMotor(MotorPort.C);
		rem.synchronizeWith(new RegulatedMotor[] { rem2});
		
		rem.startSynchronization();
		rem.forward();
		rem2.forward();
		
		rem.endSynchronization();
		Delay.msDelay(5000);

		rem.startSynchronization();
		rem.stop();
		rem2.stop();
		rem.endSynchronization();*/
		
		
		/*ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            //System.err.println("Could not listen on port: 4444.");
        	LCD.drawString("err 0", 0, 0);
            System.exit(1);
        }

        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
        	LCD.drawString("err 1", 0, 0);
            System.exit(1);
        }

    	BufferedReader in = new BufferedReader(
                new InputStreamReader(
                clientSocket.getInputStream()));
    	
    	String line;
    	String s = "";
    	
    	s = in.readLine();

    	LCD.drawString("success!!", 0, 0);
    	
    	in.close();
    	clientSocket.close();
    	serverSocket.close();
    	
    	LCD.drawString(s, 0, 0);
    	
    	Delay.msDelay(2000);*/

		/*EV3GyroSensor gy = new EV3GyroSensor(SensorPort.S4);
		
		for (int i = 0; i < 4; i++)
		{
			float[] testdata = new float[1];
			gy.getAngleMode().fetchSample(testdata, 0);
			LCD.drawString(Float.toString(testdata[0]), 0, 0);

			Delay.msDelay(5000);
		}*/
		
		/*EV3UltrasonicSensor  us = new EV3UltrasonicSensor(SensorPort.S2);
		float[] testdata = new float[1];
		us.getDistanceMode().fetchSample(testdata, 0);
		LCD.drawString(Float.toString(testdata[0]), 0, 0);*/
		
		//Driving drive = new Driving(new EV3LargeRegulatedMotor(MotorPort.C), new EV3LargeRegulatedMotor(MotorPort.D));
		
		//CalibratingUtil caliu = new CalibratingUtil(drive);
		//LCD.drawString(Float.toString(caliu.GetDeviceDegreesByMotorDegrees(1000)), 0, 0);
		//drive.DriveForward(1000);
		//LCD.drawString(Float.toString(caliu.GetDeviceDistanceByMotorDegrees(1000)), 0, 0);
		//drive.(1, false);
		//drive.TurnRightByDegrees(90, false);
		
		Robot ro = new Robot();
		//CalibratingUtil caliu = new CalibratingUtil(ro.GetDriving());
		
		//RemoteControlServer ser = new RemoteControlServer(ro);
		//ser.SetListener();
		//ser.start();
		
		//ro.AddListener(ser);
		Manager mgr = new Manager(ro);
		mgr.Start();
		
		ro.SetCollisionCheck(true);
		ro.DriveDistanceForward(2);
		//ro.TurnRightByDegrees(-135);
		
		/*ro.DriveToPosition(new Point(0.5F, 0.5F));
		ro.DriveToPosition(new Point(0.5F, -0.5F));
		ro.DriveToPosition(new Point(-0.5F, -0.5F));
		ro.DriveToPosition(new Point(-0.5F, 0.5F));
		ro.DriveToPosition(new Point(0, 0));*/
				
		//System.out.println(ByteOrder.nativeOrder());
		
		//ro.DriveDistanceForward(1);
		/*drive.DriveDistanceForward(1, true);
		HiTechnicAccelerometer ac = new HiTechnicAccelerometer(SensorPort.S1);
		
		float ma;
		float mac;
		float mal;
		
		ma = 0.0F;
		mac = 9.81F;
		mal = 9.81F;
		
		float[] analysis = new float[20];
		int index = 0;
		boolean filled = false;
		
		for (int i = 0; i < 200; i++)
		{
			float[] data = new float[3];
			ac.getAccelerationMode().fetchSample(data, 0);
			//LCD.drawString(Float.toString(data[0]), 0, 0);
			//LCD.drawString(Float.toString(data[1]), 0, 1);
			//LCD.drawString(Float.toString(data[2]), 0, 2);
			//System.out.println(Float.toString(data[1]));
			float len = (float)Math.sqrt(data[0] * data[0] + data[1] * data[1]); // + data[2] * data[2]);

			ma = len * len * len * len;
			analysis[index] = ma;
			index++;
			
			if (index >= 20)
			{
				index = 0;
				filled = true;
			}
			
			if (filled)
			{
				float min = analysis[0];
				float max = analysis[0];
				float val = 0;
				
				for (int j = 0; j < 20; j++)
				{
					if (analysis[j] > max)
					{
						//maxavg += analysis[j];
						max = analysis[j];
					}
					else if (analysis[j] < min)
					{
						min = analysis[j];
					}
				}
				
				val = min / max;
				
				//minavg /= min;
				//maxavg /= (10 - min);
				
				System.out.println(max);
				
				filled = false;
			}
			
			Delay.msDelay(50);
		}*/
		
		
		Delay.msDelay(5000);
	}

}
