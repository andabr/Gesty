package com.vodafone.gesty.data;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;

/**
 * Class used to read Native Address Book Contact data
 */
public class NativeContacts {	
	
	public static final String TAG = "NativeContacts";
	private static final String CONTACTS_DISPLAY_NAME_ALT = "display_name_alt"; 

	/**
	 * Projections we use to access NAB
	 */
	public interface Projections {
		/** Projection for retrieving contact level data + phone numbers */
		final String[] CONTACT_DATA = new String[] {
				
		        Data.CONTACT_ID,	// CONTACT_DATA_CONTACT_ID
				Data.PHOTO_ID,		// CONTACT_DATA_PHOTO_ID
				Phone.NUMBER,		// CONTACT_DATA_NUMBER
				Data.DISPLAY_NAME,	// CONTACT_DATA_NAME
                Phone.TYPE};
		
		public static final int CONTACT_DATA_CONTACT_ID = 0;
		public static final int CONTACT_DATA_PHOTO_ID = 1;
		public static final int CONTACT_DATA_NUMBER = 2;
		public static final int CONTACT_DATA_NAME = 3;
		public static final int CONTACT_DATA_NUMBER_TYPE = 4;

		/** Projection for retrieving contact level data */		
		final String[] CONTACT = new String[] {
				ContactsContract.Contacts._ID,			// CONTACT_ID
				ContactsContract.Contacts.PHOTO_ID, 	// CONTACT_PHOTO_ID
				ContactsContract.Contacts.DISPLAY_NAME,	// CONTACT_NAME
				ContactsContract.Contacts.STARRED       // FAVOURITES
				};
		
		/** Projection for retrieving contact level data */		
		final String[] SEARCH_CONTACT = new String[] {
				ContactsContract.Contacts._ID,			// CONTACT_ID
				ContactsContract.Contacts.PHOTO_ID, 	// CONTACT_PHOTO_ID
				ContactsContract.Contacts.DISPLAY_NAME,	// CONTACT_NAME
				CONTACTS_DISPLAY_NAME_ALT,
				ContactsContract.Contacts.STARRED       // FAVOURITES
				};
		public static final int CONTACT_ID = 0;
		public static final int CONTACT_PHOTO_ID = 1;
		public static final int CONTACT_NAME = 2;
		public static final int CONTACT_NAME_ALT = 3;

		
		/** Projection for retrieving phone numbers */
		final String[] PHONE_NUMBER = new String[] {
				Phone.IS_SUPER_PRIMARY,  // PHONE_NUMBER_IS_PRIMARY
				Phone.TYPE,   			// PHONE_NUMBER_TYPE
				Phone.NUMBER, 		    // PHONE_NUMBER_VALUE
				Phone.LABEL, 		    // PHONE_NUMBER_LABEL
				Phone._ID               // phone detail id
				};
		public static final int PHONE_NUMBER_IS_PRIMARY = 0;
		public static final int PHONE_NUMBER_TYPE = 1;
		public static final int PHONE_NUMBER_VALUE = 2;
		public static final int PHONE_NUMBER_LABEL = 3;
		public static final int PHONE_DETAIL_ID = 4;
		/** Projection for retrieving all email addresses **/
		final String[] EMAIL_ADRESSES = new String[] {
				Email.IS_SUPER_PRIMARY, // EMAIL_IS_PRIMARY
				Email.TYPE,				// EMAIL_TYPE
				Email.LABEL,			// EMAIL_LABEL
				Email.DATA,				// EMAIL_VALUE
				Email._ID               // email detail id
				};
		public static final int EMAIL_IS_PRIMARY = 0;
		public static final int EMAIL_TYPE = 1;
		public static final int EMAIL_LABEL = 2;
		public static final int EMAIL_VALUE = 3;
		public static final int EMAIL_DETAIL_ID = 4;
	}
	
	/** Default sort order string for contacts - sort by contact display name */
	private static final String DEFAULT_CONTACT_SORT_ORDER = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
	
