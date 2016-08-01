package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;
import robot.Driving;

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
		
		//Driving drive = new Driving(new EV3LargeRegulatedMotor(MotorPort.C, MotorPort.D));
	}

}
