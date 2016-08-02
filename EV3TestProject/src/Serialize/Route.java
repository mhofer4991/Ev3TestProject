package Serialize;

import java.util.List;
import java.util.ArrayList;

public class Route {

    public Route(List<Position> route)
    {
        this.Points = route;
    }
    
    public Route(Position Start, Position End)
    {
    	this.Points = new ArrayList<Position>();
    	this.Points.add(Start);
    	this.Points.add(End);
    }

    private List<Position> Points;
	
	public List<Position> Get_Route()
	{
		return this.Points;
	}
	
	public void Set_Route(List<Position> route)
	{
		this.Points = route;
	}
	
	public Position Start()
	{
		return this.Points.get(0);
	}
	
	public Position End()
	{
		return this.Points.get(this.Points.size()-1);
	}
}
