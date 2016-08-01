package Serialize;

public class Field {

    public Field(int x, int y)
    {
        this.Position = new Position(x,y);
        this.State = Fieldstate.free;
    }

    public Fieldstate State;

    public Position Position;
	
	
}
