package autoScan;

import java.util.ArrayList;
import Serialize.*;

public class ScanMap {

	public ScanMap()
	{
		this.map = new Map();
	}
	
	public Map map;	
	
	// Adds the state of the scanresult to all fields
	public void AddScanResult(float direction, float freeDistance, Position startPosition, Fieldstate state)
	{		
		direction -= 90;
		
		// Distanzen in X und Y richtig berechnen
		int distanceX = (int)(freeDistance * Math.sin(Math.toRadians(direction)));
		int distanceY = (int)(freeDistance * Math.cos(Math.toRadians(direction)));
		
		int maxX = (startPosition.Get_X() + distanceX);
		int maxY = (startPosition.Get_Y() + distanceY);
		
		this.AddScanResult(startPosition, new Position(maxX, maxY), state);
	}
	
	public void AddScanResult(Position startPosition, Position endPosition, Fieldstate state)
	{
		int minX = startPosition.Get_X();
		int minY = startPosition.Get_Y();
		int maxX = endPosition.Get_X();
		int maxY = endPosition.Get_Y();
				
		// Checken um wieviel das Array zu klein ist	
		// Differenz zwischen größtem x und y bei scan und array		
		//int posX = this.map.Get_Fields()[this.map.Get_Fields().length - 1][0].Get_Position().Get_X() - maxX;
		int posX = maxX - this.map.Get_Fields()[this.map.Get_Fields().length - 1][0].Get_Position().Get_X();
		int negX = this.map.Get_Fields()[0][0].Get_Position().Get_X() - minX;
		int posY = maxY - this.map.Get_Fields()[0][this.map.Get_Fields()[0].length - 1].Get_Position().Get_Y();
		int negY = this.map.Get_Fields()[0][0].Get_Position().Get_Y() - minY;
		
		// Array entsprechend vergrößern
		// mittels differnz entsprechend vergrößern		
		if (posX > 0)
		{
			this.Extend(0, posX);
		}
		
		if (posY > 0)
		{
			this.Extend(1, posY);
		}
		
		if (negX > 0)
		{
			this.Extend(2, negX);
		}
		
		if (negY > 0)
		{
			this.Extend(3, negY);
		}
		
		minX -= 0.5F;
		minY -= 0.5F;
		maxX -= 0.5F;
		maxY -= 0.5F;
		
		// Alle gescannten Zellen auf free setzen
		// Alle Zellen durchgehen, und die die gescannt wurden(Linie geht durch) auf free setzen
		for (int i = 0; i < this.map.Get_Fields().length; i++)
		{
			for (int j = 0; j < this.map.Get_Fields()[0].length; j++)
			{				
				if ((this.LineMeetsField(minX, minY, maxX, maxY, i, j)) && (this.map.Get_Fields()[i][j].Get_State() == Fieldstate.unscanned))
				{
					this.map.Get_Fields()[i][j].Set_State(state);
				}
			}
		}		
	}
	

	// Gets the indices of all free cells on the map
	public ArrayList<Position> GetAllFreeCells()
	{
		ArrayList<Position> freeCells = new ArrayList<Position>();
		
		// Freie Zellen in diese Liste geben.
		for (int i = 0; i < this.map.Get_Fields().length; i++)
		{
			for (int j = 0; j < this.map.Get_Fields()[0].length; j++)
			{
				if (this.map.Get_Fields()[i][j].Get_State() == Fieldstate.free)
				{
					freeCells.add(new Position(i, j));
				}
			}
		}
		
		return freeCells;
	}
	
