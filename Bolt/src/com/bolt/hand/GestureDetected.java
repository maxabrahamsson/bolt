package com.bolt.hand;

import com.bolt.core.HandTrackerThread;
import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.StatusException;

public class GestureDetected implements IObserver<GestureRecognizedEventArgs> {
	HandTrackerThread handTrackerThread;
	public GestureDetected(HandTrackerThread htt)
	{
		handTrackerThread=htt;
	}
	@Override
	public void update(IObservable<GestureRecognizedEventArgs> observable,GestureRecognizedEventArgs args)
	{
		try {
			handTrackerThread.handsGen.StartTracking(args.getEndPosition());
			handTrackerThread.inputProcessor.HandGestureDetected();
		} 
		catch (StatusException e) {e.printStackTrace();}
	}
}
