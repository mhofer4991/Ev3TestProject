package autoScan;

import java.util.ArrayList;
import java.util.List;

import Serialize.*;
import pathfinding.*;
import interfaces.*;

public class ScanAlgorithm {
	public ScanAlgorithm(Map map, IAlgorithmHelper roboInfo)
	{
		this.abort = false;
		this.scanMap = new ScanMap();
		this.scanMap.map = map;
		this.roboInfo = roboInfo;
	}
		
	private boolean abort;
	public ScanMap scanMap;
	public Position roboPosition;
	private IAlgorithmHelper roboInfo;

	public void UpdateRoboPosition(Position position)
	{
		this.roboPosition = position;
	}	
	
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
				// Scan to direction -> directions[i];
				// 0 up, 1 right, 2 down, 3 left
				int direction = 90 * i;
				
				// RotateTo Degrees
				roboInfo.RotateRobotTo(direction);
				
				// Scanresult from brick
				int freeDistance = roboInfo.MeasureDistance();
				
				// Scanergebnis einbinden
				this.scanMap.AddScanResult(direction, freeDistance, roboPosition, Fieldstate.free);
				
				// Manager benachrichtigen
				roboInfo.UpdateScanMap(this.scanMap);
			}
			
			// Liste der free Felder
			ArrayList<Position> freeCells = this.scanMap.GetAllFreeCells();
			List<Position> routeToNextCell = new ArrayList<Position>();				
			
			// Liste der free Felder durchgehen und Route berechnen											
			int i = 0;
			
			while (routeToNextCell.size() < 2 && i < freeCells.size())
			{				
				// Route berechnen
				//ArrayList<Edge> startEnd = new ArrayList<Edge>();
				//Edge route = new Edge(roboPosition, this.scanMap.map.Get_Fields()[freeCells.get(i).Get_X()][freeCells.get(i).Get_Y()].Get_Position());
				//startEnd.add(route);
				
				List<Position> positions = new ArrayList<Position>();
				
				positions.add(this.scanMap.map.Get_Fields()[freeCells.get(i).Get_X()][freeCells.get(i).Get_Y()].Get_Position());
				
				Route route = new Route(positions);
				
				routeToNextCell = PathIO.CalculatePath(this.scanMap.map, route, new A_Star());
				
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
			routeToNextCell = this.scanMap.map.ConvertFromArrayToRelativePositions(routeToNextCell);
			
			roboInfo.DriveRobotRoute(new Route(routeToNextCell));
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
