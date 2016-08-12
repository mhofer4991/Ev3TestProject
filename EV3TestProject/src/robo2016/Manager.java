package robo2016;

import java.util.ArrayList;
import java.util.List;

import Serialize.Field;
import Serialize.Fieldstate;
import Serialize.Map;
import Serialize.Position;
import Serialize.RoboStatus;
import Serialize.Route;
import calibrating.CalibratingUtil;
import interfaces.IAlgorithmHelper;
import interfaces.ILogger;
import interfaces.RemoteControlListener;
import interfaces.RobotStatusListener;
import interfaces.TravelListener;
import lejos.remote.ev3.EV3Request.Request;
import lejos.robotics.geometry.Point;
import Serialize.TravelRequest;
import Serialize.TravelResponse;
import autoScan.ScanAlgorithm;
import autoScan.ScanMap;
import network.RemoteControlServer;
import pathfinding.IPath;
import pathfinding.PathAlgorithm;
import pathfinding.PathIO;
import robot.Robot;

public class Manager implements RemoteControlListener, RobotStatusListener, IAlgorithmHelper, ILogger, TravelListener {
	private Robot managedRobot;
	
	private ScanMap scannedMap;
	
	private Point lastRobotPosition;
	
	//private List<Point> movingHistory;
	
	private float cellStep = 0.5F;
	
	// Travelling
	private TravelThread travelThread;
	
	private TravelRequest travelRequest;
	
	// Save response also for reconnections
	private TravelResponse travelResponse;
	
	// Save the index of the next point of the requested travel route, which has to be reached.
	private int travelIndex;
	
	// Remote
	
	private RemoteControlServer remoteServer;
	
	private boolean isConnectedToRemote;
	
	private float wishedDegrees;
	
	// Manager properties
	
	private ManagerState currentState;
	
	// Scan algorithm
	private ScanAlgorithm autoScanAlgorithm;
	
	
	public Manager()
	{
		this.managedRobot = new Robot();		
		this.managedRobot.AddListener(this);
		this.managedRobot.SetLogger(this);
		this.lastRobotPosition = this.managedRobot.GetPosition();
		//this.movingHistory = new ArrayList<Point>();
		
		this.remoteServer = new RemoteControlServer(this);
		
		this.scannedMap = new ScanMap();
		this.currentState = ManagerState.Idle;
	}
	
	public void Start()
	{
		this.remoteServer.start();
	}
	
	public void Stop()
	{
		this.remoteServer.Stop();
		this.CancelRoute();
	}
	
	//
	// Robo status listener
	//

	@Override
	public void RobotStatusUpdated(RoboStatus status) {
		if (this.currentState == ManagerState.ManualScan)
		{
			// In manual mode we track each position change 
			// and add it to the map
			//Position start = new Position((int)(Math.round(this.movingHistory.get(0).x / cellStep)), (int)(Math.round(this.movingHistory.get(0).y / cellStep)));
			//Position end = new Position((int)(Math.round(status.X / cellStep)), (int)(Math.round(status.Y / cellStep)));
			Position start = this.ConvertFromAbsoluteToRelative(lastRobotPosition);
			Position end = this.ConvertFromAbsoluteToRelative(new Point(status.X, status.Y));
			
			if (Math.sqrt(Math.pow(start.Get_X() - end.Get_X(), 2) + Math.pow(start.Get_Y() - end.Get_Y(), 2)) > 0)
			{
				System.out.println(status.X + " - " + status.Y);
				System.out.println(start.Get_X() + " - " + start.Get_Y());
				System.out.println(end.Get_X() + " - " + end.Get_Y());
				
				this.scannedMap.AddScanResult(start, end, Fieldstate.freeScanned);
				
				this.remoteServer.SendMapUpdate(scannedMap.map);
			}
		}
		else if (this.currentState == ManagerState.AutoScan)
		{
			// Notify algorithm
			//Position pos = new Position((int)(Math.round(status.X / cellStep)), (int)(Math.round(status.Y / cellStep)));
			Position pos = this.ConvertFromAbsoluteToRelative(new Point(status.X, status.Y));
			
			if (this.autoScanAlgorithm != null)
			{
				this.autoScanAlgorithm.UpdateRoboPosition(pos);
			}
		}
		
		if (this.isConnectedToRemote)
		{
			this.remoteServer.SendRoboStatus(managedRobot.GetStatus());
		}
		
		this.lastRobotPosition = new Point(status.X, status.Y);
	}

