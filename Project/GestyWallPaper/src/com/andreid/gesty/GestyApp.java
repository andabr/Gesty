package com.andreid.gesty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.os.Environment;

import com.andreid.gesty.data.GesturesTable;
import com.andreid.gesty.ui.AppsLauncherActivity.AppListItem;
import com.andreid.gesty.ui.ContactPhotoLoader;
import com.andreid.gesty.ui.GestureSnapshotLoader;

public class GestyApp extends Application {

    private GesturesTable mTable;
    private GestureLibrary mGesturesLib;
    private ContactPhotoLoader mPhotoLoader;
	private List<AppListItem> mAppList;
    private static final String INTERNAL_STORAGE_PATH = "/data/com.andreid.gesty/files/";
    private PackageManager mPackageManager;
    private boolean mFirstWizard;
    private GestureSnapshotLoader mSnapshotLoader;
	public static int DIALOG_CODE = 1637;
    
    @Override
    public void onCreate() {
        super.onCreate();
        initAppList();
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.avatar);
    }

    
    private void initAppList() {
    	mPackageManager = getPackageManager();
        // Load all matching activities and sort correctly
        Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
        List<ResolveInfo> list = onQueryPackageManager(mIntent);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(mPackageManager));
        mAppList = new ArrayList<AppListItem>(list.size());
        String className = null;
        for (ResolveInfo resolveInfo: list) {
        	ComponentInfo ci = resolveInfo.activityInfo;
            if (ci == null) { 
         	   ci = resolveInfo.serviceInfo;
            }
            if (ci != null) {
                className = ci.name;
                if (className.startsWith("com.android.") || className.startsWith("com.example.")
                		||className.startsWith("com.qo.android.")
                		||className.startsWith("com.google.android.gsf.")
                		||className.startsWith("com.google.android.partnersetup.")) {
                	continue;
                }
                mAppList.add(new AppListItem(mPackageManager, resolveInfo, null));

            }

        }
    }
    
    /**
     * Perform query on package manager for list items.  The default
     * implementation queries for activities.
     */
    protected List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        return mPackageManager.queryIntentActivities(queryIntent, /* no flags */ 0);
    }
    
    public GesturesTable getDatabaseTable() {
        if (mTable == null) {
            mTable = new GesturesTable(this);
        }
        return mTable;
    }
    
    public GestureLibrary getGesturesLibrary() {
        if (mGesturesLib == null) {
            File storeFile = new File(Environment.getDataDirectory().getAbsolutePath() + INTERNAL_STORAGE_PATH, "gestures");
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
    
    
    public AppListItem getAppInfoByPackageName(String packageName) {
    	if (mAppList == null || mAppList.isEmpty()) {
    		initAppList();
    	}
    	for (AppListItem item: mAppList) {
    		if (item.packageName.equals(packageName)) {
    			return item;
    		}
    	}
    	return null;
    }
    
    public List<AppListItem> getAppList() {
    	if (mAppList == null || mAppList.isEmpty()) {
    		initAppList();
    	}
    	return mAppList;
    }

	public void setFirstTime(boolean firstWizard) {
		this.mFirstWizard = firstWizard;
	}

	public boolean isFirstWizard() {
		return mFirstWizard;
	}

	public GestureSnapshotLoader getGestureSnapshotLoader() {
		
		return mSnapshotLoader;
	}
    
}
