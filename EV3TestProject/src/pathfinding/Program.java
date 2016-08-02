package pathfinding;

import java.util.ArrayList;
import Serialize.*;

public class Program {

	public static void main(String[] args)
	{
		ArrayList<Route> routes = new ArrayList<Route>();
		ArrayList<ArrayList<Position>> myPaths = new ArrayList<ArrayList<Position>>();

        Map map = new Map();
        
        Field[][] fields = new Field[6][6];
        
        for (int i = 0; i < 6; i++)
        {
        	for (int j = 0; j < 6; j++)
        	{
        		fields[i][j] = new Field(i,j);
        	}
        }              
        
        fields[5][2].Set_State(Fieldstate.occupied);
        fields[4][2].Set_State(Fieldstate.occupied);
        fields[3][2].Set_State(Fieldstate.occupied);
        fields[3][3].Set_State(Fieldstate.occupied);
        fields[3][4].Set_State(Fieldstate.occupied);
        
        map.Set_Fields(fields);
        
        Route route1 = new Route(new Position(2, 2), new Position(5, 5));
        Route route2 = new Route(new Position(5, 5), new Position(5, 0));
        Route route3 = new Route(new Position(5, 0), new Position(0, 4));
        Route route4 = new Route(new Position(0, 4), new Position(4, 4));
        Route route5 = new Route(new Position(4, 4), new Position(0, 0));

        routes.add(route1);
        routes.add(route2);
        routes.add(route3);
        routes.add(route4);
        routes.add(route5);

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
        
        for (int i = 0; i < completeRoute.size(); i++)
        {
            System.out.print(completeRoute.get(i).Get_X() + "/" + completeRoute.get(i).Get_Y() + "  ->  ");
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
