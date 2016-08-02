package Serialize;

import java.util.List;

public class Route {

    public Route(List<Position> route)
    {
        this.Points = route;
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
		return this.Points.get(this.Points.size());
	}
}
