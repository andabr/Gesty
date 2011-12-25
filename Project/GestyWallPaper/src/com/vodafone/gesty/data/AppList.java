package com.vodafone.gesty.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.ui.AppsLauncherActivity.AppListItem;

/**
 * 
 * List of all application installed on the phone
 *
 */
public class AppList {
	private List<AppListItem> mAppList;
	
	public AppList(GestyApp context) {
		super();
		initAppList(context);
	}

    private void initAppList(GestyApp gestyApp) {
    	PackageManager mPackageManager = gestyApp.getPackageManager();
        // Load all matching activities and sort correctly
        Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = gestyApp.onQueryPackageManager(mIntent);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(mPackageManager));
        mAppList = new ArrayList<AppListItem>(list.size());
        for (ResolveInfo resolveInfo: list) {
        	ComponentInfo ci = resolveInfo.activityInfo;
            if (ci == null) { 
         	   ci = resolveInfo.serviceInfo;
            }
            if (ci != null) {
                mAppList.add(new AppListItem(mPackageManager, resolveInfo, null));
            }
        }
    }
    /**
     * 
     * @param packageName
     * @return AppListItem or null, in case the package is not in the list.
     */
    public AppListItem getAppInfoByPackageName(String packageName) {
    	if (mAppList == null || mAppList.isEmpty()) {
    		return null;
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
    		return null;
    	}
    	return mAppList;
    }


}
