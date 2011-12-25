

package com.vodafone.gesty.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.vodafone.gesty.R;
import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.data.GestureDetail;


/**
* Displays a list of all activities which can be performed
* for a given intent. Launches when clicked.
*
*/
public class AppsLauncherActivity extends BasicMenuActivity {
   private IconResizer mIconResizer;
   private ActivityAdapter mAdapter;
   private ArrayList<String> mPackages;
   private Handler mRefreshHandler = new Handler() {
	   public void handleMessage(android.os.Message msg) {
	       mAdapter.notifyDataSetChanged();
	   }
   };

   /**
    * An item in the list
    */
   public static class AppListItem {
       public ResolveInfo resolveInfo;
       public CharSequence label;
       public Drawable icon;
       public String packageName;
       public String className;
       public Bundle extras;
       
       public AppListItem(PackageManager packageManager, ResolveInfo resolveInfo, IconResizer resizer) {
           this.resolveInfo = resolveInfo;
           label = resolveInfo.loadLabel(packageManager);
           ComponentInfo ci = resolveInfo.activityInfo;
           if (ci == null) { 
        	   ci = resolveInfo.serviceInfo;
           }
           if (label == null && ci != null) {
               label = resolveInfo.activityInfo.name;
           }
           
           if (resizer != null) {
               icon = resizer.createIconThumbnail(resolveInfo.loadIcon(packageManager));
           }
           packageName = ci.applicationInfo.packageName;
           className = ci.name;
       }

       public AppListItem() {
       }
   }

   /**
    * Adapter which shows the set of activities that can be performed for a given intent.
    */
   public class ActivityAdapter extends BaseAdapter implements Filterable {
       private ArrayList<AppListItem> mOriginalValues;

       protected final IconResizer mIconResizer;
       protected final LayoutInflater mInflater;
       private GestureSnapshotLoader mGestureSnapshotLoader;


       private Filter mFilter;
       
       public ActivityAdapter(IconResizer resizer) {
           mIconResizer = resizer;
           mInflater = (LayoutInflater) AppsLauncherActivity.this.getSystemService(
                   Context.LAYOUT_INFLATER_SERVICE);
           mOriginalValues = new ArrayList<AppsLauncherActivity.AppListItem>();
           mOriginalValues.addAll(((GestyApp)getApplication()).getAppList().getAppList());
           final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();
           
           mGestureSnapshotLoader = new GestureSnapshotLoader(AppsLauncherActivity.this,
        		   store, mRefreshHandler);

       }

       public Intent intentForPosition(int position) {

    	   if (mOriginalValues.isEmpty()) {
    		   return null;
    	   }
           AppListItem item = mOriginalValues.get(position);           
           Intent intent  = null;
           if (mPackages.contains(item.className) &&
        		   mGestureSnapshotLoader.getSnapshotForGestureId(item.className) != null) {
        	   intent = new Intent(AppsLauncherActivity.this, ViewGestureActivity.class);
        	   intent.putExtra(Constants.GESTURE_ID, item.className);
               intent.putExtra(Constants.PACKAGE_ID, item.packageName);
               intent.putExtra(Constants.CLASS_ID, item.className);
               intent.putExtra(Constants.CONTACT_DETAIL_VALUE, item.className);
               intent.putExtra(Constants.ACTION_ID, GestureDetail.ACTION_LAUNCH_APP);
           } else {
        	   intent = new Intent(AppsLauncherActivity.this, CreateGestureActivity.class);
        	   
               intent.putExtra(Constants.PACKAGE_ID, item.packageName);
               intent.putExtra(Constants.CLASS_ID, item.className);
               intent.putExtra(Constants.CONTACT_DETAIL_VALUE, item.label);
               intent.putExtra(Constants.ACTION_ID, GestureDetail.ACTION_LAUNCH_APP);
           }
        
           
           return intent;
       }

       public AppListItem itemForPosition(int position) {
    	   if (mOriginalValues.isEmpty()) {
    		   return null;
    	   }

           return mOriginalValues.get(position);
       }

       public int getCount() {
           return mOriginalValues.size();
       }

       public Object getItem(int position) {
           return position;
       }

       public long getItemId(int position) {
           return position;
       }

       public View getView(int position, View convertView, ViewGroup parent) {
           View view;
           if (convertView == null) {
               view = mInflater.inflate(
                       R.layout.application_item, parent, false);
           } else {
               view = convertView;
           }
           bindView(view, mOriginalValues.get(position));
           return view;
       }

