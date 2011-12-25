package com.vodafone.gesty.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.vodafone.gesty.GestyApp;
import com.vodafone.gesty.ui.AppsLauncherActivity.AppListItem;


public class GesturesTable extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "gesturesmapping.db";
    private static final String TABLE_NAME = "gesturesmap";
    private static final int SCHEMA_VERSION = 1;
    
    private static final String COLUMN_NAME_CID = "contactid";
    private static final String COLUMN_NAME_DID = "detailid";
    private static final String COLUMN_NAME_ACTION = "actionid";
    private static final String COLUMN_NAME_GESTUREID = "gestureid";
    private static final String COLUMN_NAME_PACKAGE = "package";
    private static final String COLUMN_NAME_CLASS = "class";
    private static final String COLUMN_NAME_URL = "url";
    
    public static final int COLUMN_INDEX_CID = 1;
    public static final int COLUMN_INDEX_DID = 2;
    public static final int COLUMN_INDEX_ACTION = 3;
    public static final int COLUMN_INDEX_GESTUREID = 4;
    public static final int COLUMN_INDEX_PACKAGE = 5;
    public static final int COLUMN_INDEX_CLASS = 6;
    public static final int COLUMN_INDEX_URL = 7;
    
    private GestyApp mGestyApp;
    
	private ArrayList<GestureItem> mGesturesToDelete = new ArrayList<GestureItem>();

    
    
    public GesturesTable(GestyApp context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
        mGestyApp = context;
    }

    @Override
    public void onCreate(SQLiteDatabase writableDb) {
        StringBuffer query = new StringBuffer("CREATE TABLE ");
        query.append(TABLE_NAME).append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT, ").
        append(COLUMN_NAME_CID).append(" LONG, ").
        append(COLUMN_NAME_DID).append(" LONG, ").
        append(COLUMN_NAME_ACTION).append(" INTEGER, ").
        append(COLUMN_NAME_GESTUREID).append(" TEXT,").
        append(COLUMN_NAME_PACKAGE).append(" TEXT,").
        append(COLUMN_NAME_CLASS).append(" TEXT,").
        append(COLUMN_NAME_URL).append(" TEXT);");

        writableDb.execSQL(query.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase writableDb, int oldVersion, int newVersion) {
        Log.w("GesturesTable", "Upgrading database, which will destroy all old data");
        writableDb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(writableDb);
    }
    
    public long addContactGesture(long contactId, long detailId, int actionType, String gestureId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_CID, contactId);
        cv.put(COLUMN_NAME_DID, detailId);
        cv.put(COLUMN_NAME_ACTION, actionType);
        cv.put(COLUMN_NAME_GESTUREID, gestureId);
        long rowId = getWritableDatabase().insertOrThrow(TABLE_NAME, null, cv);
        return rowId;
    }
    
    public long addAppGesture(int actionType, String packageName, String className, String gestureId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_PACKAGE, packageName);
        cv.put(COLUMN_NAME_CLASS, className);
        cv.put(COLUMN_NAME_ACTION, actionType);
        cv.put(COLUMN_NAME_GESTUREID, gestureId);
        long rowId = getWritableDatabase().insertOrThrow(TABLE_NAME, null, cv);
        return rowId;
    }
    
    public long addUrlGesture(int actionType, String url, String gestureId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_URL, url);
        cv.put(COLUMN_NAME_ACTION, actionType);
        cv.put(COLUMN_NAME_GESTUREID, gestureId);
        long rowId = getWritableDatabase().insertOrThrow(TABLE_NAME, null, cv);
        return rowId;
    }
    
    public int deleteContactGesture(long contactId, long detailId, int actionType) {
        String whereClause = COLUMN_NAME_CID +"=? AND " + COLUMN_NAME_DID + "=? AND " + COLUMN_NAME_ACTION + "=?";
        int rowCount = getWritableDatabase().delete(TABLE_NAME, whereClause, 
                new String[]{String.valueOf(contactId), String.valueOf(detailId), String.valueOf(actionType)});
        return rowCount;
    }
    
    public int deleteAppGesture(String packageName, String className) {
        String whereClause = COLUMN_NAME_PACKAGE +"=? AND " + COLUMN_NAME_CLASS + "=?";
        int rowCount = getWritableDatabase().delete(TABLE_NAME, whereClause, 
                new String[]{packageName, className});
        return rowCount;
    }
    
    public int deleteGestureById(String gestureId) {
        String whereClause = COLUMN_NAME_GESTUREID +"=?";
        int rowCount = getWritableDatabase().delete(TABLE_NAME, whereClause, 
                new String[]{gestureId});
        mGestyApp.getGesturesLibrary().removeEntry(gestureId);
        return rowCount;
    }
    
    public int deleteUrlGesture(String url) {
        String whereClause = COLUMN_NAME_URL +"=?";
        int rowCount = getWritableDatabase().delete(TABLE_NAME, whereClause, 
                new String[]{url});
        return rowCount;
    }
    
    public GestureDetail getGesture(String gestureId) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);
        Cursor c = null;
        try {
            c = builder.query(getReadableDatabase(), null,
                    COLUMN_NAME_GESTUREID + "=?", new String[]{gestureId}, null, null, null);
            if (c.moveToFirst()) {
            	int action = c.getInt(COLUMN_INDEX_ACTION);
            	switch (action) {
				case GestureDetail.ACTION_LAUNCH_APP:
                    return new GestureDetail(action, c.getString(COLUMN_INDEX_PACKAGE), 
                    		c.getString(COLUMN_INDEX_CLASS), gestureId);
				case GestureDetail.ACTION_BROWSE_WEB:
					return new GestureDetail(c.getString(COLUMN_INDEX_URL),
							action, gestureId);
				default:
					return new GestureDetail(c.getLong(COLUMN_INDEX_CID),
                            c.getLong(COLUMN_INDEX_DID), action, gestureId);
				}
            }    
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c= null;
            }
            
        }
        return null;
    }

    public void readGesturesForContact(Contact mContact) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);
        Cursor c = null;
        try {
            c = builder.query(getReadableDatabase(), 
                    null,
                    COLUMN_NAME_CID + "=?", 
                    new String[]{String.valueOf(mContact.getId())},
                    null, null, null);
            while (c.moveToNext()) {
                mContact.addGesture(new GestureDetail(c.getLong(COLUMN_INDEX_CID),
                        c.getLong(COLUMN_INDEX_DID), 
                        c.getInt(COLUMN_INDEX_ACTION), 
                        c.getString(COLUMN_INDEX_GESTUREID)));
            }    
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
    }
    
    
    public ArrayList<Long> getContactIdsWithGestures() {
        
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);
        builder.setDistinct(true);
        ArrayList<Long> ret = new ArrayList<Long>();
        Cursor c = null;
        try {
            c = builder.query(getReadableDatabase(), 
                    new String[]{COLUMN_NAME_CID},
                    null, null, null, null, null);
            while (c.moveToNext()) {
                ret.add(c.getLong(0));
            }  
        } catch(Exception exc) {
            exc.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return ret;
    }
    
    public ArrayList<String> getPackagesWithGestures() {
        
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);
        builder.setDistinct(true);
        ArrayList<String> ret = new ArrayList<String>();
        Cursor c = null;
        try {
            c = builder.query(getReadableDatabase(), 
                    new String[]{COLUMN_NAME_CLASS},
                    null, null, null, null, null);
            while (c.moveToNext()) {
                ret.add(c.getString(0));
            }  
        } catch(Exception exc) {
            exc.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return ret;
    }
    
	
	public Cursor getAllGesturesCursor() {
		return getGesturesCursor(null, null);
	}
	
	public boolean isEmpty() {
		boolean ret = true;
		Cursor c = getGesturesCursor(null, null);
		ret = c == null || c.getCount() == 0;
		if (c != null) {
			c.close();
			c = null;
		}
		return ret;
	}
	
	
	private Cursor getGesturesCursor(String where, String[] whereArgs) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	    builder.setTables(TABLE_NAME);
	    Cursor c = null;
	    try {
	        c = builder.query(getReadableDatabase(), 
	                    null,
	                    where, whereArgs, null, null, null);
	        
	    } catch(Exception exc) {
	        exc.printStackTrace();
	        if (c != null) {
	            c.close();
	            c = null;
	        }
	    } 
	    return c;

	}
	
	public ArrayList<GestureItem> getAllGestureItems(Context context) {
		ArrayList<GestureItem> gestures = new ArrayList<GestureItem>();
		Cursor c = null;
		try {
			c = getAllGesturesCursor();
			if (c != null) {
				mGesturesToDelete.clear();
				while (c.moveToNext()) {
					int actionId = c.getInt(COLUMN_INDEX_ACTION);
					GestureItem item = null;
					switch (actionId) {
					case GestureDetail.ACTION_CALL:
					case GestureDetail.ACTION_EMAIL:
					case GestureDetail.ACTION_TEXT:
					case GestureDetail.ACTION_VIEW_CONTACT:
						item = readContactGestureItem(c, context);
						break;
					case GestureDetail.ACTION_LAUNCH_APP:
						item = readAppGestureItem(c, context);
						break;
					case GestureDetail.ACTION_BROWSE_WEB:
						item = readUrlGestureItem(c, context);
						break;
					default:
						break;
					}
					if (item != null) {
						gestures.add(item);
					}
				}
				deleteMarkedGestures();
			}
		} finally {
			if (c != null) {
	            c.close();
	            c = null;
	        }
		}
		return gestures;
	}
	
	private void deleteMarkedGestures() {
		for (GestureItem item: mGesturesToDelete) {
			deleteGestureById(item.getGestureId());
		}
	}

	/**
	 * @param c
	 * @param context
	 * @return
	 */
	private GestureItem readContactGestureItem(Cursor c, Context context) {
		GestureItem gesture = new GestureItem(c.getInt(COLUMN_INDEX_ACTION), 
				c.getLong(COLUMN_INDEX_CID), 
				c.getLong(COLUMN_INDEX_DID), 
				c.getString(COLUMN_INDEX_GESTUREID));
		Contact contact = NativeContacts.getContact(context.getContentResolver(), gesture.getCid());
		if (contact != null) {
			gesture.setAvatarId(contact.getPhotoId());
			if (gesture.getActionId() == GestureDetail.ACTION_VIEW_CONTACT) {
				gesture.setContactDetailValue(contact.getDisplayName());
			} else {
				ContactDetail detail = contact.getContactDetail(gesture.getActionId(), gesture.getDid());
				if (detail != null) {
					gesture.setContactDetailValue(detail.getValue());
				} else {
					mGesturesToDelete.add(gesture);
					return null;
				}
			}	
		} else {
			mGesturesToDelete.add(gesture);
			return null;

		}
		
		gesture.setName(contact.getDisplayName());
		return gesture;
	}
	
	private GestureItem readAppGestureItem(Cursor c, Context context) {
		String packageName = c.getString(COLUMN_INDEX_PACKAGE);
		AppListItem item = mGestyApp.getAppList().getAppInfoByPackageName(packageName);
		GestureItem gesture = new GestureItem(GestureDetail.ACTION_LAUNCH_APP,
				0,0,
				c.getString(COLUMN_INDEX_GESTUREID));

		if (item != null) {
			
			if (!TextUtils.isEmpty(packageName)) {
				gesture.setPackage(packageName);
			}
			String className = c.getString(COLUMN_INDEX_CLASS);
			if (!TextUtils.isEmpty(className)) {
				gesture.setClassName(className);
			}
			
			return gesture;
	
		} else {
			mGesturesToDelete.add(gesture);
			return null;
		}
	}
	
	private GestureItem readUrlGestureItem(Cursor c, Context context) {
		GestureItem gesture = new GestureItem(GestureDetail.ACTION_BROWSE_WEB,
				0,0,
				c.getString(COLUMN_INDEX_GESTUREID));
		String url = c.getString(COLUMN_INDEX_URL);
		
		if (!TextUtils.isEmpty(url)) {
			gesture.setContactDetailValue(url);
			gesture.setName(url);
		}
		return gesture;
	}

}
