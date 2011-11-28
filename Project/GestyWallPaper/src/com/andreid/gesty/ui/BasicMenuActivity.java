package com.andreid.gesty.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;

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
		showAbout(view);
	}

}
