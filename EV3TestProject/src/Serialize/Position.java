package Serialize;

public class Position {

	public Position(int xIn, int yIn)
	{
		this.x = xIn;
		this.y = yIn;
	}
	
	private int x;
	private int y;
	
	public int Get_X()
	{
		return this.x;
	}
	
	public int Get_Y()
	{
		return this.y;
	}
	
	public void Set_X(int xNew)
	{
		this.x = xNew;
	}
	
	public void Set_Y(int yNew)
	{
		this.y = yNew;
	}
}
