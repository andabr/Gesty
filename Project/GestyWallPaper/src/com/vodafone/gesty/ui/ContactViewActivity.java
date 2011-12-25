package com.vodafone.gesty.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureLibrary;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.vodafone.gesty.R;
import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.data.Contact;
import com.vodafone.gesty.data.ContactDetail;
import com.vodafone.gesty.data.GestureDetail;
import com.vodafone.gesty.data.GesturesTable;
import com.vodafone.gesty.data.NativeContacts;

public class ContactViewActivity extends BasicMenuActivity {
	private ListView mList;
    private ContactViewAdapter mAdapter;
    private long mContactId;
    private Contact mContact;
    private TextView mNameText;
    private ArrayList<ContactDetailHolder> mContactDetails;
    private ImageView mContactPicture;
    private ContactPhotoLoader mPhotoLoader;
    
    private Handler mRefreshHandler = new Handler() {
    	public void handleMessage(android.os.Message msg) {
    		mAdapter.notifyDataSetChanged();
    	}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);      

        setContentView(R.layout.contactview);
 
        mList = getListView();
        mList.setItemsCanFocus(false);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mContactId = bundle.getLong(Constants.CONTACT_ID);
        mNameText = (TextView)findViewById(R.id.contactname);
        mContactPicture = (ImageView)findViewById(R.id.contactpicture);
        mPhotoLoader = getGestyApp().getContactPhotoLoader();
        mContactDetails = new ArrayList<ContactDetailHolder>();
        mAdapter = new ContactViewAdapter(this);
        mList.setAdapter(mAdapter);
    }
    
	private GestyApp getGestyApp() {
	    return (GestyApp)getApplication();
	}
    
    @Override
    protected void onResume() {
        super.onResume();

        refresh();

    }
    
    
    private static class ContactDetailHolder {
    	
    	private ContactDetail mDetail;
    	private int mAction;
    	
    	public ContactDetailHolder(ContactDetail detail, int action) {
    		mAction = action;
    		mDetail = detail;
    	}

		public ContactDetail getDetail() {
			return mDetail;
		}

		public int getAction() {
			return mAction;
		}
    }
    
    private void refresh() {
    	mContact = NativeContacts.getContact(this.getContentResolver(), mContactId);
        if (mContact != null) {
        	GesturesTable table = ((GestyApp)getApplication()).getDatabaseTable();
            table.readGesturesForContact(mContact);
             
            mNameText.setText(mContact.getDisplayName());
             
            mContactDetails.clear();
            if (mContact.hasPhoneNumbers()) {
            	for (ContactDetail detail: mContact.getPhoneNumbers()) {
                    mContactDetails.add(new ContactDetailHolder(detail, GestureDetail.ACTION_CALL));
                    mContactDetails.add(new ContactDetailHolder(detail, GestureDetail.ACTION_TEXT));
            	}
            }
            if (mContact.hasEmails()) {
            	for (ContactDetail detail: mContact.getEmails()) {
                    mContactDetails.add(new ContactDetailHolder(detail, GestureDetail.ACTION_EMAIL));
            	}
            }
            
            mPhotoLoader.loadPhoto(mContactPicture, mContact.getPhotoId());

            mAdapter.notifyDataSetChanged();
        }
    	
    }
 

    public class ContactViewAdapter extends BaseAdapter {
    	private LayoutInflater mInflater;
        private GestureSnapshotLoader mGestureSnapshotLoader;

        public ContactViewAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final GestureLibrary store = ((GestyApp)getApplication()).getGesturesLibrary();
            mGestureSnapshotLoader = new GestureSnapshotLoader(context, store, mRefreshHandler);

        }

		@Override
		public int getCount() {
			return mContactDetails.size();
		}

		@Override
		public Object getItem(int position) {
			return mContactDetails.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.contactdetail_action_listitem, null);
            } 
			ContactDetailHolder detailHolder = mContactDetails.get(position);
            ContactDetail detail = detailHolder.getDetail();
            
            TextView value = (TextView) convertView.findViewById(R.id.cd_value);
            value.setText(detail.getValue());
            TextView label = (TextView) convertView.findViewById(R.id.cd_label);
            GestureDetail gestureDetail = null;
            switch (detailHolder.getAction()) {
			case GestureDetail.ACTION_EMAIL:
				gestureDetail = detail.getGestureForAction(GestureDetail.ACTION_EMAIL);
	            label.setText(getString(R.string.direct_contact_email));
				break;
			case GestureDetail.ACTION_TEXT:
				gestureDetail = detail.getGestureForAction(GestureDetail.ACTION_TEXT);
	            label.setText(getString(R.string.direct_contact_text));
				break;
			case GestureDetail.ACTION_CALL:
				gestureDetail = detail.getGestureForAction(GestureDetail.ACTION_CALL);
	            label.setText(getString(R.string.direct_contact_phone));
				break;
			default:
				break;
			}
            ImageView img = (ImageView) convertView.findViewById(R.id.cd_image_gesture);
			if (gestureDetail != null) {
	        	Drawable snapshot = mGestureSnapshotLoader.getSnapshotForGestureId(gestureDetail.getGestureId());
//	        	Log.e("Snapshotloader:", gestureDetail.getGestureId() + " for action " + gestureDetail.getActionId());
	        	img.setImageDrawable(snapshot);
            } else {
                img.setImageDrawable(null);    
            }
            if (position == 0 && mList.getCheckedItemPosition() < 0) {
            	mList.setItemChecked(position, true);
            }
            return convertView;
		}
    }
      
	public void createGesture(View v) {
		int pos = mList.getCheckedItemPosition();

		ContactDetailHolder holder = mContactDetails.get(pos);
		ContactDetail detail = holder.getDetail();

		Intent intent = new Intent(ContactViewActivity.this, CreateGestureActivity.class);
        intent.putExtra(Constants.CONTACT_DETAIL, detail);
        intent.putExtra(Constants.CONTACT_ID, mContactId);
        intent.putExtra(Constants.CONTACT_DETAIL_VALUE, detail.getValue());
        intent.putExtra(Constants.CONTACT_DETAIL_ID, detail.getId());
		intent.putExtra(Constants.ACTION_ID, holder.getAction());
        intent.putExtra(Constants.PHOTO_ID, mContact.getPhotoId());
        intent.putExtra(Constants.CONTACT_NAME, mContact.getDisplayName());


		GestureDetail gesture = detail.getGestureForAction(holder.getAction());
		if (gesture != null) {
			intent.putExtra(Constants.GESTURE_ID, gesture.getId());
		}

		
		startActivityForResult(intent, GestyApp.DIALOG_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(resultCode);
		if (GestyApp.DIALOG_CODE == requestCode && (resultCode == Activity.RESULT_OK)) {
			finish();
		}
	}
}
