package pathfinding;

import java.util.ArrayList;
import Serialize.*;

public class Program {

	public static void main(String[] args)
	{
		ArrayList<Edge> routes = new ArrayList<Edge>();
		ArrayList<ArrayList<Position>> myPaths = new ArrayList<ArrayList<Position>>();

        Map map = new Map();
        
        Field[][] fields = new Field[100][100];
        
        for (int i = 0; i < 100; i++)
        {
        	for (int j = 0; j < 100; j++)
        	{
        		fields[i][j] = new Field(10-i,20-j);
        	}
        }              
        
        fields[0][1].Set_State(Fieldstate.occupied);
        fields[0][2].Set_State(Fieldstate.occupied);
        fields[1][0].Set_State(Fieldstate.occupied);
        fields[1][1].Set_State(Fieldstate.occupied);
        
        map.Set_Fields(fields);
        
        Edge route1 = new Edge(new Position(3, 2), new Position(5, 5));
        Edge route2 = new Edge(new Position(5, 5), new Position(5, 0));
        Edge route3 = new Edge(new Position(5, 0), new Position(0, 9));
        Edge route4 = new Edge(new Position(0, 9), new Position(4, 4));
        Edge route5 = new Edge(new Position(4, 4), new Position(0, 0));

        routes.add(route1);
        routes.add(route2);
        routes.add(route3);
        routes.add(route4);
        routes.add(route5);
        
        ArrayList<Position> route = new ArrayList<Position>();
        route.add(new Position(3,2));
        route.add(new Position(5,5));
        route.add(new Position(5,0));
        route.add(new Position(0,9));
        route.add(new Position(4,4));
        route.add(new Position(0,0));
        
        Route r = new Route(route);

        IPath path = PathAlgorithm.A_Star();
        myPaths = PathIO.GetPath(map, routes, path);

        Draw(myPaths);
        
        System.out.println();
        
        ArrayList<Position> completeRoute = new ArrayList<Position>();
        completeRoute.add(myPaths.get(0).get(0));
        
        for (int i = 0; i < myPaths.size(); i++)
        {
        	for (int j = 1; j < myPaths.get(i).size(); j++)
			{
				completeRoute.add(myPaths.get(i).get(j));
        	}
        }   
        
        ArrayList<Position> completeRoute2 = PathIO.CalculatePath(map, r, path);
        
        for (int i = 0; i < completeRoute.size(); i++)
        {
            System.out.print(completeRoute.get(i).Get_X() + "/" + completeRoute.get(i).Get_Y() + "  ->  ");
        }    
        
        System.out.println();
        
        for (int i = 0; i < completeRoute2.size(); i++)
        {
            System.out.print(completeRoute2.get(i).Get_X() + "/" + completeRoute2.get(i).Get_Y() + "  ->  ");
        }             
	}	
	
    public static void Draw(ArrayList<ArrayList<Position>> paths)
    {
        for (ArrayList<Position> path : paths)
        {
            for (int i = 0; i < path.size(); i++)
            {
                System.out.print(path.get(i).Get_X() + "/" + path.get(i).Get_Y() + "  ->  ");
            }

            System.out.println();
        }
    }
}
