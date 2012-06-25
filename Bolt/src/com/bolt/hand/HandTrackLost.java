package com.bolt.hand;

import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.InactiveHandEventArgs;
import com.bolt.core.HandTrackerThread;

public class HandTrackLost implements IObserver<InactiveHandEventArgs> {
	HandTrackerThread handTrackerThread;
	public HandTrackLost(HandTrackerThread htt)
	{
		handTrackerThread=htt;
	}
	public void update(IObservable<InactiveHandEventArgs> observable,InactiveHandEventArgs args){
		handTrackerThread.hands.remove(args.getId());
		handTrackerThread.inputProcessor.HandTrackLost(args.getId());
	}
}
