package com.andreid.gesty.ui;


import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.andreid.gesty.GestyApp;
import com.andreid.gesty.PreferencesCache;
import com.andreid.gesty.R;
import com.andreid.gesty.data.GestureDetail;
import com.andreid.gesty.data.IntentsData;
import com.andreid.gesty.ui.custom.MyGestureOverlay;

public class InputUrlActivity extends Activity {
	private EditText mUrl;
	private MyGestureOverlay mOverlay;
	private Button mBtnNext;
    private Gesture mGesture;
    private final static int DIALOG_ENTER_URL = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

		setContentView(R.layout.input_url_screen);
		mUrl = (EditText) findViewById(R.id.input_url_edit);
		mUrl.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		mUrl.setSelection(mUrl.getText().length());
		Button btnCancel = (Button) findViewById(R.id.input_url_cancel); 
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelGesture(v);
			}
		});
		
		mBtnNext = (Button) findViewById(R.id.input_url_next); 
		mBtnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addGesture(v);
			}
		});
		
		 mOverlay = (MyGestureOverlay) findViewById(R.id.gestures_overlay);
	     int color = PreferencesCache.getGestureColor(this);
	     mOverlay.setGestureColor(color);
	     mOverlay.setUncertainGestureColor(color);
	     mOverlay.setFadeEnabled(true);
         mOverlay.addOnGestureListener(new GesturesProcessor());
         if (TextUtils.isEmpty(mUrl.getText().toString())) {
        	 showDialog(DIALOG_ENTER_URL);
         }

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(resultCode);
		if (GestyApp.DIALOG_CODE == requestCode && (resultCode == Activity.RESULT_OK)) {
			finish();
		}
	}
	
    private static final float LENGTH_THRESHOLD = 120.0f;

    private class GesturesProcessor implements MyGestureOverlay.OnGestureListener {
        public void onGestureStarted(MyGestureOverlay overlay, MotionEvent event) {
            mBtnNext.setEnabled(false);
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
            mBtnNext.setEnabled(true);
        }

        public void onGestureCancelled(MyGestureOverlay overlay, MotionEvent event) {
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

            mBtnNext.setEnabled(true);
        }
    }
    
    
    public void addGesture(View v) {
        if (mGesture != null) {
        	if (TextUtils.isEmpty(mUrl.getText().toString())) {
           	 	showDialog(DIALOG_ENTER_URL);
           	 	return;
            }
            String gestureName = mUrl.getText().toString();
            if (TextUtils.isEmpty(gestureName)) {
                mUrl.setError(getString(R.string.error_missing_name));
                return;
            }
            final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();

            int actionId = GestureDetail.ACTION_BROWSE_WEB;
            ((GestyApp)getApplication()).getDatabaseTable().deleteUrlGesture(gestureName);
            store.removeEntry(gestureName);
            store.addGesture(gestureName, mGesture);
            store.save();
            ((GestyApp)getApplication()).getDatabaseTable().
                addUrlGesture(actionId, gestureName, gestureName, gestureName);
                		
    		Intent intent = new Intent(this, ViewGestureActivity.class);
    		intent.putExtra(IntentsData.ACTION_ID, actionId);
    		intent.putExtra(IntentsData.GESTURE_ID, gestureName);
    		intent.putExtra(IntentsData.CONTACT_DETAIL_VALUE, gestureName);
    		intent.putExtra(IntentsData.URL_ID, gestureName);
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
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if (id == DIALOG_ENTER_URL) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        	final EditText editUrl = (EditText) textEntryView.findViewById(R.id.url_edit);
            editUrl.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    		editUrl.setSelection(editUrl.getText().length());
    		
            return new AlertDialog.Builder(InputUrlActivity.this)
                .setIcon(R.drawable.vf_icon)
                .setTitle("Enter the url here:")
                .setView(textEntryView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
    
                        /* User clicked OK so do some stuff */
                    	mUrl.setText(editUrl.getText());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();

    	}
    	return super.onCreateDialog(id);
    }
    

}
