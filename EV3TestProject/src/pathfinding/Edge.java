package pathfinding;

import java.util.ArrayList;

import Serialize.Position;

public class Edge {
	private Position start;
	
	private Position end;
    
    public Edge(Position start, Position end)
    {
    	this.start = start;
    	this.end = end;
    }
	
	public Position Start()
	{
		return this.start;
	}
	
	public Position End()
	{
		return this.end;
	}
}
