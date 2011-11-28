package com.andreid.gesty.ui;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.andreid.gesty.PreferencesCache;
import com.andreid.gesty.R;

public class SettingsActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

	private SeekBar mSeekBar;
    private TextView mProgressText;
//    private TextView mTrackingText;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		findViewById(R.id.settings_btn_color).setBackgroundColor(PreferencesCache.getGestureColor(this));
		
		mSeekBar = (SeekBar)findViewById(R.id.seek);
		int progress = ((int)(20*PreferencesCache.getGestureAccuracy(this)));
		mSeekBar.setProgress(progress);

        mSeekBar.setOnSeekBarChangeListener(this);
        mProgressText = (TextView)findViewById(R.id.progress);
        mProgressText.setText( 
                "accuracy is " + ((float)progress)/20 + " of " + 5);
//        mTrackingText = (TextView)findViewById(R.id.tracking);
	}
	
	public void showSettings(View view) {
		// initialColor is the initially-selected color to be shown in the rectangle on the left of the arrow.
		// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware of the initial 0xff which is the alpha.
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, PreferencesCache.getGestureColor(this), 
				new OnAmbilWarnaListener() {
		        @Override
		        public void onOk(AmbilWarnaDialog dialog, int color) {
		            PreferencesCache.setGestureColor(SettingsActivity.this, color);
		    		findViewById(R.id.settings_btn_color).setBackgroundColor(color);
		        }
		        @Override
		        public void onCancel(AmbilWarnaDialog dialog) {
		        }
		});

		dialog.show();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			PreferencesCache.setGestureAccuracy(this, ((float)mSeekBar.getProgress())/20);
		}
		return super.onKeyDown(keyCode, event);
	}
	
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        mProgressText.setText( 
                "accuracy is " + ((float)progress)/20 + " of " + 5);
    }

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}


}
