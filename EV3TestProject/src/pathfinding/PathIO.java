package pathfinding;

import Serialize.*;
import java.util.ArrayList;
import java.util.List;

public class PathIO {

    public static ArrayList<ArrayList<Position>> GetPath(Map map, ArrayList<Edge> routes, IPath pathFindingAlgorithm)
    {
        return A_Star(map, routes, pathFindingAlgorithm);
    }
    
    public static ArrayList<Position> CalculatePath(Map map, Route route, IPath pathFindingAlgorithm)
    {
    	ArrayList<Edge> routes = new ArrayList<Edge>();
    	List<Position> positions = route.Get_Route();
    	
    	Position previousPos = positions.get(0);
    	
    	for (int i = 1; i < positions.size(); i++)
    	{
    		Position curPos = positions.get(i);
    		
    		routes.add(new Edge(previousPos, curPos));
    		
    		previousPos = curPos;
    	}
    	
    	ArrayList<ArrayList<Position>> myPaths = GetPath(map, routes, pathFindingAlgorithm);
    	
    	ArrayList<Position> completeRoute = new ArrayList<Position>();
        completeRoute.add(myPaths.get(0).get(0));
        
        for (int i = 0; i < myPaths.size(); i++)
        {
        	for (int j = 1; j < myPaths.get(i).size(); j++)
			{
				completeRoute.add(myPaths.get(i).get(j));
        	}
        }
        
        return completeRoute;
    }

    private static ArrayList<ArrayList<Position>> A_Star(Map map, ArrayList<Edge> routes, IPath pathFindingAlgorithm)
    {            
    	ArrayList<ArrayList<Position>> allPaths = new ArrayList<ArrayList<Position>>();

        for (Edge route : routes)
        {
            allPaths.add(pathFindingAlgorithm.PathFinder(route, map));
        }

        return allPaths;
    }
}
