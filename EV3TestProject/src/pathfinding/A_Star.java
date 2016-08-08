package pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import Serialize.*;

import Serialize.Map;
import Serialize.Position;

public class A_Star implements IPath{
	
	 public A_Star()
     {           
         this.Movements = new Move();
     }

     private Move Movements;

     private ArrayList<Position> Path;
   
     private static void ResetFieldValues(Map map)
     {
         for (Field fieldarr[] : map.Get_Fields())
         {
        	 for (Field field : fieldarr)
        	 {
	             field.DistanceStep = Integer.MAX_VALUE - 1;
	             field.IsPath = false;
        	 }
    	 }
     }

     @Override
     public ArrayList<Position> PathFinder(Edge route, Map map)
     {
         ResetFieldValues(map);
                     
         int xStart = route.Start().Get_X();
         int yStart = route.Start().Get_Y();        
         
         /*
         int xe = route.End().Get_X();
         int ye = route.End().Get_Y();

         
         // Ausgabe
         for (int i = 0; i < map.Get_Fields()[0].length; i++)
         {
             for (int j = 0; j < map.Get_Fields().length; j++)
             {
                 if (map.Get_Fields()[j][i].Get_State() == Fieldstate.free)
                 {
                     if ((i == xStart && j == yStart))
                     {
                         System.out.print("1");
                     }
                     else if ((i == xe && j == ye))
                     {
                    	 System.out.print("2");
                     }
                     else
                     {
                         System.out.print("F");
                     }
                 }
                 else if (map.Get_Fields()[j][i].Get_State() == Fieldstate.occupied)
                 {
                     if ((i == xStart && j == yStart))
                     {
                    	 System.out.print("3");
                     }
                     else if ((i == xe && j == ye))
                     {
                    	 System.out.print("4");
                     }
                     else
                     {
                    	 System.out.print("X");
                     }
                 }
                 else if (map.Get_Fields()[j][i].Get_State() == Fieldstate.freeScanned)
                 {
                     if ((i == xStart && j == yStart))
                     {
                    	 System.out.print("5");
                     }
                     else if ((i == xe && j == ye))
                     {
                    	 System.out.print("6");
                     }
                     else
                     {
                    	 System.out.print("S");
                     }
                 }
                 else if (map.Get_Fields()[j][i].Get_State() == Fieldstate.unscanned)
                 {
                     if ((i == xStart && j == yStart))
                     {
                    	 System.out.print("7");
                     }
                     else if ((i == xe && j == ye))
                     {
                    	 System.out.print("8");
                     }
                     else
                     {
                    	 System.out.print("U");
                     }
                 }
             }

             System.out.println();
         }

         System.out.println();
         */
         
         // Start auf 0 setzen
         map.Get_Fields()[xStart][yStart].DistanceStep = 0;

         // alle auf die schrittweite setzen
         while (true)
         {
             boolean progress = false;

             for (Position mainPoint : AllSquares(map))
             {
                 int x = mainPoint.Get_X();
                 int y = mainPoint.Get_Y();

                 if (FieldIsFree(x, y, map))
                 {
                     int passHere = map.Get_Fields()[x][y].DistanceStep;

                     for (Position movePoint : ValidMoves(x, y, map))
                     {
                         int newX = movePoint.Get_X();
                         int newY = movePoint.Get_Y();
                         int newPass = passHere + 1;

                         if (map.Get_Fields()[newX][newY].DistanceStep > newPass)
                         {
                             map.Get_Fields()[newX][newY].DistanceStep = newPass;
                             progress = true;
                         }
                     }
                 }
             }

             if (progress == false)
             {
                 this.HighlightPath(route, map);
                 break;
             }
         }

         return this.Path;
     }

     private void HighlightPath(Edge route, Map map)
     {
         this.Path = new ArrayList<Position>();

         int startX = route.End().Get_X();
         int startY = route.End().Get_Y();

         int endX = route.Start().Get_X();
         int endY = route.Start().Get_Y();

         this.Path.add(new Position(startX, startY));

         while (true)
         {
        	 Position lowestPoint = new Position();

             int lowest = Integer.MAX_VALUE - 1;
             for (Position movePoint : ValidMoves(startX, startY, map))
             {
                 int count = map.Get_Fields()[movePoint.Get_X()][movePoint.Get_Y()].DistanceStep;

                 if (count < lowest)
                 {
                     lowest = count;
                     lowestPoint.Set_X(movePoint.Get_X());
                     lowestPoint.Set_Y(movePoint.Get_Y());
                 }
             }

             if (lowest != Integer.MAX_VALUE - 1)
             {
                 map.Get_Fields()[lowestPoint.Get_X()][lowestPoint.Get_Y()].IsPath = true;
                 startX = lowestPoint.Get_X();
                 startY = lowestPoint.Get_Y();

                 this.Path.add(new Position(startX, startY));
             }
             else
             {
                 break;
             }

             if (startX == endX && startY == endY)
             {
                 Collections.reverse(this.Path);
                 break;
             }
         }
     }

     private static ArrayList<Position> AllSquares(Map map)
     {
    	 ArrayList<Position> result = new ArrayList<Position>();
    	 
         for (int i = 0; i < map.Get_Fields()[0].length; i++)
         {
             for (int j = 0; j < map.Get_Fields().length; j++)
             {
                 result.add(new Position(j, i));
             }
         }
         
         return result;
     }

     private boolean FieldIsFree(int x, int y, Map map)
     {
         if (map.Get_Fields()[x][y].Get_State() != Fieldstate.occupied || map.Get_Fields()[x][y].Get_State() != Fieldstate.unscanned)
         {
             return true;
         }

         return false;
     }

     private boolean CheckMoveIsValid(int x, int y, Map map)
     {
         if (x < 0 || x > map.Get_Fields().length - 1 || y < 0 || y > map.Get_Fields()[0].length - 1)
         {
             return false;
         }

         return true;
     }

     private ArrayList<Position> ValidMoves(int x, int y, Map map)
     {
    	 ArrayList<Position> result = new ArrayList<Position>();
    	 
         for (Position p : this.Movements.Set)
         {
             int newX = x + p.Get_X();
             int newY = y + p.Get_Y();

             if (CheckMoveIsValid(newX, newY, map) && FieldIsFree(newX, newY, map))
             {
                 result.add(new Position(newX, newY));
             }
         }
         
         return result;
     }
 }