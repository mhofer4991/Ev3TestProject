package robo2016;

import java.util.List;

import Serialize.Position;
import interfaces.IControllable;
import lejos.robotics.geometry.Point;

public class TravelThread extends Thread {
	private IControllable controllable;
	
	private List<Point> route;
	
	private boolean running;
	
	private boolean repeat;
	
	private Point currentDestination;
	
	public TravelThread(IControllable controllable, List<Point> route, boolean repeat)
	{
		this.controllable = controllable;
		this.route = route;
		this.repeat = repeat;
		
		this.running = false;
		this.currentDestination = null;
	}
	
	public void CancelRoute()
	{
		this.running = false;
		this.controllable.Stop();
	}
	
	public boolean IsRunning()
	{
		return this.running;
	}
	
	public Point GetCurrentDestination()
	{
		return this.currentDestination;
	}
	
	@Override
	public void run()
	{
		this.running = true;
		
		do
		{
			for (Point pos : route)
            {
				if (this.running)
				{
					this.currentDestination = pos;
					
	            	this.controllable.DriveToPosition(pos);
				}
            }
		}
		while (this.running && this.repeat);
	}
}
