package pathfinding;

import java.util.ArrayList;

import Serialize.*;

public class Move {

    public ArrayList<Position> Set;

    public Move()
    {
        this.Set = new ArrayList<Position>();
        CreateMoveSet();
    }

    private void CreateMoveSet()
    {
        this.Set.add(new Position(0, 1));
        this.Set.add(new Position(1, 0));
        this.Set.add(new Position(0, -1));
        this.Set.add(new Position(-1, 0));
    }
}
