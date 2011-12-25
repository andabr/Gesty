package com.vodafone.gesty.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.vodafone.gesty.R;
import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.data.GestureItem;

public class GestureListActivity extends BasicMenuActivity {
    
    private ToggleButton mAllToggle;
    private ToggleButton mContactsToggle;
    private ToggleButton mAppsToggle;
    private ToggleButton mUrlsToggle;
    private int mShow;

    private GestureListAdapter mAdapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);      
		super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_list);
        mAdapter = new GestureListAdapter(this, getGestyApp().getContactPhotoLoader());
        
		setListAdapter(mAdapter);
		mAllToggle = (ToggleButton) findViewById(R.id.tab_all);
        mAllToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateButtons(GestureListAdapter.MODE_ALL);
				mAdapter.setMode(mShow);
			}
		});
        mContactsToggle = (ToggleButton) findViewById(R.id.tab_contacts);
        mContactsToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateButtons(GestureListAdapter.MODE_CONTACTS);
				mAdapter.setMode(mShow);
			}
		});
        mAppsToggle = (ToggleButton) findViewById(R.id.tab_apps);
        mAppsToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateButtons(GestureListAdapter.MODE_APPS);
				mAdapter.setMode(mShow);
			}
		});
        mUrlsToggle = (ToggleButton) findViewById(R.id.tab_url);
        mUrlsToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateButtons(GestureListAdapter.MODE_URLS);
				mAdapter.setMode(mShow);
			}
		});
       View newGestureButton = findViewById(R.id.gestureslist_footer);
        newGestureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GestureListActivity.this, GestureOptionsActivity.class);
				startActivity(intent);
			}
		});
        updateButtons(GestureListAdapter.MODE_ALL);
    }
  
    private void updateButtons(int showGestures) {
    	mShow = showGestures;
    	mUrlsToggle.setChecked(mShow == GestureListAdapter.MODE_URLS);
    	mAppsToggle.setChecked(mShow == GestureListAdapter.MODE_APPS);
    	mContactsToggle.setChecked(mShow == GestureListAdapter.MODE_CONTACTS);
    	mAllToggle.setChecked(mShow == GestureListAdapter.MODE_ALL);
    }
	
	private GestyApp getGestyApp() {
	    return (GestyApp)getApplication();
	}
	
	
	@Override
	protected void onResume() {
	    super.onStart();
	    if (mAdapter != null) {
	    	mAdapter.refreshData();
	    }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.onDestroy();
			mAdapter = null;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		GestureItem item = mAdapter.getItem(position);
		Intent intent = new Intent(this, ViewGestureActivity.class);
		intent.putExtra(Constants.CONTACT_ID, item.getCid());
		intent.putExtra(Constants.ACTION_ID, item.getActionId());
		intent.putExtra(Constants.CONTACT_DETAIL_ID, item.getDid());
		intent.putExtra(Constants.GESTURE_ID, item.getGestureId());
		intent.putExtra(Constants.CONTACT_DETAIL_VALUE, item.getContactDetailValue());
		intent.putExtra(Constants.PACKAGE_ID, item.getPackage());
		intent.putExtra(Constants.CLASS_ID, item.getClassName());
		intent.putExtra(Constants.URL_ID, item.getContactDetailValue());
		intent.putExtra(Constants.CONTACT_NAME, item.getName());
		intent.putExtra(Constants.PHOTO_ID, item.getAvatarId());


		startActivity(intent);
	}
	
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.MODE, mShow);        
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mShow = savedInstanceState.getInt(Constants.MODE);
        updateButtons(mShow);
    }

}
