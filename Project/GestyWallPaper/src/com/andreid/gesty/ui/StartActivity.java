package com.andreid.gesty.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.andreid.gesty.GestyApp;

/**
 * The activity starting user interface and service of RoamingApplication.
 *
 */
public class StartActivity extends Activity {
	

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        startFirstActivity();
    }
    
    
    private void startFirstActivity() {
    	GestyApp app = (GestyApp)getApplication();
    	boolean firstTime = app.getDatabaseTable().isEmpty();
    	app.setFirstTime(firstTime);
    	if (firstTime) {
    		Intent launchIntent = new Intent();
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			launchIntent.setClassName(getPackageName(),
			           WelcomeActivity.class.getName());
			startActivity(launchIntent);
			  /**
			   * Close this activity so it doesn't show up when pressing the back
			   * key.
			   */

    	} else {
    		Intent launchIntent = new Intent();
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			launchIntent.setClassName(getPackageName(),
			           GestureListActivity.class.getName());
			startActivity(launchIntent);
    	}
		finish();
    }

    
}
