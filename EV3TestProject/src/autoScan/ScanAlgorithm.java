package autoScan;

import Serialize.*;

public class ScanAlgorithm {
	
	public static void main(String[] args)
	{
		ScanMap sm = new ScanMap();
		
		Field[][] fields = new Field[3][3];
        
        for (int i = 0; i < 3; i++)
        {
        	for (int j = 0; j < 3; j++)
        	{
        		fields[i][j] = new Field(i,j);
        	}
        }              
        
        fields[0][0].Set_State(Fieldstate.occupied);
        fields[0][1].Set_State(Fieldstate.occupied);
        fields[0][2].Set_State(Fieldstate.occupied);
        fields[1][0].Set_State(Fieldstate.occupied);
        fields[1][1].Set_State(Fieldstate.freeScanned);
        fields[1][2].Set_State(Fieldstate.occupied);
        fields[2][0].Set_State(Fieldstate.occupied);
        fields[2][1].Set_State(Fieldstate.occupied);
        fields[2][2].Set_State(Fieldstate.occupied);
		
        sm.map.Set_Fields(fields);
        
		Draw(sm.map);
		
		sm.Extend(0, 1);
		
		Draw(sm.map);
		
		sm.Extend(1, 2);
		
		Draw(sm.map);
		
		sm.Extend(2, 3);
		
		Draw(sm.map);
		
		sm.Extend(3, 5);
		
		Draw(sm.map);
		
		sm.Extend(2, 5);
		
		Draw(sm.map);
		
		sm.Extend(1, 5);
		
		Draw(sm.map);
		
		sm.Extend(0, 5);
		
		Draw(sm.map);
	}
	
	public boolean abort;
	
	public static void Draw(Map map)
	{
		Field[][] fields =  map.Get_Fields();
		
		for (int i = 0; i < fields.length; i++)
		{
			for (int j = 0; j < fields[0].length; j++)
			{
				
				String text = fields[i][j].Get_Position().Get_X() + " / " + fields[i][j].Get_Position().Get_Y() + " : " + fields[i][j].Get_State();
				System.out.println(text);/*
				
				System.out.print(fields[i][j].Get_State().ordinal());*/
			}
			
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
	}	  
}