       private void bindView(View view, AppListItem item) {
           TextView text = (TextView) view;
           text.setText(item.label);
           if (item.icon == null) {
               item.icon = mIconResizer.createIconThumbnail(
                       item.resolveInfo.loadIcon(getPackageManager()));
           }
           Drawable snapshot = mGestureSnapshotLoader.getSnapshotForGestureId(item.className);
           if (mPackages.contains(item.className) && snapshot != null) {
        	   text.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, snapshot, null);
           } else {
               text.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);    
           }
       }
       
       public Filter getFilter() {
           return mFilter;
       }
   }
       
   /**
    * Utility class to resize icons to match default icon size.  
    */
   public static class IconResizer {
       // Code is borrowed from com.android.launcher.Utilities. 
       private int mIconWidth = -1;
       private int mIconHeight = -1;

       private final Rect mOldBounds = new Rect();
       private Canvas mCanvas = new Canvas();
       private Activity mContext;
       
       public IconResizer(Activity activity) {
           mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                   Paint.FILTER_BITMAP_FLAG));
           
           final Resources resources = activity.getResources();
           mIconWidth = mIconHeight = (int) resources.getDimension(
                   android.R.dimen.app_icon_size);
           mContext = activity;
       }

       /**
        * Returns a Drawable representing the thumbnail of the specified Drawable.
        * The size of the thumbnail is defined by the dimension
        * android.R.dimen.launcher_application_icon_size.
        *
        * This method is not thread-safe and should be invoked on the UI thread only.
        *
        * @param icon The icon to get a thumbnail of.
        *
        * @return A thumbnail for the specified icon or the icon itself if the
        *         thumbnail could not be created. 
        */
       public Drawable createIconThumbnail(Drawable icon) {
           int width = mIconWidth;
           int height = mIconHeight;

           final int iconWidth = icon.getIntrinsicWidth();
           final int iconHeight = icon.getIntrinsicHeight();

           if (icon instanceof PaintDrawable) {
               PaintDrawable painter = (PaintDrawable) icon;
               painter.setIntrinsicWidth(width);
               painter.setIntrinsicHeight(height);
           }

           if (width > 0 && height > 0) {
               if (width < iconWidth || height < iconHeight) {
                   final float ratio = (float) iconWidth / iconHeight;

                   if (iconWidth > iconHeight) {
                       height = (int) (width / ratio);
                   } else if (iconHeight > iconWidth) {
                       width = (int) (height * ratio);
                   }

                   final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ?
                               Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                   final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                   final Canvas canvas = mCanvas;
                   canvas.setBitmap(thumb);
                   // Copy the old bounds to restore them later
                   // If we were to do oldBounds = icon.getBounds(),
                   // the call to setBounds() that follows would
                   // change the same instance and we would lose the
                   // old bounds
                   mOldBounds.set(icon.getBounds());
                   final int x = (mIconWidth - width) / 2;
                   final int y = (mIconHeight - height) / 2;
                   icon.setBounds(x, y, x + width, y + height);
                   icon.draw(canvas);
                   icon.setBounds(mOldBounds);
                   icon = new BitmapDrawable(mContext.getResources(), thumb);
               } else if (iconWidth < width && iconHeight < height) {
                   final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                   final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                   final Canvas canvas = mCanvas;
                   canvas.setBitmap(thumb);
                   mOldBounds.set(icon.getBounds());
                   final int x = (width - iconWidth) / 2;
                   final int y = (height - iconHeight) / 2;
                   icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                   icon.draw(canvas);
                   icon.setBounds(mOldBounds);
                   icon = new BitmapDrawable(mContext.getResources(), thumb);
               }
           }

           return icon;
       }
   }

   @Override
   protected void onCreate(Bundle icicle) {
	   requestWindowFeature(Window.FEATURE_NO_TITLE);  
       super.onCreate(icicle);
          
       requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	   setProgressBarIndeterminateVisibility(true);
	   onSetContentView();
	           
	   mIconResizer = new IconResizer(this);
	    
	   registerForContextMenu(getListView());
       
	   mPackages = ((GestyApp)getApplication()).getDatabaseTable().getPackagesWithGestures();
	   mAdapter = new ActivityAdapter(mIconResizer);
	   setListAdapter(mAdapter);
	   getListView().setTextFilterEnabled(true);
   }
   
   @Override
	protected void onResume() {
		super.onResume();
	    setProgressBarIndeterminateVisibility(false);
	    mAdapter.notifyDataSetChanged();
		
	}

   /**
    * Override to call setContentView() with your own content view to
    * customize the list layout.
    */
   protected void onSetContentView() {
       setContentView(R.layout.application_list);
   }

   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
       Intent intent = intentForPosition(position);
       startActivityForResult(intent, GestyApp.DIALOG_CODE);
   }
      
   /**
    * Return the actual Intent for a specific position in our
    * {@link android.widget.ListView}.
    * @param position The item whose Intent to return
    */
   protected Intent intentForPosition(int position) {
       ActivityAdapter adapter = (ActivityAdapter) mAdapter;
       return adapter.intentForPosition(position);
   }
   
   /**
    * Return the {@link AppListItem} for a specific position in our
    * {@link android.widget.ListView}.
    * @param position The item to return
    */
   protected AppListItem itemForPosition(int position) {
       ActivityAdapter adapter = (ActivityAdapter) mAdapter;
       return adapter.itemForPosition(position);
   }
   
   /**
    * Get the base intent to use when running
    * {@link PackageManager#queryIntentActivities(Intent, int)}.
    */
   protected Intent getTargetIntent() {
       return new Intent();
   }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (GestyApp.DIALOG_CODE == requestCode && (resultCode == Activity.RESULT_OK)) {
			setResult(resultCode);
			finish();
		}
	}
}