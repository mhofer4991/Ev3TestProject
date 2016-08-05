package Serialize;

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
}
