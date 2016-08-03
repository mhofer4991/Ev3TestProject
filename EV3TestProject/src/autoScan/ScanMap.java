package autoScan;

import Serialize.*;

public class ScanMap {

	public ScanMap()
	{
		this.map = new Map();
	}
	
	public Map map;
	
	public void AddScanResult(float direction, float freeDistance, Position startPosition)
	{
		// TODO: Berechnen der benötigten Zellen in X und Y Richtung
		// Für jede Zelle, die Aussenkanten als Gerade und prüfen ob sie sich mit der start ziel geraden schneiden
		
		
		// TODO: Checken ob Array zu klein
		
		// TODO: gegebenfalls Array entsprechend vergrößern
		
		// TODO: Alle gescannten Zellen auf free setzen
		
	}
	
	// Direction: 0 - up, 1 - right, 2 - down, 3 - left
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
}