	@Override
	public void RobotStoppedDueToObstacle(RoboStatus status, Point obstaclePosition) {
		Position relObstaclePos = this.ConvertFromAbsoluteToRelative(obstaclePosition);
		Position relRobotPos = this.ConvertFromAbsoluteToRelative(new Point(status.X, status.Y));
		ManagerState state = this.currentState;
		
		if (state == ManagerState.AutoScan)
		{				
			this.AutomaticScanModeExited();
			this.managedRobot.ObstacleGone();
			
			//Position pos = new Position((int)(Math.round(status.X / cellStep)), (int)(Math.round(status.Y / cellStep)));
			//Position arrPos = scannedMap.map.GetIndex(pos.Get_X(), pos.Get_Y());
			//this.scannedMap.map.Get_Fields()[arrPos.Get_X()][arrPos.Get_Y()].Set_State(Fieldstate.occupied);
			
			//this.scannedMap.map.GetFieldByRelativePosition(relRobotPos).Set_State(Fieldstate.occupied);
			//this.scannedMap.map.GetFieldByRelativePosition(relObstaclePos).Set_State(Fieldstate.occupied);
			
			//Log("prepare to restart auto mode");
			
			//this.AutomaticScanModeStarted();
		}
		else if (state == ManagerState.TravelRoute)
		{
			Log("obstacle while travel");

			this.CancelRouteRequested();
			this.managedRobot.ObstacleGone();
		}

		// how to make sure that the robot is not in the same cell as the obstacle????
		// solution #1
		// drive 0.5 meter backward
		// Better: drive to another position
		//this.managedRobot.DriveDistanceBackward(0.5F);
		
		// solution #2
		// look for best rotation to escape
		this.managedRobot.DriveDistanceBackward(0.1F);
		boolean escapeFound = false;
		
		for (int i = 1; i <= 3 && !escapeFound; i++)
		{
			//this.managedRobot.RotateToDegrees(i * 90);
			this.managedRobot.TurnRightByDegrees(i * 90.0F);
			float d = this.managedRobot.ScanDistance();
			
			if (d >= 0.5F)
			{
				escapeFound = true;
				this.managedRobot.DriveDistanceForward(0.5F);
			}
		}

		// solution #3
		// Drive to free point
		/*List<Position> routeBack = new ArrayList<Position>();
		routeBack.add(relRobotPos);
		
		// if the robot has the same position as the obstacle,
		// move to the nearest free cell.
		if (relObstaclePos.Get_X() == relRobotPos.Get_X() && relObstaclePos.Get_Y() == relRobotPos.Get_Y())
		{
			Position arrPos = this.scannedMap.map.GetIndex(relRobotPos.Get_X(), relRobotPos.Get_Y());
			int x = arrPos.Get_X();
			int y = arrPos.Get_Y();
			List<Position> possible = new ArrayList<Position>();
			
			if (x - 1 >= 0)
			{
				Field f = this.scannedMap.map.Get_Fields()[x - 1][y];
				
				if (f.Get_State() == Fieldstate.free || f.Get_State() == Fieldstate.freeScanned)
				{
					possible.add(f.Get_Position());
				}
			}
			else if (x + 1 < this.scannedMap.map.Get_Fields().length)
			{
				Field f = this.scannedMap.map.Get_Fields()[x + 1][y];
				
				if (f.Get_State() == Fieldstate.free || f.Get_State() == Fieldstate.freeScanned)
				{
					possible.add(f.Get_Position());
				}
			}
			else if (y - 1 >= 0)
			{
				Field f = this.scannedMap.map.Get_Fields()[x][y - 1];
				
				if (f.Get_State() == Fieldstate.free || f.Get_State() == Fieldstate.freeScanned)
				{
					possible.add(f.Get_Position());
				}
			}
			else if (y + 1 < this.scannedMap.map.Get_Fields()[0].length)
			{
				Field f = this.scannedMap.map.Get_Fields()[x][y + 1];
				
				if (f.Get_State() == Fieldstate.free || f.Get_State() == Fieldstate.freeScanned)
				{
					possible.add(f.Get_Position());
				}
			}
			
			if (possible.size() > 0)
			{
				routeBack.add(possible.get(0));
			}
		}

		try {
			this.StartRoute(new Route(routeBack), false);
			this.travelThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		// Update scan map
		//this.scannedMap.map.GetFieldByRelativePosition(relObstaclePos).Set_State(Fieldstate.occupied);
		this.scannedMap.AddScanResult(relObstaclePos, relObstaclePos, Fieldstate.occupied);
		
		this.remoteServer.SendMapUpdate(scannedMap.map);
		
		
		
		
		if (state == ManagerState.AutoScan)
		{
			Log("prepare to restart auto mode");
			
			this.AutomaticScanModeStarted();
		}
		else if (state == ManagerState.TravelRoute)
		{			
			// Create new travel request from current travel request
			if (travelRequest != null)
			{
				List<Position> newRoute = new ArrayList<Position>();
				
				newRoute.add(relRobotPos);
				
				// add remaining route points
				for (int i = travelIndex; i < travelRequest.TravelledRoute.Get_Route().size(); i++)
				{
					Position pos = travelRequest.TravelledRoute.Get_Route().get(i);
					
					// Ignore points, which are equal and in a row
					if (newRoute.get(newRoute.size() - 1).Get_X() != pos.Get_X() ||
						newRoute.get(newRoute.size() - 1).Get_Y() != pos.Get_Y())
					{
						newRoute.add(travelRequest.TravelledRoute.Get_Route().get(i));
					}
				}
				
				for (int i = 0; i < travelIndex; i++)
				{
					Position pos = travelRequest.TravelledRoute.Get_Route().get(i);
					
					// Ignore points, which are equal and in a row
					if (newRoute.get(newRoute.size() - 1).Get_X() != pos.Get_X() ||
						newRoute.get(newRoute.size() - 1).Get_Y() != pos.Get_Y())
					{
						newRoute.add(travelRequest.TravelledRoute.Get_Route().get(i));
					}
				}
				
				TravelRequest newRequest = new TravelRequest(travelRequest.ID, scannedMap.map, new Route(newRoute));
				
				this.TravelRouteRequested(newRequest);
			}
		}
	}
	
	//
	// Remote listener
	//

	@Override
	public void ConnectedToRemote() {
		this.isConnectedToRemote = true;		

		this.remoteServer.SendRoboStatus(this.managedRobot.GetStatus());
		
		if (this.currentState == ManagerState.TravelRoute)
		{
			this.remoteServer.SendMapUpdate(this.scannedMap.map);
			
			if (this.travelResponse != null)
			{
				this.remoteServer.SendTravelResponse(this.travelResponse);
			}
		}
		else if (this.currentState == ManagerState.AutoScan)
		{
			this.remoteServer.SendMapUpdate(this.scannedMap.map);
		}
		else if (this.currentState == ManagerState.ManualScan)
		{
			this.remoteServer.SendMapUpdate(this.scannedMap.map);
		}
	}

	@Override
	public void DisconnectedFromRemote() {
		this.isConnectedToRemote = true;
	}
	
	// Scan modes

	@Override
	public void ManualScanModeStarted() {
		if (this.currentState == ManagerState.Idle)
		{
			this.currentState = ManagerState.ManualScan;
			
			System.out.println("manual start");
			
			// Reset map?????
			this.wishedDegrees = this.managedRobot.GetRotation();
			this.managedRobot.SetCollisionCheck(false);
		}
	}

	@Override
	public void ManualScanModeExited() {
		this.currentState = ManagerState.Idle;
		
		System.out.println("manual exit");
		
		this.managedRobot.Stop();
	}

	@Override
	public void AutomaticScanModeStarted() {
		if (this.currentState == ManagerState.Idle)
		{
			this.currentState = ManagerState.AutoScan;
			
			System.out.println("auto start");
			
			// Reset map?????
			this.managedRobot.SetCollisionCheck(true);
			
			this.autoScanAlgorithm = new ScanAlgorithm(this.scannedMap, this);
			
			/*Position pos = new Position(
					(int)(Math.round(managedRobot.GetPosition().getX() / cellStep)), 
					(int)(Math.round(managedRobot.GetPosition().getY() / cellStep)));*/
			Position pos = this.ConvertFromAbsoluteToRelative(managedRobot.GetPosition());
			
			this.autoScanAlgorithm.UpdateRoboPosition(pos);
			
			this.autoScanAlgorithm.start();
		}
	}

	@Override
	public void AutomaticScanModeExited() {
		if (this.currentState == ManagerState.AutoScan)
		{
			this.currentState = ManagerState.Idle;
			
			System.out.println("auto exit");

			if (this.autoScanAlgorithm != null)
			{
				this.autoScanAlgorithm.Abort();
			}
			
			this.CancelRoute();
			
			/*if (this.autoScanAlgorithm != null)
			{
				try {
		            if (this.autoScanAlgorithm.isAlive())
		            {
		            	this.autoScanAlgorithm.interrupt();
						this.autoScanAlgorithm.join();
		            }
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/

			Log("auto cancelled");

			this.managedRobot.SetCollisionCheck(false);
		}
	}
	
	// -----------------

	@Override
	public void CalibratingRequested() {
		this.managedRobot.Calibrate(1000);
		
		this.remoteServer.SendCalibrationFinished();
	}

	@Override
	public void DriveRobotForward() {
		this.managedRobot.DriveForward();
	}

	@Override
	public void DriveRobotBackward() {
		this.managedRobot.DriveBackward();
	}

	@Override
	public void TurnRobotLeft() {
		this.managedRobot.TurnLeft();
	}

	@Override
	public void TurnRobotLeft(float degrees) {
		wishedDegrees -= degrees;
		//this.managedRobot.TurnLeftByDegrees(degrees);
		this.managedRobot.RotateToDegrees(wishedDegrees);
	}

	@Override
	public void TurnRobotRight() {
		this.managedRobot.TurnRight();
	}

	@Override
	public void TurnRobotRight(float degrees) {
		wishedDegrees += degrees;
		//this.managedRobot.TurnRightByDegrees(degrees);
		this.managedRobot.RotateToDegrees(wishedDegrees);
	}

	@Override
	public void StopRobot() {
		this.managedRobot.Stop();
	}

	@Override
	public void ScanArea() {
		// Abort if we are not in the manual mode
		if (this.currentState != ManagerState.ManualScan)
		{
			return;
		}
		
		float distance = -1;
		float rotation;
		Position start;
		
		if (!this.managedRobot.IsMoving())
		{
			//start = new Position((int)(Math.round(this.managedRobot.GetPosition().x / cellStep)), (int)(Math.round(this.managedRobot.GetPosition().y / cellStep)));
			start = this.ConvertFromAbsoluteToRelative(managedRobot.GetPosition());
			float temp = this.managedRobot.GetRotation();
			
			// First
			this.managedRobot.RotateToDegrees(0);
			rotation = this.managedRobot.GetRotation();
			//distance = managedRobot.ScanDistance() / cellStep;
			distance = this.MeasureDistance();
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Second
			//managedRobot.TurnRightByDegrees(90);
			this.managedRobot.RotateToDegrees(90);
			rotation = this.managedRobot.GetRotation();
			//distance = managedRobot.ScanDistance() / cellStep;
			distance = this.MeasureDistance();
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Third
			//managedRobot.TurnRightByDegrees(90);
			this.managedRobot.RotateToDegrees(180);
			rotation = this.managedRobot.GetRotation();
			//distance = managedRobot.ScanDistance() / cellStep;
			distance = this.MeasureDistance();
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Fourth
			//managedRobot.TurnRightByDegrees(90);
			this.managedRobot.RotateToDegrees(270);
			rotation = this.managedRobot.GetRotation();
			//distance = managedRobot.ScanDistance() / cellStep;
			distance = this.MeasureDistance();
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Return to old rotation
			//managedRobot.TurnRightByDegrees(90);
			managedRobot.RotateToDegrees(temp);
			
			// Send the result to the remote server
			this.remoteServer.SendMapUpdate(scannedMap.map);
		}	
	}

	@Override
	public void MapRequested() {
		this.remoteServer.SendMapResponse(this.scannedMap.map);
	}
	
	//
	// Travelling
	//
	
	/**
	 * Use this method if you want to add extended functionality for some route points
	 * Route = route sent from gui
	 */
	@Override
	public void TravelPartReached(Point position) {
		if (this.currentState == ManagerState.TravelRoute)
		{
			if (travelRequest != null)
			{
				Position relPos = this.ConvertFromAbsoluteToRelative(managedRobot.GetPosition());
				Position expected = travelRequest.TravelledRoute.Get_Route().get(travelIndex);
				
				if (expected.Get_X() == relPos.Get_X() && expected.Get_Y() == relPos.Get_Y())
				{
					Log("Travel part reached");
					
					travelIndex++;
					
					if (travelIndex >= travelRequest.TravelledRoute.Get_Route().size())
					{
						travelIndex = 0;
					}
				}
			}
		}
	}

	@Override
	public void TravelRouteRequested(Serialize.TravelRequest request) {
		// Abort if we are not in idle or travel mode, because this means
		// that the user wants to scan the map.
		if (this.currentState != ManagerState.Idle && this.currentState != ManagerState.TravelRoute)
		{
			this.remoteServer.SendTravelResponse(new TravelResponse(request.ID, false, request.TravelledRoute));
			
			return;
		}
		
		System.out.println(request.TravelledRoute.Get_Route().size());
		System.out.println("> " + request.TravelledMap.Get_Fields()[1][1].Get_State().ordinal());

		// Convert from relative coordinates to array indices 
		// to make them ready for the a star algorithm
		List<Position> convertedToIndex = request.TravelledMap.ConvertFromRelativeToArrayPositions(request.TravelledRoute.Get_Route());
		
        IPath path = PathAlgorithm.A_Star();
        List<Position> calc = PathIO.CalculatePath(request.TravelledMap, new Route(convertedToIndex), path);
        
        // Convert them back to relative coordinates
        // to make them ready for real life coordinates
        List<Position> convertedToRelative = request.TravelledMap.ConvertFromArrayToRelativePositions(calc);
        
        if (convertedToRelative.isEmpty())
        {
        	this.remoteServer.SendTravelResponse(new TravelResponse(request.ID, false, new Route(convertedToRelative)));
        }
        else
        {    		
    		// Switch to travel mode
    		this.currentState = ManagerState.TravelRoute;
        	this.travelRequest = request;
        	// Reset progress of the route
        	this.travelIndex = 0;
        	this.scannedMap = new ScanMap();
        	this.scannedMap.map = request.TravelledMap;
        	this.managedRobot.SetCollisionCheck(true);
        	this.travelResponse = new TravelResponse(request.ID, true, new Route(convertedToRelative));
        	
        	this.StartRoute(new Route(convertedToRelative), true);
            
            this.remoteServer.SendTravelResponse(travelResponse);
        }
	}

	@Override
	public void CancelRouteRequested() {
		if (this.currentState == ManagerState.TravelRoute)
		{
			this.CancelRoute();
        	this.managedRobot.SetCollisionCheck(false);
			
			this.currentState = ManagerState.Idle;
		}
	}
	
	//
	// ----------
	//

	/**
	 * @param route 
	 * The route must contain relative coordinates
	 */
	private void StartRoute(Route route, boolean repeat)
	{
        // Convert relative coordinates to absolute coordinates (1 -> 50cm for example)
        List<Point> convertedToAbsolute = new ArrayList<Point>();
        
        for (Position pos : route.Get_Route())
        {
        	//Point n = new Point((float)pos.Get_X() * cellStep, (float)pos.Get_Y() * cellStep);
        	Point n = this.ConvertFromRelativeToAbsolute(pos);
        	
        	convertedToAbsolute.add(n);
        }

        this.CancelRoute();
        
        this.travelThread = new TravelThread(this.managedRobot, convertedToAbsolute, repeat);
        this.travelThread.AddListener(this);
        this.travelThread.start();
	}
	
	private void CancelRoute()
	{
        if (this.travelThread != null)
        {
			//this.managedRobot.Stop();
			
        	/*try {
	            if (this.travelThread.IsRunning())
	            {
	            	this.travelThread.CancelRoute();
	            	this.travelThread.interrupt();
					this.travelThread.join();
	            }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
        	this.travelThread.CancelRoute();
        }
		
		System.out.println("route cancelled");
	}
	
	private Position ConvertFromAbsoluteToRelative(Point absolutePosition)
	{
		return new Position(
				(int)(Math.round(absolutePosition.x / cellStep)), 
				(int)(Math.round(absolutePosition.y / cellStep)));
	
	}
	
	private Point ConvertFromRelativeToAbsolute(Position relativePosition)
	{
		return new Point(
				(float)relativePosition.Get_X() * cellStep, 
				(float)relativePosition.Get_Y() * cellStep);
	}
	
	//
	// Algorithm helper
	// Algorithm _only_ uses relative coordinates; not absolute!
	//

	@Override
	public void RotateRobotTo(float degrees) {
		if (!this.autoScanAlgorithm.IsAborted())
		{
			this.managedRobot.RotateToDegrees(degrees);
		}
	}

	@Override
	public float GetRobotRotation() {
		return this.managedRobot.GetRotation();
	}

	@Override
	public void DriveRobotRoute(Route route) {
		if (!this.autoScanAlgorithm.IsAborted())
		{
			try {
				this.StartRoute(route, false);
				
				// The algorithm has to wait until the route is finished.
				this.travelThread.join();
				
				Log("pos reached!");
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	@Override
	public int MeasureDistance() {
		float distance = this.managedRobot.ScanDistance();
		
		if (distance > 0)
		{
			int calc = (int)(distance / cellStep);
			
			return calc;
		}

		return 0;
	}

	@Override
	public void UpdateScanMap(ScanMap map) {
		this.scannedMap = map;
		
		this.remoteServer.SendMapUpdate(this.scannedMap.map);
	}

	@Override
	public void ScanFinished() {
		this.AutomaticScanModeExited();
		
		this.remoteServer.SendAutoScanFinished();
		this.remoteServer.SendMapUpdate(this.scannedMap.map);
	}
	
	//
	// Logging
	//

	@Override
	public void Log(String text) {
		if (this.isConnectedToRemote)
		{
			this.remoteServer.SendLog(text);
		}
	}
}
