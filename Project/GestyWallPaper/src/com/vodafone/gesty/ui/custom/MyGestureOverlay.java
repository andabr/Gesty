package com.vodafone.gesty.ui.custom;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.gesture.Gesture;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.gesture.GestureUtils;
import android.gesture.OrientedBoundingBox;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.vodafone.gesty.R;
import com.vodafone.gesty.PreferencesCache;

/**
 * A transparent overlay for gesture input that can be placed on top of other
 * widgets or contain other widgets.
 *
 * @attr ref android.R.styleable#GestureOverlayView_eventsInterceptionEnabled
 * @attr ref android.R.styleable#GestureOverlayView_fadeDuration
 * @attr ref android.R.styleable#GestureOverlayView_fadeOffset
 * @attr ref android.R.styleable#GestureOverlayView_fadeEnabled
 * @attr ref android.R.styleable#GestureOverlayView_gestureStrokeWidth
 * @attr ref android.R.styleable#GestureOverlayView_gestureStrokeAngleThreshold
 * @attr ref android.R.styleable#GestureOverlayView_gestureStrokeLengthThreshold
 * @attr ref android.R.styleable#GestureOverlayView_gestureStrokeSquarenessThreshold
 * @attr ref android.R.styleable#GestureOverlayView_gestureStrokeType
 * @attr ref android.R.styleable#GestureOverlayView_gestureColor
 * @attr ref android.R.styleable#GestureOverlayView_orientation
 * @attr ref android.R.styleable#GestureOverlayView_uncertainGestureColor
 */
public class MyGestureOverlay extends FrameLayout {
    public static final int GESTURE_STROKE_TYPE_SINGLE = 0;
    public static final int GESTURE_STROKE_TYPE_MULTIPLE = 1;

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;

    private static final int FADE_ANIMATION_RATE = 16;
    private static final boolean GESTURE_RENDERING_ANTIALIAS = true;
    private static final boolean DITHER_FLAG = true;

    private final Paint mGesturePaint = new Paint();

    private long mFadeDuration = 150;
    private long mFadeOffset = 420;
    private long mFadingStart;
    private boolean mFadingHasStarted;
    private boolean mFadeEnabled = true;

    private int mCurrentColor;
    private int mCertainGestureColor; //= 0xFFFFFF00;
    private int mUncertainGestureColor; // = 0x48FFFF00;
    private float mGestureStrokeWidth = 12.0f;
    private int mInvalidateExtraBorder = 10;

    private int mGestureStrokeType = GESTURE_STROKE_TYPE_MULTIPLE;
    private float mGestureStrokeLengthThreshold = 50.0f;
    private float mGestureStrokeSquarenessTreshold = 0.275f;
    private float mGestureStrokeAngleThreshold = 40.0f;

    private int mOrientation = ORIENTATION_VERTICAL;

    private final Rect mInvalidRect = new Rect();
    private final Path mPath = new Path();
    private boolean mGestureVisible = true;

    private float mX;
    private float mY;

    private float mCurveEndX;
    private float mCurveEndY;

    private float mTotalLength;
    private boolean mIsGesturing = false;
    private boolean mPreviousWasGesturing = false;
    private boolean mInterceptEvents = true;
    private boolean mIsListeningForGestures;
    private boolean mResetGesture;

    // current gesture
    private Gesture mCurrentGesture;
    private final ArrayList<GesturePoint> mStrokeBuffer = new ArrayList<GesturePoint>(100);

    private final ArrayList<OnGestureListener> mOnGestureListeners =
            new ArrayList<OnGestureListener>();
    private final ArrayList<OnGesturingListener> mOnGesturingListeners =
            new ArrayList<OnGesturingListener>();

    private boolean mHandleGestureActions;

    // fading out effect
    private boolean mIsFadingOut = false;
    private float mFadingAlpha = 1.0f;
    private final FadeOutRunnable mFadingOut = new FadeOutRunnable();

    public MyGestureOverlay(Context context) {
        super(context);
        init();
    }

