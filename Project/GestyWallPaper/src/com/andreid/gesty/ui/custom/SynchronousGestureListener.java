package com.andreid.gesty.ui.custom;

import android.gesture.Gesture;

public interface SynchronousGestureListener {

	void onGesturePerformed(MyGestureOverlay overlay, Gesture gesture);
	
	void onGesturePerformed2(MyGestureOverlay overlay, Gesture gesture);

}