	/**
	 * Retrieves a contact including display name, photo id and phone numbers and email addresses.
	 * @param resolver The ContentResolver to be used
	 * @return The retrieved Contact, if none is found null is returned
	 */
	public static Contact getContact(ContentResolver resolver, long contactId) {
        
        Contact contact = null;
		 // Fetching only phone number data in addition to contact level info
		Cursor cursor = null;
		try {
		    cursor = resolver.query(
		    		Data.CONTENT_URI,
                    Projections.CONTACT_DATA,
                    Data.CONTACT_ID +"= ?",
                    new String[] {String.valueOf(contactId)}, 
                    DEFAULT_CONTACT_SORT_ORDER);
		    if (cursor.getCount() == 0) {
			 	Log.e(TAG+".getContact(contactId)", "Contact "+contactId+" not found!");
				return null; 
			}
			while (cursor.moveToNext()) {
				final long contactIdFromCursor = cursor.getLong(Projections.CONTACT_DATA_CONTACT_ID);
				if (contactIdFromCursor != contactId) {
					// Something wrong here, just return null
					Log.e(TAG+".getContact(contactId)", "There was a contact id mismatch!");
					return null;
				}

				if (cursor.isFirst()) {
					final long photoId = cursor.getLong(Projections.CONTACT_DATA_PHOTO_ID);
					final String displayName = cursor.getString(Projections.CONTACT_DATA_NAME);
					contact = new Contact(contactId, photoId, displayName);
				}
			}
			readContactPhoneNumbers(resolver, contact);
			readContactEmails(resolver, contact);
		 } finally {
			 if(cursor != null) {
				 cursor.close();
				 cursor = null;
			 }
		 }
		 return contact;
	}
	
	/**
	 * Method that returns a contact cursor directly from the native database
	 * @param resolver Content resolver to use
	 * @return Cursor Contacts cursor
	 */
	public static Cursor getContactsCursor(ContentResolver resolver, boolean onlyFavs) {
	    StringBuffer whereClause =  new StringBuffer();
	    whereClause.append("(").append(Contacts.HAS_PHONE_NUMBER).append("=?");
	    String inClause = getContactIdsWithEmails(resolver);
        if (inClause != null) {
	        whereClause.append(" OR ").append(ContactsContract.Contacts._ID).append(inClause);
	    }
        whereClause.append(")");
        if (onlyFavs) {
        	whereClause.append(" AND ").append(Contacts.STARRED).append("=?");
        }
	    return resolver.query(ContactsContract.Contacts.CONTENT_URI, 
                Projections.CONTACT, 
                whereClause.toString(), 
                onlyFavs? new String[]{"1", "1"}: new String[]{"1"}, 
				DEFAULT_CONTACT_SORT_ORDER);
	}
	
	/**
	 * Method that returns a contact cursor directly from the native database
	 * @param resolver Content resolver to use
	 * @return Cursor Contacts cursor
	 */
	public static Cursor getSearchContactsCursor(ContentResolver resolver, boolean onlyFavs, String constraint) {
	    StringBuffer whereClause =  new StringBuffer();
	    whereClause.append("(").append(Contacts.HAS_PHONE_NUMBER).append("=1");
	    String inClause = getContactIdsWithEmails(resolver);
        if (inClause != null) {
	        whereClause.append(" OR ").append(ContactsContract.Contacts._ID).append(inClause);
	    }
        whereClause.append(")");
        if (onlyFavs) {
        	whereClause.append(" AND ").append(Contacts.STARRED).append("=1");
        }
        if (!TextUtils.isEmpty(constraint)) {
        	whereClause.append(" AND (").append(Contacts.DISPLAY_NAME).append(" LIKE '").append(constraint).append("%'").
    		append(" OR ").append(CONTACTS_DISPLAY_NAME_ALT).append(" LIKE '").append(constraint).append("%' )");
        }
        
	    return resolver.query(ContactsContract.Contacts.CONTENT_URI, 
                Projections.SEARCH_CONTACT, 
                whereClause.toString(), 
                null, 
				DEFAULT_CONTACT_SORT_ORDER);
	}
	
