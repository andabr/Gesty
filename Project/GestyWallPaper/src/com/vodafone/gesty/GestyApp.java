package com.vodafone.gesty;

import java.io.File;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.os.Environment;

import com.vodafone.gesty.R;
import com.vodafone.gesty.data.AppList;
import com.vodafone.gesty.data.GesturesTable;
import com.vodafone.gesty.ui.ContactPhotoLoader;
import com.vodafone.gesty.ui.GestureSnapshotLoader;

public class GestyApp extends Application {

    private GesturesTable mTable;
    private GestureLibrary mGesturesLib;
    private ContactPhotoLoader mPhotoLoader;
    private static final String INTERNAL_STORAGE_PATH = "/data/com.vodafone.gesty/files/";
    private static final String INTERNAL_STORAGE_FILE = "gestures";

    private AppList mAppList;
    private GestureSnapshotLoader mSnapshotLoader;
	public static int DIALOG_CODE = 1637;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mAppList = new AppList(this);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.avatar);
    }

    
    /**
     * Perform query on package manager for list items.  The default
     * implementation queries for activities.
     */
    public List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        return getPackageManager().queryIntentActivities(queryIntent, /* no flags */ 0);
    }
    
    public GesturesTable getDatabaseTable() {
        if (mTable == null) {
            mTable = new GesturesTable(this);
        }
        return mTable;
    }
    
    public GestureLibrary getGesturesLibrary() {
        if (mGesturesLib == null) {
            File storeFile = new File(Environment.getDataDirectory().getAbsolutePath() + INTERNAL_STORAGE_PATH, INTERNAL_STORAGE_FILE);
            mGesturesLib = GestureLibraries.fromFile(storeFile);
            mGesturesLib.load();
        }
        return mGesturesLib;
    }

    /**
     * Get the Contact Photo Loader reference
     * @return ContactPhotoLoader reference
     */
    public final ContactPhotoLoader getContactPhotoLoader() {
        return mPhotoLoader;
    }
    

    /**
     * Get the Gesture Snapshot Loader reference
     * @return ContactPhotoLoader reference
     */
	public GestureSnapshotLoader getGestureSnapshotLoader() {
		return mSnapshotLoader;
	}

	/**
     * Get the AppList reference
     * @return AppList reference
     */
	public AppList getAppList() {
		return mAppList;
	}
    
}
