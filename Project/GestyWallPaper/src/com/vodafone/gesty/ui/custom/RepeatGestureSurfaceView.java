package com.vodafone.gesty.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.vodafone.gesty.R;
import com.vodafone.gesty.PreferencesCache;

public class RepeatGestureSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	 
	 private MySurfaceThread thread;
	 private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	 private float[] mPoints;
	 
	 private int mLen;
	 private volatile int mCounter;
	 private Bitmap mBackground;
	 private boolean mRunning = true;

	 public RepeatGestureSurfaceView(Context context) {
	     super(context);
	     init(context);
	 }

	 public RepeatGestureSurfaceView(Context context, AttributeSet attrs) {
	     super(context, attrs);
	     init(context);
	 }

	 public RepeatGestureSurfaceView(Context context, AttributeSet attrs, int defStyle) {
	     super(context, attrs, defStyle);
	     init(context);
	 }

	 private void init(Context context){
	     getHolder().addCallback(this);
	     thread = new MySurfaceThread(getHolder(), this);
	  
	     mPaint.setStyle(Paint.Style.STROKE);
         mPaint.setStrokeJoin(Paint.Join.ROUND);
         mPaint.setStrokeCap(Paint.Cap.ROUND);

	     mPaint.setStrokeWidth(10);
	     mPaint.setColor(PreferencesCache.getGestureColor(context));

         Drawable my = context.getResources().getDrawable(R.drawable.drawing_area);
         mBackground = ((BitmapDrawable)my).getBitmap();
         
	 }
	 
	 @Override
	 public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	 }

	 @Override
	 public void surfaceCreated(SurfaceHolder holder) {
	     thread.start();
	 }

	 @Override
	 public void surfaceDestroyed(SurfaceHolder holder) {
	     boolean retry = true;
	     while (retry) {
	         try {
	        	 thread.join();
	        	 retry = false;
	         } catch (InterruptedException e) {
	         }
	     }
	 }
	 
	 
	 public void setData(float[] p) {
		 mLen = p.length;
		 mPoints = new float[mLen];
		 for (int i = 0; i < mLen; i++) {
		     mPoints[i] = p[i];
		 }
		 mCounter = 0;
	 }
	 
	 synchronized public void stop() {
		 mRunning = false;
	 }
	 
	 @Override
	 protected void onDraw(Canvas canvas) {
		 super.onDraw(canvas);
    	 if (mPoints != null && mCounter < mLen) {
			 mCounter +=2;
			 if (mCounter < mLen) {
				 
//				 canvas.drawColor(0xFFDDDDDD);
				 canvas.drawColor(0xFFFFFFFF);

			     canvas.drawBitmap(mBackground, 0, 0, mPaint);
				 for (int i = 0; i < mCounter;) {
					 if (i >= mCounter) {
						 return;
					 }
					 float x = mPoints[i];
					 i++;
					 if (x == -100) {
						 continue;
					 }
					 if (i >= mCounter) {
						 return;
					 }
					 float y = mPoints[i];
					 i++;
					 
					 if (i >= mCounter) {
						 return;
					 }
					 float x2 = mPoints[i];
					 i++;
					 if (x2 == -100) {
						 continue;
					 }
					 if (i >= mCounter) {
						 return;
					 }
					 float y2 = mPoints[i];
					 i--;
		    		 canvas.drawLine(x, y, x2, y2, mPaint);
//					 Log.e(" gesture.draw!!",mCounter+ " point ("+x + "," +y+") to (" + x2 + "," +y2+ ")" + " , of "+mLen);
				 }
			 }
			 
		 } 
 	 }
	 
	 public class MySurfaceThread extends Thread {
	     private SurfaceHolder myThreadSurfaceHolder;
	     private RepeatGestureSurfaceView myThreadSurfaceView;
	    	 
	     public MySurfaceThread(SurfaceHolder surfaceHolder, RepeatGestureSurfaceView surfaceView) {
	    	 myThreadSurfaceHolder = surfaceHolder;
	    	 myThreadSurfaceView = surfaceView;
	     }

	    	 @Override
	     public void run() {
	    	while (mCounter < mLen && mRunning) {
		    	Canvas c = null;
		    	try {
		    	    c = myThreadSurfaceHolder.lockCanvas(null);
		    	    synchronized (myThreadSurfaceHolder){
		    	       myThreadSurfaceView.onDraw(c);
		    	    }
		    	    sleep(10);
		    	} catch (InterruptedException e) {
		    	    e.printStackTrace();
		    	} finally{
		    	    // do this in a finally so that if an exception is thrown
		    	    // during the above, we don't leave the Surface in an
		    	    // inconsistent state
		    	    if (c != null) {
	     	    	     myThreadSurfaceHolder.unlockCanvasAndPost(c);
		    	    }
		   	    }
	    	}
	    }
    }
}

