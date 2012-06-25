package com.bolt.core;
import java.util.ArrayList;

import org.OpenNI.Point3D;
import org.OpenNI.StatusException;


public abstract class InputProcessor {
	Thread checkThread;
	HandTrackerThread handtrack=null;
	public void startListen(String StartGesture,Boolean showWindow) {
		handtrack=new HandTrackerThread(this,StartGesture,showWindow);
		checkThread=new Thread(handtrack);
		checkThread.start();
	}
	public Point3D getTracker(Point3D pos)
	{
    	try {
			return handtrack.depthGen.convertRealWorldToProjective(pos);
		} catch (StatusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
	}
	public abstract void HandMove(Point3D position,int HandId);
	public abstract void HandTrackStarted(int HandId);
	public abstract void HandTrackLost(int HandId);
	public abstract void HandGestureDetected();
}
