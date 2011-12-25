package com.vodafone.gesty.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import com.vodafone.gesty.R;
import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.PreferencesCache;
import com.vodafone.gesty.data.Contact;
import com.vodafone.gesty.data.ContactDetail;
import com.vodafone.gesty.data.GestureDetail;
import com.vodafone.gesty.data.GesturesTable;
import com.vodafone.gesty.data.NativeContacts;
import com.vodafone.gesty.ui.custom.MyGestureOverlay;
import com.vodafone.gesty.ui.custom.SynchronousGestureListener;

public class GestureDetectorActivity extends Activity implements SynchronousGestureListener {
    private GestureLibrary mLibrary;
    private String mPredictionName;
    private MyGestureOverlay mOverlay;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestures_detector);
        mLibrary = ((GestyApp)getApplication()).getGesturesLibrary();
        mOverlay = (MyGestureOverlay) findViewById(R.id.gestures);
        int color = PreferencesCache.getGestureColor(this);
        mOverlay.setGestureColor(color);
        mOverlay.setUncertainGestureColor(color);
        mOverlay.setSynchronousGesturePerformedListener(this);
        mOverlay.setFadeEnabled(true);
        
    }	
	
	private void processContactAction(Contact contact, int actionId, long contactDetailId) {
		if (actionId == GestureDetail.ACTION_VIEW_CONTACT) {
        	viewContact(contact.getId());
		} else {
			ContactDetail cd = contact.getContactDetail(actionId, contactDetailId);
			if (cd != null) {
				String detailValue = contact.getContactDetail(actionId, contactDetailId).getValue();
				switch (actionId) {
		        case GestureDetail.ACTION_CALL:
		        	dialNumber(detailValue);
		            break;
		        case GestureDetail.ACTION_TEXT:
		        	openText(detailValue);
		            break;
		        case GestureDetail.ACTION_EMAIL:
		        	openEmail(detailValue);
		            break;
		        default:
		            break;
		        }
			}
		}
	}
	
	private void viewContact(long contactId) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));
		intent.setData(uri);
		startActivity(intent);
	}

	private void openEmail(String detailValue) {
        Intent mailIntent = new Intent(Intent.ACTION_SEND);
        mailIntent.setType("application/xhtml+xml"); 
        mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{detailValue});
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hello");
        
        startActivity(mailIntent);
    }
	
	private void openText(String detailValue) {
		Intent intent = new Intent(Intent.ACTION_VIEW,  Uri.fromParts("sms", detailValue, null)); 
		intent.putExtra(Intent.EXTRA_TEXT, "");
		startActivity(intent);
		finish();
	}
	
	private void dialNumber(String detailValue) {
		final Uri numberUri = Uri.parse("tel:" + detailValue);
        if (numberUri != null) {
            final Intent intent = new Intent(Intent.ACTION_CALL, numberUri);
            intent.putExtra(Intent.EXTRA_PHONE_NUMBER, detailValue);
            startActivity(intent);
        }
	}
	
	
	public void gotoGesturesList(View v) {
        Intent intent = new Intent(this, StartActivity.class);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
        startActivity(intent);
        finish();
    }



	@Override
	public void onGesturePerformedStarted(MyGestureOverlay overlay, Gesture gesture) {
    	Log.e("GestureDetectorActivity", " sync gesturePerformed");

		ArrayList<Prediction> list = mLibrary.recognize(gesture);
		if (list != null && (list.size() > 0)) {
			float threshold = PreferencesCache.getGestureAccuracy(GestureDetectorActivity.this);
			for (Prediction prediction: list) {
		    	Log.e("Predictions:",  "name=" + prediction.name + "...score=" + prediction.score);

				if (prediction.score > threshold) {
					ArrayList<Gesture> array = mLibrary.getGestures(prediction.name);

					for (Gesture predicted: array) {
						if (predicted.getStrokesCount() == gesture.getStrokesCount()) {
					    	Log.e("Prediction was matched to:"," name=" + predicted.getID());

							mPredictionName = prediction.name;
							overlay.setRecognized(true);
					    	return;
						}
					}
			    }
			}
		} 
		overlay.setRecognized(false);
	}
	
	@Override
	public void onGesturePerformedFinished(MyGestureOverlay overlay, Gesture gesture) {
		Log.e("GestureDetectorActivity", "sync gesturePerformed2");
    	if (mPredictionName != null) {
        	GesturesTable table = ((GestyApp)getApplication()).getDatabaseTable();

        	GestureDetail detail = table.getGesture(mPredictionName);
        	
    	    if (detail != null) {
    	    	try {
    	    		if (detail.getActionId() == GestureDetail.ACTION_LAUNCH_APP) {
        	            Intent intent = new Intent(Intent.ACTION_MAIN, null);
        	            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        	            //this is killer!
        				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        	            intent.setClassName(detail.getPackageName(), detail.getClassName());
        	            startActivity(intent);
        	    	} else if (detail.getActionId() == GestureDetail.ACTION_BROWSE_WEB) {
        	    		Intent i = new Intent(Intent.ACTION_VIEW);
        	    		i.setData(Uri.parse(detail.getUrl().startsWith("http")? 
        	    				detail.getUrl(): "http://"+detail.getUrl()));
        	    		startActivity(i);
        	        } else {
        	    		Contact contact = NativeContacts.getContact(getContentResolver(), detail.getContactId());
        		    	if (contact != null) {
        		    		processContactAction(contact, detail.getActionId(), detail.getId());
        				}	
        	    	}

            	} catch(Exception exc) {
            		exc.printStackTrace();
            	}
    	    }	
    		finish();    		
    	}		
	}
	

	@Override
	protected void onDestroy() {
		if (mOverlay != null) {
			mOverlay.setSynchronousGesturePerformedListener(null);
		}
		super.onDestroy();
	}
}