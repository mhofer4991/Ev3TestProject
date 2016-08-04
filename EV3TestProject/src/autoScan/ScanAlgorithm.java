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

	// Start is the Bottom Left Position of the Line/Square, End is the Top Right Position of the Line/Square
	public static boolean LineIntersectsSquare (float lineXStart, float lineYStart, float lineXEnd, float lineYEnd, float squareXStart, float squareYStart, float squareXEnd, float squareYEnd)
	{
		// Prüfen ob das Quadrat im rechteck aus start und endpunkt der line liegt
		if ((lineXStart < squareXStart && lineXEnd < squareXStart) || (lineYStart < squareYStart && lineYEnd < squareYStart) || (lineXStart > squareXEnd && lineXEnd > squareXEnd) || (lineYStart > squareYEnd && lineYEnd > squareYEnd))
		{
			return false;
		}
		
		// Start or end inside.
		if ((lineXStart > squareXStart && lineXStart < squareXEnd && lineYStart > squareYStart && lineYStart < squareYEnd) || (lineXEnd > squareXStart && lineXEnd < squareXEnd && lineYEnd > squareYStart && lineYEnd < squareYEnd)) 
		{
			return true;
		}
		
		// Steigung der Linie berechnen
		float m = (lineYEnd - lineYStart) / (lineXEnd - lineXStart);

	    float y = m * (squareXStart - lineXStart) + lineYStart;
	    if (y > squareYStart && y < squareYEnd) return true;

	    y = m * (squareXEnd - lineXStart) + lineYStart;
	    if (y > squareYStart && y < squareYEnd) return true;

	    float x = (squareYStart - lineYStart) / m + lineXStart;
	    if (x > squareXStart && x < squareXEnd) return true;

	    x = (squareYEnd - lineYStart) / m + lineXStart;
	    if (x > squareXStart && x < squareXEnd) return true;		
		
		return false;
	}
}
