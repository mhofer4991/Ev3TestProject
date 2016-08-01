package Serialize;

public class Field {

    public Field(int x, int y)
    {
        this.Position = new Position(x,y);
        this.State = Fieldstate.unscanned;
    }

    private Fieldstate State;

    private Position Position;
	
    public Fieldstate Get_State()
    {
    	return this.State;
    }
    
    public void Set_State(Fieldstate newState)
    {
    	this.State = newState;
    }
    
	public Position Get_Position()
	{
		return this.Position;
	}
    
	public void Set_Position(Position newPosition)
	{
		this.Position = newPosition;
	}
}
