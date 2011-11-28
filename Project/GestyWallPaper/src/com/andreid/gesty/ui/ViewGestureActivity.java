
package com.andreid.gesty.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureStroke;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.andreid.gesty.GestyApp;
import com.andreid.gesty.R;
import com.andreid.gesty.data.GestureDetail;
import com.andreid.gesty.data.IntentsData;
import com.andreid.gesty.ui.custom.RepeatGestureSurfaceView;

public class ViewGestureActivity extends Activity {
 
    private long mContactDetailId;
    private long mContactId;
    private String mPackageName;
    private String mClassName;
    private String mContactDetailValue;
    private String mUrl;
    private int mActionId;
    private String mOldGestureId;
    private static final int DELETE_DIALOG = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

        setContentView(R.layout.view_gesture);
        
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        if (data != null) {
        	mContactId = data.getLong(IntentsData.CONTACT_ID);
            mActionId = data.getInt(IntentsData.ACTION_ID);
            
            mContactDetailId = data.getLong(IntentsData.CONTACT_DETAIL_ID);
            mOldGestureId = data.getString(IntentsData.GESTURE_ID);
            mContactDetailValue = data.getString(IntentsData.CONTACT_DETAIL_VALUE);
            
            mPackageName = data.getString(IntentsData.PACKAGE_ID);
            mClassName = data.getString(IntentsData.CLASS_ID);
            mUrl = data.getString(IntentsData.URL_ID);
            
        	TextView gestureName = (TextView)findViewById(R.id.view_gesture_name);
            gestureName.setText(mContactDetailValue);
            
            getGesture();
        }
    }
    
    private void getGesture() {
    	if (!TextUtils.isEmpty(mOldGestureId)) {
    		GestureLibrary lib = ((GestyApp)getApplication()).getGesturesLibrary();
            ArrayList<Gesture> gestures = lib.getGestures(mOldGestureId);
            if (gestures != null && !gestures.isEmpty()) {
                Gesture gesture = gestures.get(0);
                int count = 0;
	        	for (GestureStroke stroke: gesture.getStrokes()) {
	        		count += stroke.points.length;
	        	}
	        	if (gesture.getStrokesCount() > 0) {
	        		count += gesture.getStrokesCount() - 1;
	        	}
	        	float[] gesturePoints = new float[count];
	        	int index = 0;
	        	for (GestureStroke stroke: gesture.getStrokes()) {
	        		float[] points = stroke.points;
	        		for (int i = 0, len = points.length ; i < len; i++, index++) {
	        			gesturePoints[index] = points[i];
	        		}
	        		if (index < count) {
		       			gesturePoints[index] = -100;
		       			index++;
	        		}
	        	}
	        	RepeatGestureSurfaceView view = (RepeatGestureSurfaceView) findViewById(R.id.mysurfaceview);
	        	view.setData(gesturePoints);
            }
    	}
    }
    
    public void cancelGesture(View v) {
    	setResult(RESULT_OK);
        finish();
    }
    
    public void deleteGesture(View v) {
    	if (mOldGestureId != null) {
    		showDialog(DELETE_DIALOG);
    	}
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if (id == DELETE_DIALOG) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you really want to delete this gesture?")
                  .setCancelable(false)
                  .setIcon(R.drawable.vf_icon)
                  .setTitle(R.string.app_name)
                  .setPositiveButton("delete", new Dialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
				    	performDelete();
					}
                  })
   				  .setNegativeButton("cancel", null);
            return builder.create();
    	}
    	return super.onCreateDialog(id);
    }
    
    private void performDelete() {
    	if (mOldGestureId != null) {
        	switch (mActionId) {
            case GestureDetail.ACTION_LAUNCH_APP:
            	((GestyApp)getApplication()).getDatabaseTable().
                deleteAppGesture(mPackageName, mClassName);
            	break;
            case GestureDetail.ACTION_BROWSE_WEB:
            	((GestyApp)getApplication()).getDatabaseTable().
                 	deleteUrlGesture(mUrl);
            	break;
            default:
                ((GestyApp)getApplication()).getDatabaseTable().
                	deleteContactGesture(mContactId, mContactDetailId, mActionId);
            	break;
            }
        	final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();
        	store.removeEntry(mOldGestureId);
    	}
    	setResult(RESULT_OK);
        finish();
    }
    
    public void editGesture(View v) {

    	Intent intent = new Intent(this, CreateGestureActivity.class);
		intent.putExtra(IntentsData.CONTACT_ID, mContactId);
		intent.putExtra(IntentsData.ACTION_ID, mActionId);
		intent.putExtra(IntentsData.CONTACT_DETAIL_ID, mContactDetailId);
		intent.putExtra(IntentsData.GESTURE_ID, mOldGestureId);
		intent.putExtra(IntentsData.CONTACT_DETAIL_VALUE, mContactDetailValue);
		intent.putExtra(IntentsData.PACKAGE_ID, mPackageName);
		intent.putExtra(IntentsData.CLASS_ID, mClassName);
		intent.putExtra(IntentsData.URL_ID, mContactDetailValue);

		startActivity(intent);
		setResult(RESULT_OK);
		finish();
    }
}

