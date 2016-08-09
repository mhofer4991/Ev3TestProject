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
import interfaces.RemoteControlListener;
import interfaces.RobotStatusListener;
import lejos.robotics.geometry.Point;
import Serialize.TravelRequest;
import Serialize.TravelResponse;
import autoScan.ScanMap;
import network.RemoteControlServer;
import pathfinding.IPath;
import pathfinding.PathAlgorithm;
import pathfinding.PathIO;
import robot.Robot;

public class Manager implements RemoteControlListener, RobotStatusListener {
	private Robot managedRobot;
	
	private RemoteControlServer remoteServer;
	
	private ScanMap scannedMap;
	
	private Point lastRobotPosition;
	
	private float cellStep = 0.5F;
	
	// Travelling
	private TravelThread travelThread;
	
	private TravelRequest travelRequest;
	
	public Manager(Robot managedRobot)
	{
		this.managedRobot = managedRobot;
		this.managedRobot.AddListener(this);
		this.lastRobotPosition = this.managedRobot.GetPosition();
		
		this.remoteServer = new RemoteControlServer(this);
		
		this.scannedMap = new ScanMap();
	}
	
	public void Start()
	{
		this.remoteServer.start();
	}
	
	public void Stop()
	{
		this.remoteServer.Stop();
	}
	
	//
	// Robo status listener
	//

	@Override
	public void RobotStatusUpdated(RoboStatus status) {
		// TODO:
		// check if manual or automatic
		// if manual calculate start end and add it to scanned map
		// if automatic notify automatic scan algorithm
		this.remoteServer.SendRoboStatus(managedRobot.GetStatus());
		
		Position start = new Position((int)(Math.round(this.lastRobotPosition.x / 0.5F)), (int)(Math.round(this.lastRobotPosition.y / 0.5F)));
		Position end = new Position((int)(Math.round(status.X / 0.5F)), (int)(Math.round(status.Y / 0.5F)));
		
		if (Math.sqrt(Math.pow(start.Get_X() - end.Get_X(), 2) + Math.pow(start.Get_Y() - end.Get_Y(), 2)) > 0)
		{
			System.out.println(status.X + " - " + status.Y);
			System.out.println(start.Get_X() + " - " + start.Get_Y());
			System.out.println(end.Get_X() + " - " + end.Get_Y());
			
			this.scannedMap.AddScanResult(start, end, Fieldstate.freeScanned);
			
			this.remoteServer.SendMapUpdate(scannedMap.map);
		}
		
		this.lastRobotPosition = new Point(status.X, status.Y);
	}
	
	//
	// Remote listener
	//

	@Override
	public void ConnectedToRemote() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DisconnectedFromRemote() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ManualScanModeStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ManualScanModeExited() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void AutomaticScanModeStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void AutomaticScanModeExited() {
		// TODO Auto-generated method stub
		
	}

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
		this.managedRobot.TurnLeftByDegrees(degrees);
	}

	@Override
	public void TurnRobotRight() {
		this.managedRobot.TurnRight();
	}

	@Override
	public void TurnRobotRight(float degrees) {
		this.managedRobot.TurnRightByDegrees(degrees);
	}

	@Override
	public void StopRobot() {
		this.managedRobot.Stop();
	}

	@Override
	public void ScanArea() {
		float distance = -1;
		float rotation;
		Position start;
		
		if (!this.managedRobot.IsMoving())
		{
			start = new Position((int)(Math.round(this.lastRobotPosition.x / 0.5F)), (int)(Math.round(this.lastRobotPosition.y / 0.5F)));
			
			// First
			rotation = this.managedRobot.GetRotation();
			distance = managedRobot.ScanDistance() / cellStep;
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Second
			managedRobot.TurnRightByDegrees(90);
			rotation = this.managedRobot.GetRotation();
			distance = managedRobot.ScanDistance() / cellStep;
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Third
			managedRobot.TurnRightByDegrees(90);
			rotation = this.managedRobot.GetRotation();
			distance = managedRobot.ScanDistance() / cellStep;
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Fourth
			managedRobot.TurnRightByDegrees(90);
			rotation = this.managedRobot.GetRotation();
			distance = managedRobot.ScanDistance() / cellStep;
			
			if (distance > 0)
			{
				this.scannedMap.AddScanResult(rotation, distance, start, Fieldstate.freeScanned);
			}
			
			// Return to old rotation
			managedRobot.TurnRightByDegrees(90);
			
			// Send the result to the remote server
			this.remoteServer.SendMapUpdate(scannedMap.map);
		}	
	}

	@Override
	public void TravelRouteRequested(Serialize.TravelRequest request) {
		System.out.println(request.TravelledRoute.Get_Route().size());
		System.out.println("> " + request.TravelledMap.Get_Fields()[0][0].Get_State().ordinal());
		System.out.println("> " + request.TravelledMap.Get_Fields()[1][1].Get_State().ordinal());

		// Convert from relative coordinates to array indices 
		// to make them ready for the a star algorithm
		List<Position> convertedToIndex = request.TravelledMap.ConvertFromRelativeToArrayPositions(request.TravelledRoute.Get_Route());
		
        IPath path = PathAlgorithm.A_Star();
        List<Position> calc = PathIO.CalculatePath(request.TravelledMap, new Route(convertedToIndex), path);
        
        // Convert them back to relative coordinates
        List<Position> convertedToRelative = request.TravelledMap.ConvertFromArrayToRelativePositions(calc);
        
        if (convertedToRelative.isEmpty())
        {
        	this.remoteServer.SendTravelResponse(new TravelResponse(request.ID, false, new Route(convertedToRelative)));
        }
        else
        {
            // Convert relative coordinates to absolute coordinates (1 -> 50cm for example)
            List<Point> convertedToAbsolute = new ArrayList<Point>();
            
            for (Position pos : convertedToRelative)
            {
            	Point n = new Point((float)pos.Get_X() * cellStep, (float)pos.Get_Y() * cellStep);
            	
            	convertedToAbsolute.add(n);
            }

            this.CancelRoute();
            
            this.travelThread = new TravelThread(this.managedRobot, convertedToAbsolute);
            this.travelThread.start();
            
            this.remoteServer.SendTravelResponse(new TravelResponse(request.ID, true, new Route(convertedToRelative)));
        }
	}

	@Override
	public void CancelRouteRequested() {
		this.CancelRoute();
	}
	
	private void CancelRoute()
	{
        if (this.travelThread != null)
        {
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
}
