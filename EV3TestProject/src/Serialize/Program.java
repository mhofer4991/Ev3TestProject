package Serialize;

import com.google.gson.Gson;

public class Program {

	public static void main(String[] args)
	{
		Map pos = new Map();
		Field f = new Field(0,0);
		Field[][] fa = new Field[][]{{f}};
		pos.Set_Fields(fa);
	
		Gson g = new Gson();
	
		System.out.println(g.toJson(pos));
		
		Field m = g.fromJson(jsonString, Field.class);
					
		System.out.println(m.toString());
		System.out.println(m.Get_State());
		System.out.println(m.Get_Position());
		System.out.println(m.Get_Position().Get_X());
		System.out.println(m.Get_Position().Get_Y());	
	}
	
	private static String jsonString = "{ \"State\":1, \"Position\":{\"x\":21,\"y\":6} }";
}