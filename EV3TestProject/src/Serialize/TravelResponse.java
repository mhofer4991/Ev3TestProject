package Serialize;

public class TravelResponse {
    public TravelResponse(int id, Route createdRoute)
    {
    	this.ID = id;
    	this.CreatedRoute = createdRoute;
    }

    public int ID;

    public Route CreatedRoute;
}