    public MyGestureOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gestureOverlayViewStyle);
    }

    public MyGestureOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //overriding what the GestureOverlay super constructor has set
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GestureOverlayView, defStyle, 0);

        mGestureStrokeWidth = a.getFloat(R.styleable.GestureOverlayView_gestureStrokeWidth,
                mGestureStrokeWidth);
        mInvalidateExtraBorder = Math.max(1, ((int) mGestureStrokeWidth) - 1);
        mCertainGestureColor = PreferencesCache.getGestureColor(context);

        mUncertainGestureColor = PreferencesCache.getGestureColor(context);

        mFadeDuration = 1000;// the duration of the fading effect
        mFadeOffset = 420;//the time between gesture end and start of fading
        mGestureStrokeType = a.getInt(R.styleable.GestureOverlayView_gestureStrokeType,
                mGestureStrokeType);
        mGestureStrokeLengthThreshold = a.getFloat(
                R.styleable.GestureOverlayView_gestureStrokeLengthThreshold,
                mGestureStrokeLengthThreshold);
        mGestureStrokeAngleThreshold = a.getFloat(
                R.styleable.GestureOverlayView_gestureStrokeAngleThreshold,
                mGestureStrokeAngleThreshold);
        mGestureStrokeSquarenessTreshold = a.getFloat(
                R.styleable.GestureOverlayView_gestureStrokeSquarenessThreshold,
                mGestureStrokeSquarenessTreshold);
        mInterceptEvents = a.getBoolean(R.styleable.GestureOverlayView_eventsInterceptionEnabled,
                mInterceptEvents);
        mFadeEnabled = a.getBoolean(R.styleable.GestureOverlayView_fadeEnabled,
                mFadeEnabled);

        a.recycle();

        init();
    }

    private void init() {
        setWillNotDraw(false);

        final Paint gesturePaint = mGesturePaint;
        gesturePaint.setAntiAlias(GESTURE_RENDERING_ANTIALIAS);
        gesturePaint.setColor(mCertainGestureColor);
        gesturePaint.setStyle(Paint.Style.STROKE);
        gesturePaint.setStrokeJoin(Paint.Join.ROUND);
        gesturePaint.setStrokeCap(Paint.Cap.ROUND);
        gesturePaint.setStrokeWidth(mGestureStrokeWidth);
        gesturePaint.setDither(DITHER_FLAG);

        mCurrentColor = mCertainGestureColor;
        setPaintAlpha(255);
    }

    public ArrayList<GesturePoint> getCurrentStroke() {
        return mStrokeBuffer;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setGestureColor(int color) {
        mCertainGestureColor = color;
    }

    public void setUncertainGestureColor(int color) {
        mUncertainGestureColor = color;
    }

    public int getUncertainGestureColor() {
        return mUncertainGestureColor;
    }

    public int getGestureColor() {
        return mCertainGestureColor;
    }

    public float getGestureStrokeWidth() {
        return mGestureStrokeWidth;
    }

    public void setGestureStrokeWidth(float gestureStrokeWidth) {
        mGestureStrokeWidth = gestureStrokeWidth;
        mInvalidateExtraBorder = Math.max(1, ((int) gestureStrokeWidth) - 1);
        mGesturePaint.setStrokeWidth(gestureStrokeWidth);
    }

    public int getGestureStrokeType() {
        return mGestureStrokeType;
    }

    public void setGestureStrokeType(int gestureStrokeType) {
        mGestureStrokeType = gestureStrokeType;
    }

    public float getGestureStrokeLengthThreshold() {
        return mGestureStrokeLengthThreshold;
    }

    public void setGestureStrokeLengthThreshold(float gestureStrokeLengthThreshold) {
        mGestureStrokeLengthThreshold = gestureStrokeLengthThreshold;
    }

    public float getGestureStrokeSquarenessTreshold() {
        return mGestureStrokeSquarenessTreshold;
    }

    public void setGestureStrokeSquarenessTreshold(float gestureStrokeSquarenessTreshold) {
        mGestureStrokeSquarenessTreshold = gestureStrokeSquarenessTreshold;
    }

    public float getGestureStrokeAngleThreshold() {
        return mGestureStrokeAngleThreshold;
    }

    public void setGestureStrokeAngleThreshold(float gestureStrokeAngleThreshold) {
        mGestureStrokeAngleThreshold = gestureStrokeAngleThreshold;
    }

    public boolean isEventsInterceptionEnabled() {
        return mInterceptEvents;
    }

    public void setEventsInterceptionEnabled(boolean enabled) {
        mInterceptEvents = enabled;
    }

    public boolean isFadeEnabled() {
        return mFadeEnabled;
    }

    public void setFadeEnabled(boolean fadeEnabled) {
        mFadeEnabled = fadeEnabled;
    }

    public Gesture getGesture() {
        return mCurrentGesture;
    }

    public void setGesture(Gesture gesture) {
        if (mCurrentGesture != null) {
            clear(false);
        }

        setCurrentColor(mCertainGestureColor);
        mCurrentGesture = gesture;

        final Path path = mCurrentGesture.toPath();
        final RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        // TODO: The path should also be scaled to fit inside this view

        mPath.rewind();
        mPath.addPath(path, -bounds.left + (getWidth() - bounds.width()) / 2.0f,
                -bounds.top + (getHeight() - bounds.height()) / 2.0f);

        mResetGesture = true;

        invalidate();
    }

    public Path getGesturePath() {
        return mPath;
    }

    public Path getGesturePath(Path path) {
        path.set(mPath);
        return path;
    }

    public boolean isGestureVisible() {
        return mGestureVisible;
    }

    public void setGestureVisible(boolean visible) {
        mGestureVisible = visible;
    }

    public long getFadeOffset() {
        return mFadeOffset;
    }

    public void setFadeOffset(long fadeOffset) {
        mFadeOffset = fadeOffset;
    }

    public void addOnGestureListener(OnGestureListener listener) {
        mOnGestureListeners.add(listener);
    }

    public void removeOnGestureListener(OnGestureListener listener) {
        mOnGestureListeners.remove(listener);
    }

    public void removeAllOnGestureListeners() {
        mOnGestureListeners.clear();
    }

    public void addOnGesturingListener(OnGesturingListener listener) {
        mOnGesturingListeners.add(listener);
    }

    public void removeOnGesturingListener(OnGesturingListener listener) {
        mOnGesturingListeners.remove(listener);
    }

    public void removeAllOnGesturingListeners() {
        mOnGesturingListeners.clear();
    }

    public boolean isGesturing() {
        return mIsGesturing;
    }

    private void setCurrentColor(int color) {
        mCurrentColor = color;
        if (mFadingHasStarted) {
            setPaintAlpha((int) (255 * mFadingAlpha));
        } else {
            setPaintAlpha(255);
        }
        invalidate();
    }

    /**
     * @hide
     */
    public Paint getGesturePaint() {
        return mGesturePaint;
    }

    private int mPhase;
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        //set fading effect to the path if there's a listener and the gesturePerformed() was fired to listeners
        if (!mEffectPath.isEmpty() && mFadingOut.hasFired && mSyncListener != null) {
        	// make path effects for a finished gesture
            RectF bounds = new RectF();
            mEffectPath.computeBounds(bounds, false);
            PathEffect mEffect = new PathDashPathEffect(makePathDash(), 16, 
            		mPhase, PathDashPathEffect.Style.ROTATE);
            mPhase += 1;
            if (!mRecognized) { // show blur
	   	        MaskFilter mBlur = new BlurMaskFilter(mPhase, BlurMaskFilter.Blur.NORMAL);
	   	        getGesturePaint().setMaskFilter(mBlur);
            } 
            getGesturePaint().setPathEffect(mEffect);
            canvas.drawPath(mEffectPath, getGesturePaint());
        } else {
        
	        if (mCurrentGesture != null && mGestureVisible) {
	            canvas.drawPath(mPath, mGesturePaint);
	        }
        }
    }

    private void setPaintAlpha(int alpha) {
        alpha += alpha >> 7;
        final int baseAlpha = mCurrentColor >>> 24;
        final int useAlpha = baseAlpha * alpha >> 8;
        mGesturePaint.setColor((mCurrentColor << 8 >>> 8) | (useAlpha << 24));
    }

    public void clear(boolean animated) {
        clear(animated, false, true);
    }
    
    private Path mEffectPath = new Path();


    private void clear(boolean animated, boolean fireActionPerformed, boolean immediate) {
        setPaintAlpha(255);
        removeCallbacks(mFadingOut);
        mResetGesture = false;
        
        mFadingOut.resetMultipleStrokes = false;
        // reset the animation to be able to start a new one
        mFadingOut.hasFired = false;
        mEffectPath.reset();
        mEffectPath.addPath(mPath);
        
        if (animated && mCurrentGesture != null) {
            mFadingAlpha = 1.0f;
            mIsFadingOut = true;
            mFadingHasStarted = false;
            mFadingStart = AnimationUtils.currentAnimationTimeMillis() + mFadeOffset;
            postDelayed(mFadingOut, mFadeOffset);
        } else {
            mFadingAlpha = 1.0f;
            mIsFadingOut = false;
            mFadingHasStarted = false;

            if (immediate) {
                mCurrentGesture = null;
                Log.e("MyGestureOverlay", "clear immediate rewind!!!!");

                mPath.rewind();
                invalidate();
            } else if (fireActionPerformed) {
                postDelayed(mFadingOut, mFadeOffset);
            } else if (mGestureStrokeType == GESTURE_STROKE_TYPE_MULTIPLE) {
                mFadingOut.resetMultipleStrokes = true;
                postDelayed(mFadingOut, mFadeOffset);
            } else {
                mCurrentGesture = null;
                mPath.rewind();
                invalidate();
            }
        }
    }

    public void cancelClearAnimation() {
        setPaintAlpha(255);
        mIsFadingOut = false;
        mFadingHasStarted = false;
        removeCallbacks(mFadingOut);
        mPath.rewind();
        mCurrentGesture = null;
    }

    public void cancelGesture() {
        mIsListeningForGestures = false;

        // add the stroke to the current gesture
        mCurrentGesture.addStroke(new GestureStroke(mStrokeBuffer));

        // pass the event to handlers
        final long now = SystemClock.uptimeMillis();
        final MotionEvent event = MotionEvent.obtain(now, now,
                MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);

        final ArrayList<OnGestureListener> listeners = mOnGestureListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureCancelled(this, event);
        }

        event.recycle();

        clear(false);
        mIsGesturing = false;
        mPreviousWasGesturing = false;
        mStrokeBuffer.clear();

        final ArrayList<OnGesturingListener> otherListeners = mOnGesturingListeners;
        count = otherListeners.size();
        for (int i = 0; i < count; i++) {
            otherListeners.get(i).onGesturingEnded(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelClearAnimation();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            final boolean cancelDispatch = (mIsGesturing || (mCurrentGesture != null &&
                    mCurrentGesture.getStrokesCount() > 0 && mPreviousWasGesturing)) &&
                    mInterceptEvents;

            processEvent(event);

            if (cancelDispatch) {
                event.setAction(MotionEvent.ACTION_CANCEL);
            }

            super.dispatchTouchEvent(event);

            return true;
        }

        return super.dispatchTouchEvent(event);
    }

    private boolean processEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mIsListeningForGestures) {
                    Rect rect = touchMove(event);
                    if (rect != null) {
                        invalidate(rect);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsListeningForGestures) {
                    touchUp(event, false);
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsListeningForGestures) {
                    touchUp(event, true);
                    invalidate();
                    return true;
                }
        }

        return false;
    }
    private void touchDown(MotionEvent event) {
        mIsListeningForGestures = true;
        float x = event.getX();
        float y = event.getY();

        mX = x;
        mY = y;

        mTotalLength = 0;
        mIsGesturing = false;
        // make sure to cancel animation quickly!!!
        if (mEffectPath != null) {
            mEffectPath.reset();
            mPhase = 0;
			getGesturePaint().setPathEffect(null);
        	getGesturePaint().setMaskFilter(null);
        	Log.e("MyGestureOverlay", "reset effect path!!");
        }

        if (mGestureStrokeType == GESTURE_STROKE_TYPE_SINGLE || mResetGesture) {
            if (mHandleGestureActions) {
            	setCurrentColor(mUncertainGestureColor);
            }
            mResetGesture = false;
            mCurrentGesture = null;

            mPath.rewind();
        } else if (mCurrentGesture == null || mCurrentGesture.getStrokesCount() == 0) {
            if (mHandleGestureActions) {
            	setCurrentColor(mUncertainGestureColor);
            }
        }

        // if there is fading out going on, stop it.
        if (mFadingHasStarted) {
            cancelClearAnimation();
        } else if (mIsFadingOut) {
            setPaintAlpha(255);
            mIsFadingOut = false;
            mFadingHasStarted = false;
            removeCallbacks(mFadingOut);
        }

        if (mCurrentGesture == null) {
            mCurrentGesture = new Gesture();
        }

        mStrokeBuffer.add(new GesturePoint(x, y, event.getEventTime()));
        mPath.moveTo(x, y);

        final int border = mInvalidateExtraBorder;
        mInvalidRect.set((int) x - border, (int) y - border, (int) x + border, (int) y + border);

        mCurveEndX = x;
        mCurveEndY = y;

        // pass the event to handlers
        final ArrayList<OnGestureListener> listeners = mOnGestureListeners;
        final int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureStarted(this, event);
        }
    }

    private static final float TOUCH_TOLERANCE = 3;

    private Rect touchMove(MotionEvent event) {
        Rect areaToRefresh = null;

        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            areaToRefresh = mInvalidRect;

            // start with the curve end
            final int border = mInvalidateExtraBorder;
            areaToRefresh.set((int) mCurveEndX - border, (int) mCurveEndY - border,
                    (int) mCurveEndX + border, (int) mCurveEndY + border);

            float cX = mCurveEndX = (x + previousX) / 2;
            float cY = mCurveEndY = (y + previousY) / 2;

            mPath.quadTo(previousX, previousY, cX, cY);

            // union with the control point of the new curve
            areaToRefresh.union((int) previousX - border, (int) previousY - border,
                    (int) previousX + border, (int) previousY + border);

            // union with the end point of the new curve
            areaToRefresh.union((int) cX - border, (int) cY - border,
                    (int) cX + border, (int) cY + border);

            mX = x;
            mY = y;

            mStrokeBuffer.add(new GesturePoint(x, y, event.getEventTime()));

            if (mHandleGestureActions && !mIsGesturing) {
                mTotalLength += (float) Math.sqrt(dx * dx + dy * dy);

                if (mTotalLength > mGestureStrokeLengthThreshold) {
                    final OrientedBoundingBox box =
                            GestureUtils.computeOrientedBoundingBox(mStrokeBuffer);

                    float angle = Math.abs(box.orientation);
                    if (angle > 90) {
                        angle = 180 - angle;
                    }

                    if (box.squareness > mGestureStrokeSquarenessTreshold ||
                            (mOrientation == ORIENTATION_VERTICAL ?
                                    angle < mGestureStrokeAngleThreshold :
                                    angle > mGestureStrokeAngleThreshold)) {

                        mIsGesturing = true;
                        setCurrentColor(mCertainGestureColor);

                        final ArrayList<OnGesturingListener> listeners = mOnGesturingListeners;
                        int count = listeners.size();
                        for (int i = 0; i < count; i++) {
                            listeners.get(i).onGesturingStarted(this);
                        }
                    }
                }
            }

            // pass the event to handlers
            final ArrayList<OnGestureListener> listeners = mOnGestureListeners;
            final int count = listeners.size();
            for (int i = 0; i < count; i++) {
                listeners.get(i).onGesture(this, event);
            }
        }

        return areaToRefresh;
    }

    private void touchUp(MotionEvent event, boolean cancel) {
        mIsListeningForGestures = false;

        // A gesture wasn't started or was cancelled
        if (mCurrentGesture != null) {
            // add the stroke to the current gesture
            mCurrentGesture.addStroke(new GestureStroke(mStrokeBuffer));

            if (!cancel) {
                // pass the event to handlers
                final ArrayList<OnGestureListener> listeners = mOnGestureListeners;
                int count = listeners.size();
                for (int i = 0; i < count; i++) {
                    listeners.get(i).onGestureEnded(this, event);
                }
                clear(mHandleGestureActions && mFadeEnabled, mHandleGestureActions && mIsGesturing,
                        false);
            } else {
                cancelGesture(event);

            }
        } else {
            cancelGesture(event);
        }

        mStrokeBuffer.clear();
        mPreviousWasGesturing = mIsGesturing;
        mIsGesturing = false;

        final ArrayList<OnGesturingListener> listeners = mOnGesturingListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGesturingEnded(this);
        }
    }

    private void cancelGesture(MotionEvent event) {
        // pass the event to handlers
        final ArrayList<OnGestureListener> listeners = mOnGestureListeners;
        final int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureCancelled(this, event);
        }

        clear(false);
    }

    private class FadeOutRunnable implements Runnable {
        boolean resetMultipleStrokes;
        volatile boolean hasFired;

        public void run() {

            if (mIsFadingOut) {
                if (!hasFired) {
                	fireGesturePerformedStartedSynchronously();
                	hasFired = true;
                }
                final long now = AnimationUtils.currentAnimationTimeMillis();
                final long duration = now - mFadingStart;

                if (duration > mFadeDuration) {
                    fireGesturePerformedFinishedSynchronously();
                    if (mEffectPath != null) {
                        mEffectPath.reset();
                        mPhase = 0;
        				getGesturePaint().setPathEffect(null);
        	        	getGesturePaint().setMaskFilter(null);
        	        	Log.e("MyGestureOverlay", "reset effect path!!");
                    }
                    
                    mPreviousWasGesturing = false;
                    mIsFadingOut = false;
                    mFadingHasStarted = false;

                    mPath.rewind();
                    mCurrentGesture = null;
                    setPaintAlpha(255);
                    
                } else {
                    mFadingHasStarted = true;
                    postDelayed(this, FADE_ANIMATION_RATE);
                }
            } else if (resetMultipleStrokes) {
                mResetGesture = true;
            } else {
            	fireGesturePerformedStartedSynchronously();
            	fireGesturePerformedFinishedSynchronously();

                mFadingHasStarted = false;

                mPath.rewind();
                mCurrentGesture = null;
                mPreviousWasGesturing = false;
                setPaintAlpha(255);
            }

            invalidate();
        }
    }

    private Path makePathDash() {
        Path p = new Path();
        p.moveTo(6, 0);
        p.lineTo(0, -6);
        p.lineTo(8, -6);
        p.lineTo(12, 0);
        p.lineTo(8, 6);
        p.lineTo(0, 6);
        return p;
    }

    public static interface OnGesturingListener {
        void onGesturingStarted(MyGestureOverlay overlay);

        void onGesturingEnded(MyGestureOverlay overlay);
    }

    public static interface OnGestureListener {
        void onGestureStarted(MyGestureOverlay overlay, MotionEvent event);

        void onGesture(MyGestureOverlay overlay, MotionEvent event);

        void onGestureEnded(MyGestureOverlay overlay, MotionEvent event);

        void onGestureCancelled(MyGestureOverlay overlay, MotionEvent event);
    }


    private volatile boolean mRecognized;
	public void setRecognized(boolean recognized) {
		mRecognized = recognized;	
	}
	
	private SynchronousGestureListener mSyncListener;
	public void setSynchronousGesturePerformedListener(SynchronousGestureListener listener) {
		
		mSyncListener = listener;
        mHandleGestureActions = (mSyncListener != null);

	}
	
	private void fireGesturePerformedStartedSynchronously() {
		if (mSyncListener != null) {
			mSyncListener.onGesturePerformedStarted(this, mCurrentGesture);
		}
	}
	
	private void fireGesturePerformedFinishedSynchronously() {
		if (mSyncListener != null) {
			mSyncListener.onGesturePerformedFinished(this, mCurrentGesture);
		}
	}
	
}
