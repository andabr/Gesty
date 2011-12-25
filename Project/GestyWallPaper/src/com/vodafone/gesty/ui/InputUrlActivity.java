package com.vodafone.gesty.ui;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vodafone.gesty.R;
import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.PreferencesCache;
import com.vodafone.gesty.data.GestureDetail;
import com.vodafone.gesty.ui.custom.MyGestureOverlay;

public class InputUrlActivity extends Activity {
	private TextView mUrl;
	
	private MyGestureOverlay mOverlay;
	private Button mBtnNext;
    private Gesture mGesture;
    private Handler mDelayHandler = new Handler();
	private static final long DELAY_FIRST_TEXT_CHANGED = 500;
	final String HTTP_PREFIX = "http://";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

		setContentView(R.layout.input_url_screen);
		mUrl = (TextView) findViewById(R.id.url_textview);

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
         checkUrl();

	}
	
	public boolean checkUrl() {
		String url = mUrl.getText().toString().trim();
		if (TextUtils.isEmpty(url) || HTTP_PREFIX.equals(url)) {
       	 	showDialog(Constants.DIALOG_ENTER_URL);
       	 	return false;
        }
		return true;
	}
	
	public void clickUrl(View v) {
   	 	showDialog(Constants.DIALOG_ENTER_URL);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(resultCode);
		if (GestyApp.DIALOG_CODE == requestCode && (resultCode == Activity.RESULT_OK)) {
			finish();
		}
	}
	

    private class GesturesProcessor implements MyGestureOverlay.OnGestureListener {
        public void onGestureStarted(MyGestureOverlay overlay, MotionEvent event) {
            mBtnNext.setEnabled(false);
            mGesture = null;
        }

        public void onGesture(MyGestureOverlay overlay, MotionEvent event) {
        }

        public void onGestureEnded(MyGestureOverlay overlay, MotionEvent event) {
            mGesture = overlay.getGesture();
            if (mGesture.getLength() < Constants.LENGTH_THRESHOLD) {
				clearGesture(null);
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
            outState.putParcelable(Constants.GESTURE, mGesture);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        mGesture = savedInstanceState.getParcelable(Constants.GESTURE);
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
    
    private void performAddGesture() {

        String gestureName = mUrl.getText().toString();

        final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();

        int actionId = GestureDetail.ACTION_BROWSE_WEB;
        ((GestyApp)getApplication()).getDatabaseTable().deleteUrlGesture(gestureName);
        store.removeEntry(gestureName);
        store.addGesture(gestureName, mGesture);
        store.save();
        ((GestyApp)getApplication()).getDatabaseTable().
            addUrlGesture(actionId, gestureName, gestureName);
            		
		Intent intent = new Intent(this, ViewGestureActivity.class);
		intent.putExtra(Constants.ACTION_ID, actionId);
		intent.putExtra(Constants.GESTURE_ID, gestureName);
		intent.putExtra(Constants.CONTACT_DETAIL_VALUE, gestureName);
		intent.putExtra(Constants.URL_ID, gestureName);
        startActivityForResult(intent, GestyApp.DIALOG_CODE);
        
        Toast.makeText(this, getString(R.string.save_success), Toast.LENGTH_LONG).show();
    }
    
    public void addGesture(View v) {
        if (mGesture != null) {
        	
        	if (checkLength()) {
            	return;
            }

            final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();
            String recognizedName = checkRecognized(store);
            
            String gestureName = mUrl.getText().toString();
            if (checkUrl()) {
                if (recognizedName != null && !gestureName.equals(recognizedName)) {
                	showDialog(Constants.DUPLICATE_DIALOG);
                	return;
                }
                performAddGesture();
            } 
        } 
    }
    
    public void cancelGesture(View v) {
        setResult(RESULT_OK);
        finish();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if (id == Constants.DIALOG_ENTER_URL) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        	final EditText editUrl = (EditText) textEntryView.findViewById(R.id.url_edit);
        	if (!TextUtils.isEmpty(mUrl.getText())) {
          		editUrl.setText(mUrl.getText());
          	}

            editUrl.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    		editUrl.setSelection(editUrl.getText().length());
          	
    		
    		final AlertDialog dialog = new AlertDialog.Builder(InputUrlActivity.this)
                .setIcon(R.drawable.vf_icon)
                .setTitle(getString(R.string.url_gesture_popup_title))
                .setView(textEntryView)
                .setPositiveButton(getString(R.string.button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                      	mUrl.setText(editUrl.getText());
                    }
                })
                .setNegativeButton(getString(R.string.button_cancel), null)
                .create();
    		final TextWatcher listener = new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
					if (saveButton != null) {
						saveButton.setEnabled(Patterns.WEB_URL.matcher(s.toString()).matches());
                      	mUrl.setText(s);
					}
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				@Override
				public void afterTextChanged(Editable s) {
				}
    		};
    		editUrl.addTextChangedListener(listener);
    		mDelayHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
		    		listener.onTextChanged(editUrl.getText().toString(), 0, 0, 0);
					
				}
			}, DELAY_FIRST_TEXT_CHANGED);
    		return dialog;

    	} else if (id == Constants.DUPLICATE_DIALOG) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.gesture_exists_popup_text))
                  .setCancelable(false)
                  .setIcon(R.drawable.vf_icon)
                  .setTitle(R.string.app_name)
                  .setPositiveButton(getString(R.string.button_save), new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				    	performAddGesture();
					}
                  })
   				  .setNegativeButton(getString(R.string.button_retry), new Dialog.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						clearGesture(null);
    					}
                    });
            return builder.create();
    	}
    	return super.onCreateDialog(id);
    }
        
    private boolean checkLength() {
    	if (mGesture.getLength() > Constants.LENGTH_GESTURE_TOO_LONG) {
    		mDelayHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					clearGesture(null);
				}
			}, Constants.DURATION_TOAST);
            Toast toast = Toast.makeText(this, getString(R.string.gesture_too_long_toast), Constants.DURATION_TOAST);
            toast.show();
            return true;
        }
    	return false;
    }
    
    private String checkRecognized(GestureLibrary mLibrary) {
    	ArrayList<Prediction> list = mLibrary.recognize(mGesture);
		if (list != null && (list.size() > 0)) {
			float threshold = PreferencesCache.getGestureAccuracy(this);
			for (Prediction prediction: list) {
		    	Log.e("Predictions:",  "name=" + prediction.name + "...score=" + prediction.score);

				if (prediction.score > threshold) {
					ArrayList<Gesture> array = mLibrary.getGestures(prediction.name);

					for (Gesture predicted: array) {
						if (predicted.getStrokesCount() == mGesture.getStrokesCount()) {
					    	Log.e("Prediction was matched to:"," name=" + predicted.getID());
					    	return prediction.name;
						}
					}
			    }
			}
		} 
		return null;
    }
    
    public void clearGesture(View v) {
		mOverlay.clear(false);
		mGesture = null;
    }
    

}
