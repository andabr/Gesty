package com.andreid.gesty.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.andreid.gesty.GestyApp;
import com.andreid.gesty.R;
import com.andreid.gesty.data.IntentsData;

public class GestureOptionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_new_gesture);
				Intent intent = getIntent();
		if (intent.getBooleanExtra("skip", false)) {
			findViewById(R.id.opt_skip).setVisibility(View.VISIBLE);
		}

	}

	public void createWeb(View v) {
		Intent intent = new Intent(GestureOptionsActivity.this, InputUrlActivity.class);
		startActivityForResult(intent, GestyApp.DIALOG_CODE);
	}
	
	public void createApp(View v) {
		Intent intent = new Intent(GestureOptionsActivity.this, AppsLauncherActivity.class);
		startActivityForResult(intent, GestyApp.DIALOG_CODE);
	}

	public void createContact(View v) {
		Intent intent = new Intent(GestureOptionsActivity.this, ContactListActivity.class);
		startActivityForResult(intent, GestyApp.DIALOG_CODE);
	}
	
	public void createDirectContact(View v) {
		Intent intent = new Intent(GestureOptionsActivity.this, ContactListActivity.class);
		intent.putExtra(IntentsData.MODE_DIRECT, true);
		startActivityForResult(intent, GestyApp.DIALOG_CODE);
	}
	
	public void skip(View v) {
		setResult(RESULT_OK);
		finish();

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (GestyApp.DIALOG_CODE == requestCode && (resultCode == Activity.RESULT_OK)) {
			finish();
		}
	}
	
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
