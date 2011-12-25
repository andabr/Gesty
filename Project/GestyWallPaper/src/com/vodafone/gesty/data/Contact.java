package com.vodafone.gesty.data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.vodafone.gesty.data.NativeContacts.Projections;

/**
 * 
 * This is an extended representation of a native contact in the address book, 
 * that includes the related native contact details @see {@link ContactDetail}.
 */
public class Contact implements Serializable {
	/** Default serial Version UID */
	private static final long serialVersionUID = 1L;
	/** Logger tag*/
	private static final String TAG = "Contact";
    /** One's own contact ID */
    public static final long OWN_ID = 0;
    /** Contact ID to use when there is none */
    public static final long NO_ID = -1;
    /** Photo ID to be used when there is no Photo for this Contact */
    public static final long NO_PHOTO = 0;
	/** Contact ID */
	protected long mId;
	/** Photo ID */
	protected long mPhotoId;
	/** Display Name */
	protected String mDisplayName;
	/** Phone numbers */
	protected List<ContactDetail> mPhoneNumbers;
	/** Emails */
	protected List<ContactDetail> mEmails;
	/** Invalid contact id*/
	private static final int INVALID_ID = -1;


	/**
	 * Default constructor
	 */
	public Contact() {
		init(NO_ID, NO_PHOTO, null);
	}

	/**
	 * Constructor taking Contact ID and Display Name
	 * @param id
	 * @param displayName
	 */
	public Contact(long id, String displayName) {
		init(id, NO_PHOTO, displayName);
	}

	/**
	 * Constructor taking Native Contact Id, Photo ID and Display Name
	 * @param id
	 * @param photoId
	 * @param displayName
	 */
	public Contact(long id, long photoId, String displayName) {
		init(id, photoId, displayName);
	}
	

	/**
	 * Constructs a base Contact from a Cursor
	 *
	 * @param cursor
	 * @return Contact object or null if the cursor was null, or the contact id in the cursor is -1.
	 */
	public static Contact constructFrom(Cursor cursor) {
		if(cursor == null || cursor.getCount() == 0) {
			Log.e(TAG, "Error constructing contact from cursor - cursor is null or empty!");
			return null;
		}
		final long id = cursor.getLong(Projections.CONTACT_ID);
		if (id == INVALID_ID) {
			return null;
		}
		final long photoId = cursor.getLong(Projections.CONTACT_PHOTO_ID);
		final String name = cursor.getString(Projections.CONTACT_NAME);

		return new Contact(id, photoId, name);
	}

	/**
	 * Initialization method
	 * @param id Contact ID
	 * @param photoId Contact Photo ID
	 * @param displayName Display Name
	 */
	private void init(long id, long photoId, String displayName) {
		mId = id;
		mPhotoId = photoId;
		mDisplayName = displayName;
	}

	public void setId(long id) {
	    this.mId = id;
	}
	
	/**
	 * Gets the Contact Id
	 * @return Id of the contact
	 */
	public long getId() {
		return mId;
	}

	/**
	 * @return The photoId
	 */
	public long getPhotoId() {
		return mPhotoId;
	}

	/**
	 * @param photoId The photoId to set
	 */
	public void setPhotoId(long photoId) {
		mPhotoId = photoId;
	}

	/**
	 * Get display name
	 * @return
	 */
	public String getDisplayName() {
		return mDisplayName;
	}

	/**
	 * Sets display name
	 * @param name
	 */
	public void setDisplayName(String name) {
		mDisplayName = name;
	}

	/**
	 * Gets phone numbers
	 * @return Phone numbers
	 */
	public List<ContactDetail> getPhoneNumbers() {
		return mPhoneNumbers;
	}

	/**
	 * Checks if this contact has any added phone numbers
	 * @return {@code true} if the contact has phone numbers, {@code false} if not
	 */
	public boolean hasPhoneNumbers() {
		return mPhoneNumbers != null && !mPhoneNumbers.isEmpty();
	}
	
	public void addPhoneNumber(ContactDetail number) {
		if (mPhoneNumbers == null) {
			mPhoneNumbers = new ArrayList<ContactDetail>();
		}
		if (!mPhoneNumbers.contains(number)) {
			mPhoneNumbers.add(number);
		}
	}
	
	public void addEmail(ContactDetail email) {
	    if (mEmails == null) {
	        mEmails = new ArrayList<ContactDetail>();
	    }
	    if (!mEmails.contains(email)) {
	    	mEmails.add(email);
	    }
	}
		
	public List<ContactDetail> getEmails() {
	    return mEmails;
	}
		
	/**
	 * Checks if contact has emails
	 * @return {@¢ode true} if contact has emails, {@¢ode false} if not
	 */
	public boolean hasEmails() {
	    return !(mEmails == null || mEmails.isEmpty());
	}
	/**
	 * 
	 * @param actionId @see {@link GestureDetail}
	 * @param detailId
	 * @return ContactDetail by action id and detail id or null if the corresponding detail doesn't exist. 
	 */
	public ContactDetail getContactDetail(int actionId, long detailId) {
	    switch (actionId) {
	        case GestureDetail.ACTION_CALL:
	        case GestureDetail.ACTION_TEXT:
	            for (ContactDetail detail: mPhoneNumbers) {
	                if (detail.getId() == detailId) {
	                    return detail;
	                }
	            }
	            break;
	        case GestureDetail.ACTION_EMAIL:
	            for (ContactDetail detail: mEmails) {
                    if (detail.getId() == detailId) {
                        return detail;
                    }
                }
	            break;
	    }
	    return null;
	}
	
	public void addGesture(GestureDetail gesture) {
	    ContactDetail detail = getContactDetail(gesture.getActionId(), gesture.getId());
	    detail.addGesture(gesture);
	}
	
	@Override
    public String toString() {
        return "Contact [mContactId=" + mId 
        		+ ", mDisplayName=" + mDisplayName
                + ", mPhoneNumbers=" + mPhoneNumbers
                + ", mEmails=" + mEmails
                + ", mPhotoId=" + mPhotoId + "]";
    }
}
