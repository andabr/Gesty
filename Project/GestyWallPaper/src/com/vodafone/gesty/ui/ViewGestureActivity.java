
package com.vodafone.gesty.ui;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.R;
import com.vodafone.gesty.data.GestureDetail;
import com.vodafone.gesty.ui.AppsLauncherActivity.AppListItem;
import com.vodafone.gesty.ui.AppsLauncherActivity.IconResizer;
import com.vodafone.gesty.ui.custom.RepeatGestureSurfaceView;

public class ViewGestureActivity extends Activity {
 
    private long mContactDetailId;
    private long mContactId;
    private String mPackageName;
    private String mClassName;
    private String mContactDetailValue;
    private String mUrl;
    private String mContactName;
    private int mActionId;

    private String mOldGestureId;
    private static final int DELETE_DIALOG = 12;
    
    private long mPhotoId;
    private RepeatGestureSurfaceView mSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

        setContentView(R.layout.view_gesture);
        
        Intent intent = getIntent();
    	mSurfaceView = (RepeatGestureSurfaceView) findViewById(R.id.mysurfaceview);

        Bundle data = intent.getExtras();
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

            getGesture();
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
	        	mSurfaceView.setData(gesturePoints);
            }
    	}
    }
    
    public void acceptGesture(View v) {
    	setResult(RESULT_OK);
    	mSurfaceView.stop();
        finish();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		setResult(RESULT_OK);
        	mSurfaceView.stop();
            finish();
            return true;
    	}
    	return super.onKeyDown(keyCode, event);
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
            builder.setMessage(getString(R.string.delete_gesture_popup_text))
                  .setCancelable(false)
                  .setIcon(R.drawable.vf_icon)
                  .setTitle(R.string.app_name)
                  .setPositiveButton(getString(R.string.button_delete), new Dialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
				    	performDelete();
					}
                  })
   				  .setNegativeButton(getString(R.string.button_cancel), null);
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
    	mSurfaceView.stop();

        finish();
    }
    
    public void editGesture(View v) {

    	Intent intent = new Intent(this, CreateGestureActivity.class);
		intent.putExtra(Constants.CONTACT_ID, mContactId);
		intent.putExtra(Constants.ACTION_ID, mActionId);
		intent.putExtra(Constants.CONTACT_DETAIL_ID, mContactDetailId);
		intent.putExtra(Constants.GESTURE_ID, mOldGestureId);
		intent.putExtra(Constants.CONTACT_DETAIL_VALUE, mContactDetailValue);
		intent.putExtra(Constants.PACKAGE_ID, mPackageName);
		intent.putExtra(Constants.CLASS_ID, mClassName);
		intent.putExtra(Constants.URL_ID, mContactDetailValue);
		intent.putExtra(Constants.CONTACT_NAME, mContactName);
		intent.putExtra(Constants.PHOTO_ID, mPhotoId);

		startActivity(intent);
		setResult(RESULT_OK);
    	mSurfaceView.stop();

		finish();
    }
}

