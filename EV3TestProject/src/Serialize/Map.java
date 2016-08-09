package Serialize;

import java.util.ArrayList;
import java.util.List;

public class Map {

    private Field[][] Fields;

    private int Id;
    
    public int Get_Id()
    {
    	return this.Id;
    }
	
    public Field[][] Get_Fields()
    {
    	return  this.Fields;
    }
    
    public void Set_Fields(Field[][] newFields)
    {
    	this.Fields = newFields;
    }
    
	public Position GetIndex(int x, int y)
	{
		int minX = this.Fields[0][0].Get_Position().Get_X();
		int minY = this.Fields[0][0].Get_Position().Get_Y();
		
		return new Position(x - minX, y - minY);
	}
	
	public Position GetRelatives(int x, int y)
	{
		return this.Fields[x][y].Get_Position();
	}
	
	public List<Position> ConvertFromRelativeToArrayPositions(List<Position> positions)
	{
		List<Position> converted = new ArrayList<Position>();
		
		for (Position pos : positions)
		{
			Position n = this.GetIndex(pos.Get_X(), pos.Get_Y());
			
			converted.add(n);
		}
		
		return converted;
	}
	
	public List<Position> ConvertFromArrayToRelativePositions(List<Position> positions)
	{
		List<Position> converted = new ArrayList<Position>();
        
        for (Position pos : positions)
        {
        	Position n = this.GetRelatives(pos.Get_X(), pos.Get_Y());
        	
        	converted.add(n);
        }
		
		return converted;
	}
	
	public Field GetFieldByRelativePosition(Position relPos)
	{
		Position newPos = GetIndex(relPos.Get_X(), relPos.Get_Y());
		
		return this.Get_Fields()[newPos.Get_X()][newPos.Get_Y()];
	}
}
