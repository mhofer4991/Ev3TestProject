package Serialize;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.annotations.SerializedName;

public enum Fieldstate {
	@SerializedName("0")
	free(0),
	
	@SerializedName("1")
	occupied(1), 
	
	@SerializedName("2")
	freeScanned(2), 
	
	@SerializedName("3")
	unscanned(3);

	
	private int fieldState;

	private static Map<Integer, Fieldstate> map = new HashMap<Integer, Fieldstate>();
	
	static {
	    for (Fieldstate stateEnum : Fieldstate.values()) {
	        map.put(stateEnum.fieldState, stateEnum);
	    }
	}
	
	private Fieldstate(final int state) { fieldState = state; }
	
	public static Fieldstate valueOf(int fieldState) {
	    return map.get(fieldState);
	}

}
