package com.andreid.gesty.ui;

import com.andreid.gesty.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity used to display the About Information
 */
public class AboutActivity extends Activity {
	/** Log Tag */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		TextView versionText = (TextView) findViewById(R.id.about_text_version);
		versionText.append(" "+getVersionName(this));
	}

	/** 
	 * Utility method used to fetch the application's version name
	 * @param context The context that provides access to the Package Manager instance
	 * @return The application version name
	 */
	private static String getVersionName(Context context) {
        PackageInfo info;
        //int versionCode = -1;
        String versionName = "Unknown";
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            //versionCode = info.versionCode;
            versionName = info.versionName;

        } catch (NameNotFoundException e) {
        	
        }
        
        return versionName;
		
	}		
}
