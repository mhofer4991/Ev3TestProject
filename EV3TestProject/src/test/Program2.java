package test;

import Serialize.Field;
import Serialize.Fieldstate;
import Serialize.Position;
import autoScan.ScanMap;

public class Program2 {

	public static void main(String[] args) {
		ScanMap map = new ScanMap();
		
		Field[][] fields = new Field[1][1];
        
        for (int i = 0; i < 1; i++)
        {
        	for (int j = 0; j < 1; j++)
        	{
        		fields[i][j] = new Field(i,j);
        	}
        }              
        
        /*fields[0][0].Set_State(Fieldstate.unscanned);
        fields[0][1].Set_State(Fieldstate.unscanned);
        fields[0][2].Set_State(Fieldstate.unscanned);
        fields[1][0].Set_State(Fieldstate.unscanned);*/
        fields[0][0].Set_State(Fieldstate.free);
        /*fields[1][2].Set_State(Fieldstate.unscanned);
        fields[2][0].Set_State(Fieldstate.unscanned);
        fields[2][1].Set_State(Fieldstate.unscanned);
        fields[2][2].Set_State(Fieldstate.unscanned);*/
		
        map.map.Set_Fields(fields);
        map.Extend(0, 1);
        //map.Extend(1, 3);
        //map.Extend(2, 3);
        //map.Extend(3, 3);

        DrawMap(map);
                
        map.AddScanResult(90,20, new Position(0,1), Fieldstate.freeScanned);
        //map.AddScanResult(new Position(-1, 2), new Position(-2,2), Fieldstate.freeScanned);
        System.out.println("-----");
        /*System.out.println(map.LineMeetsField(0, 1, 0, -1, 3, 3));
        System.out.println(map.LineMeetsField(0, 1, 0, -1, 2, 2));
        System.out.println(map.LineMeetsField(0, 1, 0, -1, 3, 2));
        System.out.println(map.LineMeetsField(0, 1, 0, -1, 2, 3));*/
        
        //System.out.println(Math.round(0.45F));
        
        DrawMap(map);   
        
        Field[][] d = new Field[2][4];
        
        //System.out.println(d[2].length);
        
        System.out.println(Boolean.toString(Float.NEGATIVE_INFINITY < 2));
	}
	
	public static void DrawMap(ScanMap map)
	{
		for (int i = 0; i < map.map.Get_Fields()[0].length; i++)
		{
			for (int j = 0; j < map.map.Get_Fields().length; j++)
			{
				System.out.print(map.map.Get_Fields()[j][i].Get_State().ordinal() + " ");
			}
			System.out.println();
		}
	}
}
