package network;

import com.google.gson.Gson;

public class Helper {
	public static String GetObjectAsString(Object o)
	{
		Gson g = new Gson();
		
		return g.toJson(o);
	}
}
