package com.vodafone.gesty.ui;


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.vodafone.gesty.R;

/**
 * Activity used to display the About Information
 */
public class AboutActivity extends Activity {
	/** Log Tag */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.about);
	}		
}