	/**
	 * This method fills in email information into the provided contact.
	 * @param resolver - ContentResolver 
	 * @param contact - Contact
	 */
	public static void readContactEmails(ContentResolver resolver, Contact contact) {
	    if (contact == null) {
	        return;
	    }
	    Cursor cursor = null;
	    try {
	        cursor = resolver.query(Email.CONTENT_URI,
	                Projections.EMAIL_ADRESSES,
	                Data.CONTACT_ID + "=?" + " AND "
	                        + Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'",
	                new String[] {String.valueOf(contact.getId())}, null);
	        if (cursor != null) {
	            String value = null;
	            int type = 0;
	            String label = null;
	            boolean isPrimary;
	            long detailId = 0;
	            while (cursor.moveToNext()) {
	                value = cursor.getString(Projections.EMAIL_VALUE);
	                type = cursor.getInt(Projections.EMAIL_TYPE);
	                label = cursor.getString(Projections.EMAIL_LABEL);
	                isPrimary = cursor.getInt(Projections.EMAIL_IS_PRIMARY) > 0;
	                detailId = cursor.getInt(Projections.EMAIL_DETAIL_ID);
	                contact.addEmail(new ContactDetail(ContactDetail.KIND_EMAIL, type, label, value, isPrimary, detailId));
	            }
	        }
	    } catch(Exception exc) {
	        exc.printStackTrace();
	    } finally {
	        if (cursor != null) {
	            cursor.close();
	            cursor = null;
	        }
	    }
	}
	
	/**
     * This method fills in email information into the provided contact.
     * @param resolver - ContentResolver 
     * @param contact - Contact
     */
    private static String getContactIdsWithEmails(ContentResolver resolver) {

        StringBuffer ret = new StringBuffer(" IN (");
        ArrayList<Long> contactIds = new ArrayList<Long>();
        Cursor cursor = null;
        try {
            cursor = resolver.query(Email.CONTENT_URI,
                    new String[] {Data.CONTACT_ID},
                    Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'",
                    null, null);
            if (cursor != null) {
                Long value = null;
                while (cursor.moveToNext()) {
                    value = cursor.getLong(0);
                    if (!contactIds.contains(value)) {
                        contactIds.add(value);
                        ret.append("'").append(value).append("',");
                    }
                }
            }
            if (!contactIds.isEmpty()) {
                //delete the trailing comma
                ret.setLength(ret.length()-1);
                ret.append(")");
            } else {
                return null;
            }
        } catch(Exception exc) {
            exc.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return ret.toString();
    }
	
	
	/**
	 * Reads contact's phone numbers from db putting them into the Contact object
	 * @param contact Contact to read phone numbers for
	 */
	public static void readContactPhoneNumbers(final ContentResolver resolver,
                                              final Contact contact) {
		if (contact == null) {
			return;
		}
		Cursor cursor = null;
		try {
		    cursor = resolver.query(Phone.CONTENT_URI,
		           Projections.PHONE_NUMBER,
		           Data.CONTACT_ID + "=?" + " AND "
		                 + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
		           new String[] {String.valueOf(contact.getId())}, null);
		        
		    if (cursor == null) {
		        return;
		    }
		    while (cursor.moveToNext()) {
                final String number = cursor.getString(Projections.PHONE_NUMBER_VALUE);
                final int type = cursor.getInt(Projections.PHONE_NUMBER_TYPE);
                final boolean isPrimary = cursor.getInt(Projections.PHONE_NUMBER_IS_PRIMARY) > 0;
                final String label = cursor.getString(Projections.PHONE_NUMBER_LABEL);
                final long id = cursor.getLong(Projections.PHONE_DETAIL_ID);
                contact.addPhoneNumber(new ContactDetail(ContactDetail.KIND_PHONE, type, label, number, isPrimary, id));
            }
		} catch(Exception exc) {
		    exc.printStackTrace();
		} finally {
		    if (cursor != null) {
                cursor.close();
                cursor = null;
            }
		}
	}
	
}
