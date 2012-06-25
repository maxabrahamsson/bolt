package com.bolt.hand;

import org.OpenNI.ActiveHandEventArgs;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;

import com.bolt.core.HandTrackerThread;

public class HandTrackStart implements IObserver<ActiveHandEventArgs> {
	HandTrackerThread handTrackerThread;
	public HandTrackStart(HandTrackerThread htt)
	{
		handTrackerThread=htt;
	}
	public void update(IObservable<ActiveHandEventArgs> observable,ActiveHandEventArgs args){
		handTrackerThread.hands.put(args.getId(), args.getPosition());
		handTrackerThread.inputProcessor.HandTrackStarted(args.getId());
	}
}
