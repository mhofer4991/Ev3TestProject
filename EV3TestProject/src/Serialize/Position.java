package Serialize;

/**
 * 
 * @author Markus
 * Determines the position of a field in a two dimensional array (map).
 * Please do not use it to determine the position of the robot.
 */
public class Position {

	public Position()
	{
		this.X = 0;
		this.Y = 0;
	}
	
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
