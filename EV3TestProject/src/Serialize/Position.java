package Serialize;

public class Position {

	public Position(int xIn, int yIn)
	{
		this.X = xIn;
		this.Y = yIn;
	}
	
	private int X;
	private int Y;
	
	public int Get_X()
	{
		return this.X;
	}
	
	public int Get_Y()
	{
		return this.Y;
	}
	
	public void Set_X(int xNew)
	{
		this.X = xNew;
	}
	
	public void Set_Y(int yNew)
	{
		this.Y = yNew;
	}
}
