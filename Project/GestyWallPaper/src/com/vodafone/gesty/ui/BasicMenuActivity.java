package com.vodafone.gesty.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;

/**
 * 
 * Activity to be extended by those activities that want
 *  to use the benefit of menu functionality.
 */
public abstract class BasicMenuActivity extends ListActivity {

	public void showSettings(View view) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void showAbout(View view) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
	
	public void showHelp(View view) {
		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}

}
