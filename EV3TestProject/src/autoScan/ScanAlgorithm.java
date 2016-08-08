package autoScan;

import java.util.ArrayList;

import Serialize.*;
import pathfinding.*;

public class ScanAlgorithm {
	
	public static void main(String[] args)
	{
		ScanMap sm = new ScanMap();
		
		Field[][] fields = new Field[3][3];
        
        for (int i = 0; i < 3; i++)
        {
        	for (int j = 0; j < 3; j++)
        	{
        		fields[i][j] = new Field(i,j);
        	}
        }              
        
        fields[0][0].Set_State(Fieldstate.occupied);
        fields[0][1].Set_State(Fieldstate.occupied);
        fields[0][2].Set_State(Fieldstate.occupied);
        fields[1][0].Set_State(Fieldstate.occupied);
        fields[1][1].Set_State(Fieldstate.freeScanned);
        fields[1][2].Set_State(Fieldstate.occupied);
        fields[2][0].Set_State(Fieldstate.occupied);
        fields[2][1].Set_State(Fieldstate.occupied);
        fields[2][2].Set_State(Fieldstate.occupied);
		
        sm.map.Set_Fields(fields);
        
		Draw(sm.map);
		
		sm.Extend(0, 1);
		
		Draw(sm.map);
		
		sm.Extend(1, 2);
		
		Draw(sm.map);
		
		sm.Extend(2, 3);
		
		Draw(sm.map);
		
		sm.Extend(3, 5);
		
		Draw(sm.map);
		
		sm.Extend(2, 5);
		
		Draw(sm.map);
		
		sm.Extend(1, 5);
		
		Draw(sm.map);
		
		sm.Extend(0, 5);
		
		Draw(sm.map);
	}
	
	public static void Draw(Map map)
	{
		Field[][] fields =  map.Get_Fields();
		
		for (int i = 0; i < fields.length; i++)
		{
			for (int j = 0; j < fields[0].length; j++)
			{
				
				String text = fields[i][j].Get_Position().Get_X() + " / " + fields[i][j].Get_Position().Get_Y() + " : " + fields[i][j].Get_State();
				System.out.println(text);
				
				/*				
				System.out.print(fields[i][j].Get_State().ordinal());
				*/
			}
			
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
	}
	
	
	
	
	public ScanAlgorithm(Map map)
	{
		this.abort = false;
		this.scanMap = new ScanMap();
		this.scanMap.map = map;
	}
	
	private boolean abort;
	public ScanMap scanMap;
	public Position roboPosition;

	public void Abort()
	{
		this.abort = true;
	}
	
	public void Scan()
	{
		this.abort = false;			
		
		while (!this.abort)
		{
			// Current Field auf free&scanned setzen
			this.scanMap.map.Get_Fields()[roboPosition.Get_X()][roboPosition.Get_Y()].Set_State(Fieldstate.freeScanned);
			
			// Scan in die Richtungen wo unscanned oder free ist
			ArrayList<Integer> directions = new ArrayList<Integer>();
			for (int i = 0; i < 4; i++)
			{							
				if (CheckUnscannd(roboPosition, i))
				{
					directions.add(i);
				}
			}
			
			for (int i = 0; i < directions.size(); i++)
			{
				// TODO: Scan to direction -> directions[i];
				// 0 up, 1 right, 2 down, 3 left
				int direction = 0;
				int freeDistance = 1;
				
				// Scanergebnis einbinden
				this.scanMap.AddScanResult(direction, freeDistance, roboPosition, Fieldstate.free);
			}
			
			// Liste der free Felder
			ArrayList<Position> freeCells = this.scanMap.GetAllFreeCells();
			ArrayList<Position> routeToNextCell = new ArrayList<Position>();				
			
			// Liste der free Felder durchgehen und Route berechnen											
			int i = 0;
			
			while (routeToNextCell.size() < 2 && i < freeCells.size())
			{				
				// Route berechnen
				ArrayList<Edge> startEnd = new ArrayList<Edge>();
				Edge route = new Edge(roboPosition, this.scanMap.map.Get_Fields()[freeCells.get(i).Get_X()][freeCells.get(i).Get_Y()].Get_Position());
				startEnd.add(route);
				
				routeToNextCell = PathIO.GetPath(this.scanMap.map, startEnd, new A_Star()).get(0);
				
				// TChecken ob die Route mind. 2 einträge hat
				if (routeToNextCell.size() >= 2)
				{					
					break;
				}				
				else
				{
					// Punkt ist nicht erreichbar
					// Punkt auf unscnnned setzen
					this.scanMap.map.Get_Fields()[freeCells.get(i).Get_X()][freeCells.get(i).Get_Y()].Set_State(Fieldstate.unscanned);
				}
				
				i++;
			}
			
			// Wenn keine Felder mehr angefahren werden können
			if (routeToNextCell.size() < 2)
			{
				return;
			}
			
			// TODO: Nächstes freies, ungescanntes Feld anfahren
			// Route abfahren
		}		
	}
	
	// Returns a value indicating whether a field needs to be scanned or not.
	public boolean CheckUnscannd(Position pos, Integer direction)
	{
		int x;
		int y;
		
		switch (direction)
		{
			// up
			case 0:
				x = pos.Get_X();
				y = pos.Get_Y() + 1;
				break;
			// right
			case 1:
				x = pos.Get_X() + 1;
				y = pos.Get_Y();
				break;
			// down
			case 2:
				x = pos.Get_X();
				y = pos.Get_Y() - 1;
				break;
			// left
			case 3:
				x = pos.Get_X() - 1;
				y = pos.Get_Y();
				break;				
			default:
				throw new IllegalArgumentException();
		}
		
		if ( x < 0 || y < 0 || x >= this.scanMap.map.Get_Fields().length || y >= this.scanMap.map.Get_Fields()[0].length)
		{
			return true;
		}
		
		switch (this.scanMap.map.Get_Fields()[x][y].Get_State())
		{
			case free:
				return true;
			case freeScanned:
				return false;
			case unscanned:
				return true;
			case occupied:
				return false;	
			default:
				return true;
		}
	}		  	
}
