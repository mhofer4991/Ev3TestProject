package pathfinding;

import java.util.ArrayList;
import java.util.List;

import Serialize.Field;
import Serialize.Fieldstate;
import Serialize.Map;
import Serialize.Position;
import Serialize.Route;

public class Program2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Field[][] fields = new Field[5][5];
		
		for (int i = 0; i < 5; i++)
		{
			for (int j = 0; j < 5; j++)
			{
				fields[i][j] = new Field(i, j);
				fields[i][j].Set_State(Fieldstate.freeScanned);
			}
		}
		
		fields[0][2].Set_State(Fieldstate.occupied);
		fields[2][0].Set_State(Fieldstate.occupied);
		
		Map map = new Map();
		map.Set_Fields(fields);
		
		List<Position> points = new ArrayList<Position>();
		points.add(new Position(0, 0));
		points.add(new Position(0, 4));
		
		Route route = new Route(points);

        IPath path = PathAlgorithm.A_Star();
        List<Position> calc = PathIO.CalculatePath(map, route, path);
		
        
        for (int i = 0; i < calc.size(); i++)
        {
            System.out.print(calc.get(i).Get_X() + "/" + calc.get(i).Get_Y() + "  ->  ");
        }             
	}

}
