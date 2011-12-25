package com.vodafone.gesty.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import com.vodafone.gesty.PreferencesCache;

public class GestureSnapshotLoader {

	private GestureLibrary mStore;
	private Handler mHandler;
    private int mThumbnailSize;
    private int mThumbnailInset;
    private int mPathColor;
    private HashMap<String, Drawable> mGestureSnapshots;
    private WorkerThread mThread;
    private Context mContext;
    // a delay to avoid redundant notification to the handler 
    private final static int DELAY_TO_NOTIFY = 500;
	
	public GestureSnapshotLoader(Context context, GestureLibrary store, Handler handler) {
		super();
		mContext = context;
		mStore = store;
		mHandler = handler;
		mThread = new WorkerThread();
		mThread.start();
        mGestureSnapshots = new HashMap<String, Drawable>();
        readColors();
	}
	
	private void readColors() {
        mPathColor = PreferencesCache.getGestureColor(mContext);
        mThumbnailInset = PreferencesCache.getGestureInset(mContext);
        mThumbnailSize = PreferencesCache.getGestureSize(mContext);

	}
	
	/**
     * Custom implementation of WorkerThread.
     */
    class WorkerThread extends Thread {
        private static final int MAX_QUEUED_ITEMS = 50;
        private BlockingQueue<String> mRequestQueue;
        private boolean running = true;
          
        /**
         * Constructor.
         */
        public WorkerThread() {
            mRequestQueue =
                new ArrayBlockingQueue<String>(MAX_QUEUED_ITEMS);
        }
      
        /**
         * This method add a new UiQueueItem in the requests queue.
         * @param uiRequest UiQueueItem - ui request.
         */
        public void put(String uiRequest) {
            try {
                mRequestQueue.put(uiRequest);
            } catch (InterruptedException iex) {
                Log.i("GestureSnapshotLoader.WorkerThread", "put(): Thread interrupted");
                Thread.currentThread().interrupt();
            }
        }
          
        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        public void run() {
            try {
                while (running) {
                	loadGesture(mRequestQueue.take());
                }
            } catch (InterruptedException iex) {
                Log.i("GestureSnapshotLoader.WorkerThread", "run(): Thread interrupted");
            }
        }
        
        /**
         * This method stops the worker thread.
         */
        public void kill() {
            running = false;
            Thread.currentThread().interrupt();
            mRequestQueue.clear();
            mRequestQueue = null;
        }
    }
    
	private void loadGesture(String gestureId) {
        ArrayList<Gesture> gestures = mStore.getGestures(gestureId);
        
        if (gestures != null) {
        	for (Gesture gesture : gestures) {
                final Bitmap bitmap = gesture.toBitmap(mThumbnailSize, mThumbnailSize,
                       mThumbnailInset, mPathColor);
                mGestureSnapshots.put(gestureId, new BitmapDrawable(bitmap));
                
                break;
            }
        	mHandler.removeMessages(0);
            mHandler.sendEmptyMessageDelayed(0, DELAY_TO_NOTIFY);
        }
	}
	
	public Drawable getSnapshotForGestureId(String gestureId) {
		Drawable snapshot = mGestureSnapshots.get(gestureId);
		if (snapshot == null) {
			mThread.put(gestureId);
		}
		return snapshot;
	}

	public void destroy() {
		if (mThread != null) {
			mThread.kill();
			mThread = null;
		}
		mGestureSnapshots.clear();
		mStore = null;
		mHandler = null;
		mContext = null;
		
	}
	
	public void clearCache() {
		mGestureSnapshots.clear();
		readColors();
	}
}
