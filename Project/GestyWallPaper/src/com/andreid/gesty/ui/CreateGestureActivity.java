/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andreid.gesty.ui;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.andreid.gesty.GestyApp;
import com.andreid.gesty.R;
import com.andreid.gesty.data.GestureDetail;
import com.andreid.gesty.data.IntentsData;
import com.andreid.gesty.ui.custom.MyGestureOverlay;

public class CreateGestureActivity extends Activity {
    private static final float LENGTH_THRESHOLD = 120.0f;

    private Gesture mGesture;
    private View mDoneButton;

    private long mContactDetailId;
    private long mContactId;
    private String mPackageName;
    private String mClassName;
    private String mContactDetailValue;
    private String mUrl;
    private int mActionId;
    private String mOldGestureId;
    private MyGestureOverlay mOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

        setContentView(R.layout.create_gesture);
        
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        mOverlay = (MyGestureOverlay) findViewById(R.id.gestures_overlay);
//        int color = PreferencesCache.getGestureColor(this);
//        mOverlay.setGestureColor(color);
//        mOverlay.setUncertainGestureColor(color);
//        mOverlay.setFadeEnabled(true);


        mDoneButton = findViewById(R.id.done);
        if (data != null) {
        	mContactId = data.getLong(IntentsData.CONTACT_ID);
            mActionId = data.getInt(IntentsData.ACTION_ID);
            
            mContactDetailId = data.getLong(IntentsData.CONTACT_DETAIL_ID);
            mOldGestureId = data.getString(IntentsData.GESTURE_ID);
            mContactDetailValue = data.getString(IntentsData.CONTACT_DETAIL_VALUE);
            
            mPackageName = data.getString(IntentsData.PACKAGE_ID);
            mClassName = data.getString(IntentsData.CLASS_ID);
            mUrl = data.getString(IntentsData.URL_ID);
            
         	TextView gestureName = (TextView)findViewById(R.id.create_gesture_gesture_name);
            gestureName.setText(mContactDetailValue);
            mOverlay.addOnGestureListener(new GesturesProcessor());

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mGesture != null) {
            outState.putParcelable(IntentsData.GESTURE, mGesture);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        mGesture = savedInstanceState.getParcelable(IntentsData.GESTURE);
        if (mGesture != null) {
        	mOverlay.clear(false);
            mOverlay.post(new Runnable() {
                public void run() {
                    mOverlay.setGesture(mGesture);
                }
            });

            mDoneButton.setEnabled(true);
        }
    }

    public void addGesture(View v) {
        if (mGesture != null) {
            final TextView input = (TextView) findViewById(R.id.create_gesture_gesture_name);
            final CharSequence name = input.getText();
            if (name.length() == 0) {
                input.setError(getString(R.string.error_missing_name));
                return;
            }

            final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();

            String gestureName = null;
            switch (mActionId) {
            case GestureDetail.ACTION_LAUNCH_APP:
            	gestureName = mClassName; 
            	if (mOldGestureId != null) {
            		((GestyApp)getApplication()).getDatabaseTable().
                 	deleteAppGesture(mPackageName, mClassName);
            		store.removeEntry(mOldGestureId);
            	}
                store.addGesture(gestureName, mGesture);
                store.save();
                ((GestyApp)getApplication()).getDatabaseTable().
                addAppGesture(mActionId, mPackageName, mClassName, gestureName);
            	break;
            case GestureDetail.ACTION_BROWSE_WEB:
            	gestureName = mUrl;
            	if (mOldGestureId != null) {
            		((GestyApp)getApplication()).getDatabaseTable().
                 	deleteUrlGesture(mUrl);
            		store.removeEntry(mOldGestureId);

            	}
            	store.addGesture(gestureName, mGesture);
                store.save();
            	((GestyApp)getApplication()).getDatabaseTable().
                addUrlGesture(mActionId, mUrl, mUrl, gestureName);
            	break;
            default:
            	if (mActionId == GestureDetail.ACTION_VIEW_CONTACT) {
            		gestureName = mContactDetailValue;
            	} else {
                	gestureName = mContactDetailId + "_" + mActionId;
            	}
            	if (mOldGestureId != null) {
            		store.removeEntry(mOldGestureId);
                	((GestyApp)getApplication()).getDatabaseTable().
                	deleteContactGesture(mContactId, mContactDetailId, mActionId);
            	}
                store.addGesture(gestureName, mGesture);
                store.save();
                ((GestyApp)getApplication()).getDatabaseTable().
                addContactGesture(mContactId, mContactDetailId, mActionId, gestureName);
            	break;
            }
                		
    		Intent intent = new Intent(this, ViewGestureActivity.class);
    		intent.putExtra(IntentsData.CONTACT_ID, mContactId);
    		intent.putExtra(IntentsData.ACTION_ID, mActionId);
    		intent.putExtra(IntentsData.CONTACT_DETAIL_ID, mContactDetailId);
    		intent.putExtra(IntentsData.GESTURE_ID, gestureName);
    		intent.putExtra(IntentsData.CONTACT_DETAIL_VALUE, mContactDetailValue);
    		intent.putExtra(IntentsData.PACKAGE_ID, mPackageName);
    		intent.putExtra(IntentsData.CLASS_ID, mClassName);
    		intent.putExtra(IntentsData.URL_ID, mContactDetailValue);
            startActivityForResult(intent, GestyApp.DIALOG_CODE);
            

            final String path = new File(Environment.getExternalStorageDirectory(),
                    "gestures").getAbsolutePath();
            Toast.makeText(this, getString(R.string.save_success, path), Toast.LENGTH_LONG).show();
            
        } 
    }
    
    public void cancelGesture(View v) {
        setResult(RESULT_OK);
        finish();
    }
    
    private class GesturesProcessor implements MyGestureOverlay.OnGestureListener {
        public void onGestureStarted(MyGestureOverlay overlay, MotionEvent event) {
            mDoneButton.setEnabled(false);
            mGesture = null;
        }

        public void onGesture(MyGestureOverlay overlay, MotionEvent event) {
        }

        public void onGestureEnded(MyGestureOverlay overlay, MotionEvent event) {
            mGesture = overlay.getGesture();
            if (mGesture.getLength() < LENGTH_THRESHOLD) {
            	//TODO:!
                overlay.clear(true);
                mGesture = null;
            }
            mDoneButton.setEnabled(true);
        }

        public void onGestureCancelled(MyGestureOverlay overlay, MotionEvent event) {
        }
        
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(resultCode);
		if (GestyApp.DIALOG_CODE == requestCode && (resultCode == Activity.RESULT_OK)) {
			finish();
		}
	}
	
	
}