	// Extends the map in a given direction: 0 - up, 1 - right, 2 - down, 3 - left
	public void Extend(int direction, int amount) throws IllegalArgumentException
	{
		Field[][] current = this.map.Get_Fields();
		Field[][] next;
		
		if(direction == 0 || direction == 2)
		{
			next = new Field[current.length][current[0].length + amount];
		}
		else if (direction == 1 || direction == 3)
		{
			next = new Field[current.length + amount][current[0].length];	
		}
		else
		{
			throw new IllegalArgumentException(); 
		}
		
		int minX = current[0][0].Get_Position().Get_X();
		int minY = current[0][0].Get_Position().Get_Y();	
		
		int currAmountX = current.length;
		int currAmountY = current[0].length;
		
		int nextAmountX = next.length;
		int nextAmountY = next[0].length;
		
		switch (direction)
		{
			// Extend to positive Y
			case 0:
				// Alle X durchgehen
				for (int i = 0; i < nextAmountX; i++)
				{
					// Alle bisherigen Y einfügen
					for (int j = 0; j < currAmountY; j++)
					{
						next[i][j] = current[i][j];
					}
					
					// Alle neuen Y einfügen
					for (int j = 0; j < amount; j++)
					{
						next[i][currAmountY + j] = new Field(minX + i, minY + j + currAmountY);
					}
				}
				
				break;
				
			// Extend to positive X
			case 1:
				// Alle Y durchgehen
				for (int j = 0; j < nextAmountY; j++)
				{
					// Alle bisherigen X einfügen
					for (int i = 0; i < currAmountX; i++)
					{
						next[i][j] = current[i][j];
					}				
					
					// Alle neuen X einfügen
					for (int i = 0; i < amount; i++)
					{
						next[currAmountX + i][j] = new Field(minX + i + currAmountX, minY + j);
					}						
				}
				
				break;
				
			// Extend to negative Y
			case 2:
				// Alle X durchgehen
				for (int i = 0; i < nextAmountX; i++)
				{
					// Alle neuen Y einfügen
					for (int j = 0; j < amount; j++)
					{
						next[i][j] = new Field(minX + i, minY - amount + j);
					}
					
					// Alle bisherigen Y einfügen
					for (int j = 0; j < currAmountY; j++)
					{
						next[i][amount + j] = current[i][j];
					}					
				}
			
				break;
				
			// Extend to negative X
			case 3:
				// Alle Y durchgehen
				for (int j = 0; j < nextAmountY; j++)
				{
					// Alle neuen X einfügen
					for (int i = 0; i < amount; i++)
					{
						next[i][j] = new Field(minX - amount + i, minY + j);
					}
					
					// Alle bisherigen Y einfügen
					for (int i = 0; i < currAmountX; i++)
					{
						next[amount + i][j] = current[i][j];
					}					
				}
				
				break;
			default:
				break;
		}
		
		this.map.Set_Fields(next);
	}
	
	// Checks if a line meets a field
	public boolean LineMeetsField(float lineXStart, float lineYStart, float lineXEnd, float lineYEnd, int fieldIndexX, int fieldIndexY)
	{
		float squareXStart = this.map.Get_Fields()[fieldIndexX][fieldIndexY].Get_Position().Get_X(); // - 0.5F;
		float squareYStart = this.map.Get_Fields()[fieldIndexX][fieldIndexY].Get_Position().Get_Y(); // - 0.5F;
		//float squareXEnd = squareXStart +0.5F;
		//float squareYEnd = squareYStart +0.5F;
		float squareXEnd = this.map.Get_Fields()[fieldIndexX][fieldIndexY].Get_Position().Get_X() + 1; // + 0.5F;
		float squareYEnd = this.map.Get_Fields()[fieldIndexX][fieldIndexY].Get_Position().Get_Y() + 1; // 0.5F;
				
		//System.out.println(squareXStart + " - " + squareYStart);
		//System.out.println(squareXEnd + " - " + squareYEnd);
		//System.out.println("-----------");
		
		return LineIntersectsSquare(lineXStart, lineYStart, lineXEnd, lineYEnd, squareXStart, squareYStart, squareXEnd, squareYEnd);
	}
	
	// Start is the Bottom Left Position of the Line/Square, End is the Top Right Position of the Line/Square
	public static boolean LineIntersectsSquare (float lineXStart, float lineYStart, float lineXEnd, float lineYEnd, float squareXStart, float squareYStart, float squareXEnd, float squareYEnd)
	{
		// Prüfen ob das Quadrat im rechteck aus start und endpunkt der line liegt
		if ((lineXStart < squareXStart && lineXEnd < squareXStart) || (lineYStart < squareYStart && lineYEnd < squareYStart) || (lineXStart > squareXEnd && lineXEnd > squareXEnd) || (lineYStart > squareYEnd && lineYEnd > squareYEnd))
		{
			return false;
		}
		
		// Start or end inside.
		if ((lineXStart >= squareXStart && lineXStart <= squareXEnd && lineYStart >= squareYStart && lineYStart <= squareYEnd) || (lineXEnd >= squareXStart && lineXEnd <= squareXEnd && lineYEnd >= squareYStart && lineYEnd <= squareYEnd)) 
		{
			return true;
		}
		
		// Steigung der Linie berechnen
		float m = (lineYEnd - lineYStart) / (lineXEnd - lineXStart);

	    float y = m * (squareXStart - lineXStart) + lineYStart;
	    if (y >= squareYStart && y <= squareYEnd)
	    { 
	    	return true;
	    }

	    y = m * (squareXEnd - lineXStart) + lineYStart;
	    if (y >= squareYStart && y <= squareYEnd)
	    { 
	    	return true;
	    }

	    float x = (squareYStart - lineYStart) / m + lineXStart;
	    if (x >= squareXStart && x <= squareXEnd)
	    { 
	    	return true;
	    }

	    x = (squareYEnd - lineYStart) / m + lineXStart;
	    if (x >= squareXStart && x <= squareXEnd)
	    {
	    	return true;
	    }		
		
		return false;
	}
}
