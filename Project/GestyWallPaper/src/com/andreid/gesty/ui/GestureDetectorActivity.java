package com.andreid.gesty.ui;

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

import com.andreid.gesty.GestyApp;
import com.andreid.gesty.PreferencesCache;
import com.andreid.gesty.R;
import com.andreid.gesty.data.Contact;
import com.andreid.gesty.data.GestureDetail;
import com.andreid.gesty.data.GesturesTable;
import com.andreid.gesty.data.NativeContacts;
import com.andreid.gesty.ui.custom.MyGestureOverlay;
import com.andreid.gesty.ui.custom.SynchronousGestureListener;

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
		String detailValue = null;

		switch (actionId) {
        case GestureDetail.ACTION_CALL:
        	detailValue = contact.getContactDetail(actionId, contactDetailId).getValue();
        	dialNumber(detailValue);
            break;
        case GestureDetail.ACTION_TEXT:
        	detailValue = contact.getContactDetail(actionId, contactDetailId).getValue();
        	openText(detailValue);
            break;
        case GestureDetail.ACTION_VIEW_CONTACT:
        	viewContact(contact.getId());
        	break;
        case GestureDetail.ACTION_EMAIL:
        	detailValue = contact.getContactDetail(actionId, contactDetailId).getValue();
        	openEmail(detailValue);
            break;
        default:
            break;
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
        Intent intent = new Intent(this, GestureListActivity.class);
//        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);
        //this is killer!

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
        startActivity(intent);
        finish();
    }



	@Override
	public void onGesturePerformed(MyGestureOverlay overlay, Gesture gesture) {
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
	
	
	
//	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {	
//    	Log.e("GestureDetectorActivity", "standard gesturePerformed");
//	}

	@Override
	protected void onDestroy() {
		if (mOverlay != null) {
			mOverlay.setSynchronousGesturePerformedListener(null);
		}
		super.onDestroy();
	}

	@Override
	public void onGesturePerformed2(MyGestureOverlay overlay, Gesture gesture) {
		Log.e("GestureDetectorActivity", "sync gesturePerformed2");
    	if (mPredictionName != null) {
        	GesturesTable table = ((GestyApp)getApplication()).getDatabaseTable();

        	GestureDetail detail = table.getGesture(mPredictionName);
    	    if (detail != null) {
    	    	if (detail.getActionId() == GestureDetail.ACTION_LAUNCH_APP) {
    	            Intent intent = new Intent(Intent.ACTION_MAIN, null);
    	            intent.addCategory(Intent.CATEGORY_LAUNCHER);
    	            //this is killer!
    				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    	            intent.setClassName(detail.getPackageName(), detail.getClassName());
    	            startActivity(intent);
    	    	} else if (detail.getActionId() == GestureDetail.ACTION_BROWSE_WEB) {
    	    		Intent i = new Intent(Intent.ACTION_VIEW);
    	    		i.setData(Uri.parse(detail.getUrl()));
    	    		startActivity(i);
    	        } else {
    	    		Contact contact = NativeContacts.getContact(getContentResolver(), detail.getContactId());
    		    	if (contact != null) {
    		    		processContactAction(contact, detail.getActionId(), detail.getDetailId());
    				}	
    	    	}
    	    }	
    		finish();    		
    	}		
	}
}