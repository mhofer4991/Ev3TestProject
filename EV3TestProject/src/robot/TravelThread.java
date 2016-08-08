package robot;

import java.util.List;

import Serialize.Position;
import interfaces.IControllable;
import lejos.robotics.geometry.Point;

public class TravelThread extends Thread {
	private IControllable controllable;
	
	private List<Point> route;
	
	private boolean running;
	
	private Point currentDestination;
	
	public TravelThread(IControllable controllable, List<Point> route)
	{
		this.controllable = controllable;
		this.route = route;
		
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
		
		while (this.running)
		{
			for (Point pos : route)
            {
				this.currentDestination = pos;
				
            	this.controllable.DriveToPosition(pos);
            }
		}
	}
}
