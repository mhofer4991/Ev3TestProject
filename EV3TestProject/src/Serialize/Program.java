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
	}
}