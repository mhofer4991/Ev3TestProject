package pathfinding;

import java.util.ArrayList;
import Serialize.*;

public interface IPath {

	ArrayList<Position> PathFinder(Route route, Map map);		
}
