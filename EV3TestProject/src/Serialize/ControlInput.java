package Serialize;

public class ControlInput {
	public int Code;
	
	public boolean Released;
	
	public boolean UseValue;
	
	public float Value;
	
	public ControlInput(int code, boolean released, boolean useValue, float value)
	{
		this.Code = code;
		this.Released = released;
		this.UseValue = useValue;
		this.Value = value;
	}
}
