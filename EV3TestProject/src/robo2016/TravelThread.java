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

					float a = pos.x - this.controllable.GetPosition().x;
					float g = pos.y - this.controllable.GetPosition().y;
					float h = (float)Math.sqrt(Math.pow(a, 2) + Math.pow(g, 2));
					
					float angle = (float) (Math.acos(g / h) * (180 / Math.PI));
					
					if (a < 0)
					{
						angle *= -1.0F;
					}

					if (this.running)
					{
						this.controllable.RotateToDegrees(angle);	
					}
					
					if (this.running)
					{
						this.controllable.DriveDistanceForward(h);
					}
					
	            	/*float d = this.controllable.DriveToPosition(pos);
	            	
	            	// TODO:
	            	// change this
	            	
	            	if (this.running)
	            	{
	            		this.controllable.DriveDistanceForward(d);
	            	}*/
				}
            }
		}
		while (this.running && this.repeat);
	}
}
