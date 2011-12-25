package com.vodafone.gesty.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.gesture.GestureLibrary;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.R;
import com.vodafone.gesty.data.GestureDetail;
import com.vodafone.gesty.data.GestureItem;
import com.vodafone.gesty.ui.AppsLauncherActivity.AppListItem;
import com.vodafone.gesty.ui.AppsLauncherActivity.IconResizer;

public class GestureListAdapter extends BaseAdapter {
	public static final int MODE_ALL = 0;
    public static final int MODE_CONTACTS = 1;
    public static final int MODE_APPS = 2;
    public static final int MODE_URLS = 3;
    
    private Activity mContext;
    private LayoutInflater mInflater;
    private ArrayList<GestureItem> mAllGestureItems = new ArrayList<GestureItem>();
    private ArrayList<GestureItem> mCurrentGestureItems = new ArrayList<GestureItem>();
    private ContactPhotoLoader mPhotoLoader;
    private GestureSnapshotLoader mGestureSnapshotLoader;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			notifyDataSetChanged();
		}
	};
    
    private final GestureLibrary store;
    private int mMode;
    private IconResizer mIconResizer;
    private GestyApp mApp;
    
    private Drawable mDefaultUrlPicture;
    private final Drawable mCallIcon, mSmsIcon, mEmailIcon;
    
	public GestureListAdapter(Activity context, ContactPhotoLoader photoLoader) {
		super();
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mApp = (GestyApp) context.getApplication();
        mPhotoLoader = photoLoader;
        
        store = ((GestyApp)context.getApplication()).getGesturesLibrary();
        mGestureSnapshotLoader = new GestureSnapshotLoader(context, store, mHandler);
        mDefaultUrlPicture = context.getResources().getDrawable(R.drawable.icon_url_big);
        mCallIcon = context.getResources().getDrawable(R.drawable.icon_punch_call);
        mSmsIcon = context.getResources().getDrawable(R.drawable.icon_punch_sms);
        mEmailIcon = context.getResources().getDrawable(R.drawable.icon_punch_email);


        mIconResizer = new IconResizer(mContext);
        

	}
	
	public void refreshData() {
		mAllGestureItems = ((GestyApp)mContext.getApplication()).getDatabaseTable().getAllGestureItems(mContext);
		mCurrentGestureItems.clear();
		mGestureSnapshotLoader.clearCache();

		switch (mMode) {
		case MODE_ALL:
			mCurrentGestureItems.addAll(mAllGestureItems);
			break;
		case MODE_APPS:
			for (GestureItem item: mAllGestureItems) {
				if (item.getActionId() == GestureDetail.ACTION_LAUNCH_APP) {
					mCurrentGestureItems.add(item);
				}
			}
			break;
		case MODE_CONTACTS:
			for (GestureItem item: mAllGestureItems) {
				if (item.getCid() > 0) {
					mCurrentGestureItems.add(item);
				}
			}
			break;
		case MODE_URLS:
			for (GestureItem item: mAllGestureItems) {
				if (item.getActionId() == GestureDetail.ACTION_BROWSE_WEB) {
					mCurrentGestureItems.add(item);
				}
			}
			break;
		default:
			break;
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View listItem = null;
        if (convertView == null || (convertView.getTag() == null)) {
            /** Unknown type, so generate a new item. **/
            listItem = newView();
        } else {
            /** Known type, so recycle the view. **/
            listItem = convertView;
        }

        bindView(listItem, mContext, mCurrentGestureItems.get(position));
        return listItem;
	}
	
	public View newView() {
		/** Unknown type, so generate a new item. **/
        View listItem = mInflater.inflate(R.layout.gesture_listitem, null);
        GestureViewHolder holder = new GestureViewHolder();
        holder.mActionTitle = (TextView) listItem.findViewById(R.id.gestureli_textActionDetails);
        holder.mName = (TextView) listItem.findViewById(R.id.gestureli_textName);
        holder.mContactImage = (ImageView) listItem.findViewById(R.id.gestureli_imageAvatar);
        holder.mGestureImage = (ImageView) listItem.findViewById(R.id.gestureli_imageSnapshot);
        holder.mActionIcon = (ImageView) listItem.findViewById(R.id.gestureli_actionImage);
        listItem.setTag(holder);
        return listItem;
	}
	
	public void bindView(View view, Context context, GestureItem item) {
		GestureViewHolder holder = (GestureViewHolder) view.getTag(); 
		holder.populate(item);
	}
	
    /**
     * Class to store view data to make getView() more efficient.
     */
    private class GestureViewHolder {         

        public TextView mName;
        public TextView mActionTitle;
        public ImageView mActionIcon;

        public ImageView mContactImage;
        public ImageView mGestureImage;        
        
        public void populate(GestureItem gestureItem) {
           	switch(gestureItem.getActionId()) {
        	case GestureDetail.ACTION_LAUNCH_APP:
        		AppListItem item = mApp.getAppList().getAppInfoByPackageName(gestureItem.getPackage());
        		if (item != null) {
            		if (item.icon == null) {
            			item.icon = mIconResizer.createIconThumbnail(
                                item.resolveInfo.loadIcon(mContext.getPackageManager()));
            		}
            		mContactImage.setImageDrawable(item.icon);
                	setNameText(mName, item.label.toString(), Constants.NO_NAME);
                	gestureItem.setContactDetailValue(item.label.toString());
                	mActionTitle.setVisibility(View.GONE);
                	mActionIcon.setVisibility(View.GONE);
        		}
        		break;
        	case GestureDetail.ACTION_BROWSE_WEB:
            	setNameText(mName, gestureItem.getName(), Constants.NO_NAME);
            	mActionTitle.setVisibility(View.GONE);
            	mActionIcon.setVisibility(View.GONE);
            	mContactImage.setImageDrawable(mDefaultUrlPicture);
        		break;
        	case GestureDetail.ACTION_CALL:
        		mPhotoLoader.loadPhoto(mContactImage, gestureItem.getAvatarId());
            	setNameText(mName, gestureItem.getName(), Constants.NO_NAME);
            	mActionTitle.setText(gestureItem.getContactDetailValue());
            	mActionTitle.setVisibility(View.VISIBLE);
            	mActionIcon.setImageDrawable(mCallIcon);
            	mActionIcon.setVisibility(View.VISIBLE);
            	break;
        	case GestureDetail.ACTION_EMAIL:
        		mPhotoLoader.loadPhoto(mContactImage, gestureItem.getAvatarId());
            	setNameText(mName, gestureItem.getName(), Constants.NO_NAME);
            	mActionTitle.setText(gestureItem.getContactDetailValue());
            	mActionTitle.setVisibility(View.VISIBLE);     
            	mActionIcon.setImageDrawable(mEmailIcon);
            	mActionIcon.setVisibility(View.VISIBLE);
            	break;
        	case GestureDetail.ACTION_TEXT:
        		mPhotoLoader.loadPhoto(mContactImage, gestureItem.getAvatarId());
            	setNameText(mName, gestureItem.getName(), Constants.NO_NAME);
            	mActionTitle.setText(gestureItem.getContactDetailValue());
            	mActionTitle.setVisibility(View.VISIBLE);
            	mActionIcon.setImageDrawable(mSmsIcon);
            	mActionIcon.setVisibility(View.VISIBLE);
            	break;
        	case GestureDetail.ACTION_VIEW_CONTACT:
        		mPhotoLoader.loadPhoto(mContactImage, gestureItem.getAvatarId());
            	setNameText(mName, gestureItem.getName(), Constants.NO_NAME);
            	mActionTitle.setVisibility(View.GONE);
            	mActionIcon.setVisibility(View.GONE);
            	break;
            	default:
            		
        	}
        	Drawable snapshot = mGestureSnapshotLoader.getSnapshotForGestureId(gestureItem.getGestureId());
        	mGestureImage.setImageDrawable(snapshot);
        }
        
        private void setNameText(final TextView nameTextView,
                final String name, final String noName) {
            if (!TextUtils.isEmpty(name)) {
                nameTextView.setText(name);
            } else {
                nameTextView.setText(noName);
            }
        }
    }

	@Override
	public int getCount() {
		return mCurrentGestureItems.size();
	}

	@Override
	public GestureItem getItem(int position) {
		return mCurrentGestureItems.get(position);
	}
	

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            mPhotoLoader.pause();
        } else {
            mPhotoLoader.resume();
        }
    }
	
	public void setMode(int mode) {
		mMode = mode;
		refreshData();
	}
	
	public void onDestroy() {
//		if (mGestureSnapshotLoader != null) {
//			mGestureSnapshotLoader.destroy();
//		}
	}

}
