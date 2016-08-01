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
}
