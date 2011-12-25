package com.vodafone.gesty.ui.custom;

import android.gesture.Gesture;

public interface SynchronousGestureListener {

	void onGesturePerformedStarted(MyGestureOverlay overlay, Gesture gesture);
	
	void onGesturePerformedFinished(MyGestureOverlay overlay, Gesture gesture);

}
