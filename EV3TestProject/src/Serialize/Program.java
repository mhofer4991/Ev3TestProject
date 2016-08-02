package Serialize;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class Program {

	public static void main(String[] args)
	{
		Map pos = new Map();
		Field f = new Field(0,0);
		Field[][] fa = new Field[][]{{f}};
		pos.Set_Fields(fa);
	
		Gson g = new Gson();
		
		// Reading from File
		String jsonString = "";
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get("C:\\Users\\Michael\\git\\Ev3TestProject\\EV3TestProject\\Json.txt"));
			jsonString = new String(encoded, StandardCharsets.UTF_8);
			System.out.println(jsonString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
		// Deserializing
		Field m = g.fromJson(jsonString, Field.class);
					
		System.out.println(m.toString());
		System.out.println(m.Get_State());
		System.out.println(m.Get_Position());
		System.out.println(m.Get_Position().Get_X());
		System.out.println(m.Get_Position().Get_Y());
		
		String text = g.toJson(pos);
		
		try {								
			System.out.println(text);
			PrintWriter writer = new PrintWriter("Json.txt");
			writer.println(text);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}					
	}
}