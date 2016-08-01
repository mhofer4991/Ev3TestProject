package test;

import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class Program {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LCD.drawString("hello world!", 0, 0);
		Delay.msDelay(5000);
	}

}
