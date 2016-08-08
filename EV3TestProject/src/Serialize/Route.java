package Serialize;

import java.util.List;
import java.util.ArrayList;

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
}
