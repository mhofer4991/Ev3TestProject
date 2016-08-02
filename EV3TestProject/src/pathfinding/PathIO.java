package pathfinding;

import Serialize.*;
import java.util.ArrayList;

public class PathIO {

    public static ArrayList<ArrayList<Position>> GetPath(Map map, ArrayList<Route> routes, IPath pathFindingAlgorithm)
    {
        return A_Star(map, routes, pathFindingAlgorithm);
    }

    private static ArrayList<ArrayList<Position>> A_Star(Map map, ArrayList<Route> routes, IPath pathFindingAlgorithm)
    {            
    	ArrayList<ArrayList<Position>> allPaths = new ArrayList<ArrayList<Position>>();

        for (Route route : routes)
        {
            allPaths.add(pathFindingAlgorithm.PathFinder(route, map));
        }

        return allPaths;
    }
}
