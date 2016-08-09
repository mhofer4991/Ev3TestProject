package Serialize;

public class TravelResponse {
    public TravelResponse(int id, boolean accepted, Route createdRoute)
    {
    	this.ID = id;
    	this.Accepted = accepted;
    	this.CreatedRoute = createdRoute;
    }

    public int ID;
    
    public boolean Accepted;

    public Route CreatedRoute;
}
