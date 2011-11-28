package com.andreid.gesty.data;
import java.io.Serializable;
import java.util.ArrayList;

import android.database.Cursor;
import android.util.Log;

import com.andreid.gesty.data.NativeContacts.Projections;

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
     /** Statically cached instance of the Me Contact */
	private static final Contact sMeContact = new Contact(OWN_ID, null);
	/** Contact ID */
	protected long mId;
	/** Photo ID */
	protected long mPhotoId;
	/** Display Name */
	protected String mDisplayName;
	/** Phone numbers */
	protected ArrayList<ContactDetail> mPhoneNumbers;
	/** Emails */
	protected ArrayList<ContactDetail> mEmails;
	
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
	 * Utility method that creates the statically cached me Contact 
	 * @return
	 */
	public static Contact getMeContact() {
		return sMeContact;
	}
	
	/**
	 * Checks if this contact is the Me Contact
	 * @return {@code true} if this contact is the Me contact, {@code false} if not
	 */
	public boolean isMeContact() {
		return this == sMeContact;
	}

	/**
	 * Constructs a base Contact from a Cursor
	 *
	 * @param cursor
	 * @return
	 */
	public static Contact constructFrom(Cursor cursor) {
		if(cursor == null || cursor.getCount() == 0) {
			Log.e(TAG, "Error constructing contact from cursor - cursor is null or empty!");
			return null;
		}
		final long id = cursor.getLong(Projections.CONTACT_ID);
		if(id == INVALID_ID) {
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
	public ArrayList<ContactDetail> getPhoneNumbers() {
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
		if(mPhoneNumbers == null) {
			mPhoneNumbers = new ArrayList<ContactDetail>();
		}
		if(!mPhoneNumbers.contains(number)) {
			mPhoneNumbers.add(number);
		}
	}
	
	public void addEmail(ContactDetail email) {
	    if (mEmails == null) {
	        mEmails = new ArrayList<ContactDetail>();
	    }
	    if(!mEmails.contains(email)) {
	    	mEmails.add(email);
	    }
	}
		
	public ArrayList<ContactDetail> getEmails() {
	    return mEmails;
	}
		
	/**
	 * Checks if contact has emails
	 * @return {@¢ode true} if contact has emails, {@¢ode false} if not
	 */
	public boolean hasEmails() {
	    return !(mEmails == null || mEmails.isEmpty());
	}
	
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
	    ContactDetail detail = getContactDetail(gesture.getActionId(), gesture.getDetailId());
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
