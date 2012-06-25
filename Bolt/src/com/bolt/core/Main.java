package com.bolt.core;
import org.OpenNI.Point3D;;

public class Main extends InputProcessor {
	static Main m;
	public static void main(String[] args) {
		m=new Main();
		m.startListen("RaiseHand",true);
	}

	@Override
	public void HandMove(Point3D position, int HandId) {
		System.out.println(m.getTracker(position).getX());	
	}

	@Override
	public void HandTrackStarted(int HandId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void HandTrackLost(int HandId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void HandGestureDetected() {
		// TODO Auto-generated method stub
		
	}
}
