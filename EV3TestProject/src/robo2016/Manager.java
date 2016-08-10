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

public class Manager implements RemoteControlListener, RobotStatusListener, IAlgorithmHelper, ILogger {
	private Robot managedRobot;
	
	private ScanMap scannedMap;
	
	private Point lastRobotPosition;
	
	//private List<Point> movingHistory;
	
	private float cellStep = 0.5F;
	
	// Travelling
	private TravelThread travelThread;
	
	private TravelRequest travelRequest;
	
	// Remote
	
	private RemoteControlServer remoteServer;
	
	private boolean isConnectedToRemote;
	
	private float wishedDegrees;
	
	// Manager properties
	
	private ManagerState currentState;
	
	// Scan algorithm
	private ScanAlgorithm autoScanAlgorithm;
	
	
	public Manager(Robot managedRobot)
	{
		this.managedRobot = managedRobot;
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
		if (this.isConnectedToRemote)
		{
			this.remoteServer.SendRoboStatus(managedRobot.GetStatus());
		}
		
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
		
		this.lastRobotPosition = new Point(status.X, status.Y);
	}

	@Override
	public void RobotStoppedDueToObstacle(RoboStatus status) {
		if (this.currentState == ManagerState.AutoScan)
		{				
			this.AutomaticScanModeExited();
			
			//Position pos = new Position((int)(Math.round(status.X / cellStep)), (int)(Math.round(status.Y / cellStep)));
			//Position arrPos = scannedMap.map.GetIndex(pos.Get_X(), pos.Get_Y());
			//this.scannedMap.map.Get_Fields()[arrPos.Get_X()][arrPos.Get_Y()].Set_State(Fieldstate.occupied);
			Position pos = this.ConvertFromAbsoluteToRelative(new Point(status.X, status.Y));
			
			this.scannedMap.map.GetFieldByRelativePosition(pos).Set_State(Fieldstate.occupied);
			
			this.remoteServer.SendMapUpdate(scannedMap.map);
			
			// Better: drive to another position
			this.managedRobot.DriveDistanceBackward(0.5F);
			
			Log("prepare to restart auto mode");
			
			this.AutomaticScanModeStarted();
		}
	}
	
	//
	// Remote listener
	//

	@Override
	public void ConnectedToRemote() {
		this.isConnectedToRemote = true;
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

			this.autoScanAlgorithm.Abort();
			
			this.CancelRoute();
			
			if (this.autoScanAlgorithm != null)
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
			}

			this.managedRobot.SetCollisionCheck(false);
		}
	}
	
	// -----------------

	@Override
	public void CalibratingRequested() {
		this.managedRobot.Calibrate(1000);
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
	public void TravelRouteRequested(Serialize.TravelRequest request) {
		// Abort if we are not in idle or travel mode, because this means
		// that the user wants to scan the map.
		if (this.currentState != ManagerState.Idle && this.currentState != ManagerState.TravelRoute)
		{
			this.remoteServer.SendTravelResponse(new TravelResponse(request.ID, false, request.TravelledRoute));
			
			return;
		}
		
		// Switch to travel mode
		this.currentState = ManagerState.TravelRoute;
		
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
        	/*
            // Convert relative coordinates to absolute coordinates (1 -> 50cm for example)
            List<Point> convertedToAbsolute = new ArrayList<Point>();
            
            for (Position pos : convertedToRelative)
            {
            	Point n = new Point((float)pos.Get_X() * cellStep, (float)pos.Get_Y() * cellStep);
            	
            	convertedToAbsolute.add(n);
            }

            this.CancelRoute();
            
            this.travelThread = new TravelThread(this.managedRobot, convertedToAbsolute);
            this.travelThread.start();*/
        	this.travelRequest = request;
        	
        	this.StartRoute(new Route(convertedToRelative), true);
            
            this.remoteServer.SendTravelResponse(new TravelResponse(request.ID, true, new Route(convertedToRelative)));
        }
	}

	@Override
	public void CancelRouteRequested() {
		if (this.currentState == ManagerState.TravelRoute)
		{
			this.CancelRoute();
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
        this.travelThread.start();
	}
	
	private void CancelRoute()
	{
        if (this.travelThread != null)
        {
			//this.managedRobot.Stop();
			
	    	try {
	            if (this.travelThread.IsRunning())
	            {
	            	this.travelThread.CancelRoute();
	            	this.travelThread.interrupt();
					this.travelThread.join();
	            }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
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
	public void DriveRobotRoute(Route route) {
		if (!this.autoScanAlgorithm.IsAborted())
		{
			try {
				this.StartRoute(route, false);
				
				// The algorithm has to wait until the route is finished.
				this.travelThread.join();
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
