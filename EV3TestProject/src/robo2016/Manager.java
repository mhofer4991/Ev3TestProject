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
		this.remoteServer.SendRoboStatus(managedRobot.GetStatus());
		
		Position start = new Position((int)(Math.round(this.lastRobotPosition.x / 0.5F)), (int)(Math.round(this.lastRobotPosition.y / 0.5F)));
		Position end = new Position((int)(Math.round(status.X / 0.5F)), (int)(Math.round(status.Y / 0.5F)));
		
		System.out.println(status.X + " - " + status.Y);
		System.out.println(start.Get_X() + " - " + start.Get_Y());
		System.out.println(end.Get_X() + " - " + end.Get_Y());
		
		this.scannedMap.AddScanResult(start, end, Fieldstate.freeScanned);
		
		this.remoteServer.SendMapUpdate(scannedMap.map);
		
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
	public void TravelRouteRequested(Serialize.TravelRequest request) {
		System.out.println(request.TravelledRoute.Get_Route().size());
		System.out.println("> " + request.TravelledMap.Get_Fields()[0][0].Get_State().ordinal());
		System.out.println("> " + request.TravelledMap.Get_Fields()[1][1].Get_State().ordinal());

		// Convert from relative coordinates to array indices 
		// to make them ready for the a star algorithm
		List<Position> converted = request.TravelledMap.ConvertFromRelativeToArrayPositions(request.TravelledRoute.Get_Route());
		
        IPath path = PathAlgorithm.A_Star();
        List<Position> calc = PathIO.CalculatePath(request.TravelledMap, new Route(converted), path);
        
        // Convert them back to relative coordinates
        List<Position> bconverted = request.TravelledMap.ConvertFromArrayToRelativePositions(calc);
        
        this.remoteServer.SendTravelResponse(request.ID, new Route(bconverted));
        
        if (!bconverted.isEmpty())
        {
            while (true)
            {
                for (Position pos : bconverted)
                {
                	Point n = new Point((float)pos.Get_X() * 0.5F, (float)pos.Get_Y() * 0.5F);
                	this.managedRobot.DriveToPosition(n);
                }
            }
        }
	}
}
