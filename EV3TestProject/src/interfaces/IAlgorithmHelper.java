package interfaces;

import Serialize.*;
import autoScan.ScanMap;

public interface IAlgorithmHelper {
	void RotateRobotTo(float degrees);
	
	void DriveRobotTo(Position position);
	
	void DriveRobotRoute(Route route);
	
	int MeasureDistance();
	
	void UpdateScanMap(ScanMap map);
}
