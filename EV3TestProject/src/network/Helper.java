package network;

import com.google.gson.Gson;

public class Helper {
	public static String GetObjectAsString(Object o)
	{
		Gson g = new Gson();
		
		return g.toJson(o);
	}
	
	public static <T> T GetObjectFromString(String s, Class<T> c)
	{
		Gson g = new Gson();
		
		return g.fromJson(s, c);
	}
}
