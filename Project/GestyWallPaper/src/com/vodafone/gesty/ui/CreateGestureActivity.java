
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.PreferencesCache;
import com.vodafone.gesty.R;
import com.vodafone.gesty.data.GestureDetail;
import com.vodafone.gesty.ui.AppsLauncherActivity.AppListItem;
import com.vodafone.gesty.ui.AppsLauncherActivity.IconResizer;
import com.vodafone.gesty.ui.custom.MyGestureOverlay;

public class CreateGestureActivity extends Activity {
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
    private String mContactName;
    private MyGestureOverlay mOverlay;
    private long mPhotoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

        setContentView(R.layout.create_gesture);
        
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        mOverlay = (MyGestureOverlay) findViewById(R.id.gestures_overlay);

        mDoneButton = findViewById(R.id.done);
        if (data != null) {
        	mContactId = data.getLong(Constants.CONTACT_ID);
            mActionId = data.getInt(Constants.ACTION_ID);
            
            mContactDetailId = data.getLong(Constants.CONTACT_DETAIL_ID);
            mOldGestureId = data.getString(Constants.GESTURE_ID);
            mContactDetailValue = data.getString(Constants.CONTACT_DETAIL_VALUE);
            
            mPackageName = data.getString(Constants.PACKAGE_ID);
            mClassName = data.getString(Constants.CLASS_ID);
            mUrl = data.getString(Constants.URL_ID);
            
            mContactName = data.getString(Constants.CONTACT_NAME);
            mPhotoId = data.getLong(Constants.PHOTO_ID);
            
            TextView contactName = (TextView)findViewById(R.id.contact_textName);
        	TextView actionName = (TextView)findViewById(R.id.contact_textActionDetails);
        	ImageView actionIcon = (ImageView)findViewById(R.id.contact_actionImage);
            ImageView contactPicture = (ImageView)findViewById(R.id.contact_imageAvatar);
        	initProfileHeader(contactPicture, actionIcon, contactName, actionName);
        	
            mOverlay.addOnGestureListener(new GesturesProcessor());

        }
    }
    
    private void initProfileHeader(ImageView avatar, ImageView iconImage, TextView title, TextView actionText) {
    	switch (mActionId) {
    	case GestureDetail.ACTION_EMAIL:
            ContactPhotoLoader mPhotoLoader = ((GestyApp)getApplication()).getContactPhotoLoader();
    		mPhotoLoader.loadPhoto(avatar, mPhotoId);
    		iconImage.setImageResource(R.drawable.icon_punch_email);
    		title.setText(mContactName);
    		actionText.setText(mContactDetailValue);
    		break;
    	case GestureDetail.ACTION_CALL:
    		mPhotoLoader = ((GestyApp)getApplication()).getContactPhotoLoader();
    		mPhotoLoader.loadPhoto(avatar, mPhotoId);
    		iconImage.setImageResource(R.drawable.icon_punch_call);
    		title.setText(mContactName);
    		actionText.setText(mContactDetailValue);
    		break;
    	case GestureDetail.ACTION_TEXT:
    		mPhotoLoader = ((GestyApp)getApplication()).getContactPhotoLoader();
    		mPhotoLoader.loadPhoto(avatar, mPhotoId);
    		iconImage.setImageResource(R.drawable.icon_punch_sms);
    		title.setText(mContactName);
    		actionText.setText(mContactDetailValue);
    		break;
    	case GestureDetail.ACTION_VIEW_CONTACT:
    		mPhotoLoader = ((GestyApp)getApplication()).getContactPhotoLoader();
    		mPhotoLoader.loadPhoto(avatar, mPhotoId);
    		iconImage.setVisibility(View.GONE);
    		actionText.setVisibility(View.GONE);
    		title.setText(mContactName);
    		break;
    	case GestureDetail.ACTION_LAUNCH_APP:
    		AppListItem item = ((GestyApp)getApplication()).getAppList().
    			getAppInfoByPackageName(mPackageName);
    		if (item != null) {
        		if (item.icon == null) {
        	        IconResizer iconResizer = new IconResizer(this);
        			item.icon = iconResizer.createIconThumbnail(
                            item.resolveInfo.loadIcon(getPackageManager()));
        		}
        		avatar.setImageDrawable(item.icon);
    		}
    		iconImage.setVisibility(View.GONE);
    		actionText.setVisibility(View.GONE);
    		title.setText(mContactDetailValue);
    		break;
    	case GestureDetail.ACTION_BROWSE_WEB:
    		avatar.setImageResource(R.drawable.icon_url_big);
    		title.setText(mContactDetailValue);
    		iconImage.setVisibility(View.GONE);
    		actionText.setVisibility(View.GONE);
    		break;
    	default:
    		break;
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

            mDoneButton.setEnabled(true);
        }
    }

    private Handler mHandler = new Handler();
    
    
    private boolean checkLength() {
    	if (mGesture.getLength() > Constants.LENGTH_GESTURE_TOO_LONG) {
            mHandler.postDelayed(new Runnable() {
				
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
    
    public void clearGesture(View v) {
		mOverlay.clear(false);
		mGesture = null;
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
    
    private String getGestureName() {
    	switch (mActionId) {
        case GestureDetail.ACTION_LAUNCH_APP:
        	return mClassName; 
        case GestureDetail.ACTION_BROWSE_WEB:
        	return mUrl;
        default:
        	if (mActionId == GestureDetail.ACTION_VIEW_CONTACT) {
        		return mContactDetailValue;
        	} else {
            	return mContactDetailId + "_" + mActionId;
        	}
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if (id == Constants.DUPLICATE_DIALOG) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.gesture_exists_popup_text))
                  .setCancelable(false)
                  .setIcon(R.drawable.vf_icon)
                  .setTitle(R.string.app_name)
                  .setPositiveButton(getString(R.string.button_save), new Dialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
			            final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();
				    	performAddGesture(store, getGestureName());
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
    
    private void performAddGesture(GestureLibrary store, String gestureName) {
    	switch (mActionId) {
        case GestureDetail.ACTION_LAUNCH_APP:
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
         	if (mOldGestureId != null) {
         		((GestyApp)getApplication()).getDatabaseTable().
              	deleteUrlGesture(mUrl);
         		store.removeEntry(mOldGestureId);
         	}
         	store.addGesture(gestureName, mGesture);
            store.save();
         	((GestyApp)getApplication()).getDatabaseTable().
            addUrlGesture(mActionId, mUrl, gestureName);
         	break;
        default:
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
 		intent.putExtra(Constants.CONTACT_ID, mContactId);
 		intent.putExtra(Constants.ACTION_ID, mActionId);
 		intent.putExtra(Constants.CONTACT_DETAIL_ID, mContactDetailId);
 		intent.putExtra(Constants.GESTURE_ID, gestureName);
 		intent.putExtra(Constants.CONTACT_DETAIL_VALUE, mContactDetailValue);
 		intent.putExtra(Constants.PACKAGE_ID, mPackageName);
 		intent.putExtra(Constants.CLASS_ID, mClassName);
 		intent.putExtra(Constants.URL_ID, mContactDetailValue);
		intent.putExtra(Constants.PHOTO_ID, mPhotoId);
		intent.putExtra(Constants.CONTACT_NAME, mContactName);


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
            
            String gestureName = getGestureName();
            if (recognizedName != null && !gestureName.equals(recognizedName)) {
            	showDialog(Constants.DUPLICATE_DIALOG);
            	return;
            }
            
            performAddGesture(store, gestureName);
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
            if (mGesture.getLength() < Constants.LENGTH_THRESHOLD) {
                clearGesture(null);
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
