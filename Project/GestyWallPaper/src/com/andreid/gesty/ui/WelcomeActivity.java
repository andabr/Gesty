package com.andreid.gesty.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.andreid.gesty.GestyApp;
import com.andreid.gesty.R;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
	}
	
	public void next(View v) {
		Intent intent = new Intent(this, GestureOptionsActivity.class);
		intent.putExtra("skip", true);
		startActivityForResult(intent, GestyApp.DIALOG_CODE);
	}
	
	public void skip(View v) {
		Intent intent = new Intent(this, GestureListActivity.class);
		GestyApp gesty = (GestyApp) getApplication();
		gesty.setFirstTime(false);
		setResult(RESULT_OK);
		startActivity(intent);

		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(resultCode);
		if (GestyApp.DIALOG_CODE == requestCode) {
			skip(null);
		}
	}
}
