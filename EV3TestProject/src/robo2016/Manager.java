package robo2016;

import Serialize.RoboStatus;
import calibrating.CalibratingUtil;
import interfaces.RemoteControlListener;
import interfaces.RobotStatusListener;
import network.RemoteControlServer;
import robot.Robot;

public class Manager implements RemoteControlListener, RobotStatusListener {
	private Robot managedRobot;
	
	private RemoteControlServer remoteServer;
	
	public Manager(Robot managedRobot)
	{
		this.managedRobot = managedRobot;
		this.managedRobot.AddListener(this);
		
		this.remoteServer = new RemoteControlServer(this);
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
		//this.remoteServer.SendRoboStatus(managedRobot.GetStatus());
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
	public void TurnRobotRight() {
		this.managedRobot.TurnRight();
	}

	@Override
	public void StopRobot() {
		this.managedRobot.Stop();
	}
}
