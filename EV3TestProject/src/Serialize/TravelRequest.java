package Serialize;

public class TravelRequest {
	public TravelRequest(int id, Map travelledMap, Route travelledRoute)
    {
		this.ID = id;
        this.TravelledMap = travelledMap;
        this.TravelledRoute = travelledRoute;
    }

    public Map TravelledMap;

    public Route TravelledRoute;
    
    public int ID;
}
