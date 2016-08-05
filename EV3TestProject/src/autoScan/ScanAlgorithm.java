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
	
	public ScanAlgorithm(Map map)
	{
		this.abort = false;
	}
	
	public boolean abort;

	public void Scan()
	{
		this.abort = false;
		
		ScanMap scanMap = new ScanMap();
		
		while (!this.abort)
		{
			// TODO: Current Field auf free&scanned setzen
			
			// TODO: Rundumscan bzw.
			// TODO: Scan in die Richtungen wo unscanned oder free ist
						
			// Liste der free Felder
			ArrayList<Position> freeCells = scanMap.GetAllFreeCells();
			ArrayList<Position> routeToNextCell = new ArrayList<Position>();				
			
			// Liste der free Felder durchgehen und Route berechnen											
			int i = 0;
			
			while (routeToNextCell.size() < 2 && i < freeCells.size())
			{				
				// Route berechnen
				ArrayList<Route> startEnd = new ArrayList<Route>();
				Route route = new Route(new Position(/* Current Robot Position*/), scanMap.map.Get_Fields()[freeCells.get(i).Get_X()][freeCells.get(i).Get_Y()].Get_Position());
				startEnd.add(route);
				
				routeToNextCell = PathIO.GetPath(scanMap.map, startEnd, new A_Star()).get(0);
				
				// TChecken ob die Route mind. 2 einträge hat
				if (routeToNextCell.size() >= 2)
				{					
					break;
				}				
				else
				{
					// TODO: Punkt ist nicht erreichbar
					// Punkt auf unscnnned setzen?
				}
				
				i++;
			}
			
			// Wenn keine Felder mehr angefahren werden können
			if (routeToNextCell.size() < 2)
			{
				return;
			}
			
			// TODO: Nächstes freies, ungescanntes Feld anfahren
		}		
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
}
