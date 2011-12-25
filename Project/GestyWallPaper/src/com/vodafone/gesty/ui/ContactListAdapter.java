package com.vodafone.gesty.ui;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.vodafone.gesty.R;
import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.data.Contact;
import com.vodafone.gesty.data.NativeContacts;
import com.vodafone.gesty.data.NativeContacts.Projections;

public class ContactListAdapter extends CursorAdapter {
    protected static int MODE_FAVS = 1;
    protected static int MODE_ALL = 0;
	private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Long> mContactsWithGesturesIds;
    /** Contact Photo cached loading */
    private ContactPhotoLoader mPhotoLoader;
    private int mMode;

    public ContactListAdapter(Context context, ContactPhotoLoader photoLoader) {
        super(context, NativeContacts.getContactsCursor(context.getContentResolver(), false));
        mPhotoLoader = photoLoader;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        refresh();
    }

	public void setMode(int mode) {
		this.mMode = mode;
		changeCursor(NativeContacts.getContactsCursor(mContext.getContentResolver(), mMode == MODE_FAVS));
	}

	public int getMode() {
		return mMode;
	}
	
    public void refresh() {
        mContactsWithGesturesIds = ((GestyApp)((ContactListActivity)mContext).getApplication()).
        	getDatabaseTable().getContactIdsWithGestures();
        notifyDataSetChanged();
    }
  
    @Override
	public void bindView(View view, Context context, Cursor cursor) {
        ContactViewHolder holder = (ContactViewHolder)view.getTag();
        holder.populate(getCursor());
	}
    
    /** {@inheritDoc} */
    @Override
    public Contact getItem(final int position) {
        if (getCursor().moveToPosition(position)) {
            return Contact.constructFrom(getCursor());
        } else {
            // the position correspond to a header, not a Contact
            return null;
        }
    }

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
	    View view = mInflater.inflate(R.layout.contactlistitem, null);

        /**
         * Creates a ViewHolder and store references to the two children views
         * we want to bind data to.
         */
        final ContactViewHolder holder = new ContactViewHolder();
        holder.mContactName = (TextView)view.findViewById(R.id.textName);
        holder.mContactImage = (ImageView)view.findViewById(R.id.imageAvatar);
        holder.mGestureImage = (ImageView)view.findViewById(R.id.imageExpand);

        view.setTag(holder);
        
        return view;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View listItem;
	    Cursor c = getCursor();
        if (convertView == null || (convertView.getTag() == null)) {
            /** Unknown type, so generate a new item. **/
            listItem = newView(mContext, c, parent);
            
        } else {
            /** Known type, so recycle the view. **/
            listItem = convertView;
        }

        c.moveToPosition(position);
        bindView(listItem, mContext, c);

        return listItem;
	}
	
    /**
     * Class to store view data to make getView() more efficient.
     */
    private class ContactViewHolder {         

        public TextView mContactName;

        public ImageView mContactImage;

        public ImageView mGestureImage;
        
        public long mContactId;
        
        public void populate(Cursor cursor) {
            mContactId = cursor.getLong(Projections.CONTACT_ID);
            final String displayName = cursor.getString(Projections.CONTACT_NAME);
            setNameText(mContactName, displayName, Constants.NO_NAME);
            final long photoId = cursor.getLong(Projections.CONTACT_PHOTO_ID);
            mPhotoLoader.loadPhoto(mContactImage, photoId);
            if (mContactsWithGesturesIds.contains(mContactId)) {
                mGestureImage.setImageResource(R.drawable.icon_has_gesture);
                mGestureImage.setVisibility(View.VISIBLE);
            } else {
                mGestureImage.setVisibility(View.GONE);
            }
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
    
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            mPhotoLoader.pause();
        } else {
            mPhotoLoader.resume();
        }
    }

}
