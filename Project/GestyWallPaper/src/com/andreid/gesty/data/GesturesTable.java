package com.andreid.gesty.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;


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
    private static final String COLUMN_NAME_URL_TITLE = "url_title";
    private static final String COLUMN_NAME_SNAPSHOT = "snapshot";
    
    public static final int COLUMN_INDEX_CID = 1;
    public static final int COLUMN_INDEX_DID = 2;
    public static final int COLUMN_INDEX_ACTION = 3;
    public static final int COLUMN_INDEX_GESTUREID = 4;
    public static final int COLUMN_INDEX_PACKAGE = 5;
    public static final int COLUMN_INDEX_CLASS = 6;
    public static final int COLUMN_INDEX_URL = 7;
    public static final int COLUMN_INDEX_URL_TITLE = 8;
    public static final int COLUMN_INDEX_SNAPSHOT = 9;
    
    
    public GesturesTable(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
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
        append(COLUMN_NAME_URL).append(" TEXT,").
        append(COLUMN_NAME_URL_TITLE).append(" TEXT,").
        append(COLUMN_NAME_SNAPSHOT).append(" BLOB);");

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
    
    public long addUrlGesture(int actionType, String url, String gestureId, String title) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_URL, url);
        cv.put(COLUMN_NAME_ACTION, actionType);
        cv.put(COLUMN_NAME_GESTUREID, gestureId);
        cv.put(COLUMN_NAME_URL_TITLE, title);
        long rowId = getWritableDatabase().insertOrThrow(TABLE_NAME, null, cv);
        return rowId;
    }
    
    public long updateContactGesture(long contactId, long detailId, int actionType, String gestureId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_ACTION, actionType);
        cv.put(COLUMN_NAME_GESTUREID, gestureId);
        StringBuffer where = new StringBuffer(COLUMN_NAME_CID);
        where.append("=? AND ").append(COLUMN_NAME_DID).append("=?");
        long rowId = getWritableDatabase().update(TABLE_NAME, cv, where.toString(), 
                new String[]{String.valueOf(contactId), String.valueOf(detailId)});
                
        return rowId;
    }
    
    public long updateAppGesture(int actionType, String packageName, String className, String gestureId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_ACTION, actionType);
        cv.put(COLUMN_NAME_GESTUREID, gestureId);
        StringBuffer where = new StringBuffer(COLUMN_NAME_PACKAGE);
        where.append("=? AND " + COLUMN_NAME_CLASS + "=?");
        long rowId = getWritableDatabase().update(TABLE_NAME, cv, where.toString(), 
                new String[] {packageName, className});
                
        return rowId;
    }
    
    public long updateUrlGesture(int actionType, String url, String gestureId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_ACTION, actionType);
        cv.put(COLUMN_NAME_GESTUREID, gestureId);
        StringBuffer where = new StringBuffer(COLUMN_NAME_URL);
        where.append("=?");
        long rowId = getWritableDatabase().update(TABLE_NAME, cv, where.toString(), 
                new String[] {url});
                
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
							c.getString(COLUMN_INDEX_URL_TITLE), action, gestureId);
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
    
    public GestureDetail readGestureForApp(String packageName, String className) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);
        Cursor c = null;
        GestureDetail detail = null;
        try {
            c = builder.query(getReadableDatabase(), 
                    null,
                    COLUMN_NAME_PACKAGE + "=? AND " + COLUMN_NAME_CLASS + "=?", 
                    new String[] {packageName, className},
                    null, null, null);
            while (c.moveToNext()) {
               detail = new GestureDetail(c.getInt(COLUMN_INDEX_ACTION),
                        c.getString(COLUMN_INDEX_PACKAGE), 
                        c.getString(COLUMN_INDEX_CLASS), 
                        c.getString(COLUMN_INDEX_GESTUREID));
               break;
            }    
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return detail;
    }
 
    public GestureDetail readGestureForUrl(String url) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);
        Cursor c = null;
        GestureDetail detail = null;
        try {
            c = builder.query(getReadableDatabase(), 
                    null,
                    COLUMN_NAME_URL + "=?", 
                    new String[] {url},
                    null, null, null);
            while (c.moveToNext()) {
               detail = new GestureDetail(c.getString(COLUMN_INDEX_URL), 
                        c.getString(COLUMN_INDEX_URL_TITLE),
                        c.getInt(COLUMN_INDEX_ACTION),
                        c.getString(COLUMN_INDEX_GESTUREID));
               break;
            }    
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return detail;
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
    
    
	public ArrayList<String> getUrlsWithGestures() {
	        
	    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	    builder.setTables(TABLE_NAME);
	    builder.setDistinct(true);
	    ArrayList<String> ret = new ArrayList<String>();
	    Cursor c = null;
	    try {
	        c = builder.query(getReadableDatabase(), 
	                    new String[]{COLUMN_NAME_URL},
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
					gestures.add(item);
				}
			}
			
		} finally {
			if (c != null) {
	            c.close();
	            c = null;
	        }
		}
		return gestures;
	}
	
	private GestureItem readContactGestureItem(Cursor c, Context context) {
		GestureItem gesture = new GestureItem(c.getInt(COLUMN_INDEX_ACTION), 
				c.getLong(COLUMN_INDEX_CID), 
				c.getLong(COLUMN_INDEX_DID), 
				c.getString(COLUMN_INDEX_GESTUREID));
		Contact contact = NativeContacts.getContact(context.getContentResolver(), gesture.getCid());
		gesture.setAvatarId(contact.getPhotoId());
		if (gesture.getActionId() == GestureDetail.ACTION_VIEW_CONTACT) {
			gesture.setContactDetailValue(contact.getDisplayName());
		} else {
			ContactDetail detail = contact.getContactDetail(gesture.getActionId(), gesture.getDid());
			gesture.setContactDetailValue(detail.getValue());
		}
		gesture.setName(contact.getDisplayName());
		return gesture;
	}
	
	private GestureItem readAppGestureItem(Cursor c, Context context) {
		GestureItem gesture = new GestureItem(GestureDetail.ACTION_LAUNCH_APP,
				0,0,
				c.getString(COLUMN_INDEX_GESTUREID));
		String packageName = c.getString(COLUMN_INDEX_PACKAGE);
		if (!TextUtils.isEmpty(packageName)) {
			gesture.setPackage(packageName);
		}
		String className = c.getString(COLUMN_INDEX_CLASS);
		if (!TextUtils.isEmpty(className)) {
			gesture.setClassName(className);
		}
		return gesture;
	}
	
	private GestureItem readUrlGestureItem(Cursor c, Context context) {
		GestureItem gesture = new GestureItem(GestureDetail.ACTION_BROWSE_WEB,
				0,0,
				c.getString(COLUMN_INDEX_GESTUREID));
		String url = c.getString(COLUMN_INDEX_URL);
		String urlTitle = c.getString(COLUMN_INDEX_URL_TITLE);
		
		if (!TextUtils.isEmpty(url)) {
		gesture.setContactDetailValue(url);
		gesture.setName(urlTitle);
		}
		return gesture;
	}
	
	public ArrayList<GestureItem> getContactsGestures(Context context) {
		Cursor c = null;
		ArrayList<GestureItem> gestures = new ArrayList<GestureItem>();
		try {
			c = getGesturesCursor(COLUMN_NAME_CID + " IS NOT NULL", null);
			if (c != null) {
				while (c.moveToNext()) {
					GestureItem gesture = new GestureItem(c.getInt(COLUMN_INDEX_ACTION), 
											c.getLong(COLUMN_INDEX_CID), 
											c.getLong(COLUMN_INDEX_DID), 
											c.getString(COLUMN_INDEX_GESTUREID));
					gestures.add(gesture);
					Contact contact = NativeContacts.getContact(context.getContentResolver(), gesture.getCid());
					gesture.setAvatarId(contact.getPhotoId());
					if (gesture.getActionId() == GestureDetail.ACTION_VIEW_CONTACT) {
						gesture.setContactDetailValue(contact.getDisplayName());
					} else {
						ContactDetail detail = contact.getContactDetail(gesture.getActionId(), gesture.getDid());
						gesture.setContactDetailValue(detail.getValue());
					}
					gesture.setName(contact.getDisplayName());
				}					
			}
		} finally {
			if (c != null) {
	            c.close();
	            c = null;
	        }
		}
		return gestures;
	}
	
	public ArrayList<GestureItem> getAppsGestures(Context context) {
		Cursor c = null;
		ArrayList<GestureItem> gestures = new ArrayList<GestureItem>();
		try {
			c = getGesturesCursor(COLUMN_NAME_ACTION + "=?", new String[] {String.valueOf(GestureDetail.ACTION_LAUNCH_APP)});
			if (c != null) {
				while (c.moveToNext()) {
					GestureItem gesture = new GestureItem(GestureDetail.ACTION_LAUNCH_APP,
											0,0,
											c.getString(COLUMN_INDEX_GESTUREID));
					gestures.add(gesture);
					String packageName = c.getString(COLUMN_INDEX_PACKAGE);
					if (!TextUtils.isEmpty(packageName)) {
						gesture.setPackage(packageName);						
					}
				}					
			}
		} finally {
			if (c != null) {
	            c.close();
	            c = null;
	        }
		}
		return gestures;
	}
	
	public ArrayList<GestureItem> getURLsGestures(Context context, PackageManager packMan, Intent queryIntent) {
		Cursor c = null;
		ArrayList<GestureItem> gestures = new ArrayList<GestureItem>();
		try {
			c = getGesturesCursor(COLUMN_NAME_ACTION + "=?", new String[] {String.valueOf(GestureDetail.ACTION_BROWSE_WEB)});
			if (c != null) {
				while (c.moveToNext()) {
					GestureItem gesture = new GestureItem(GestureDetail.ACTION_BROWSE_WEB,
											0,0,
											c.getString(COLUMN_INDEX_GESTUREID));
					gestures.add(gesture);
					String url = c.getString(COLUMN_INDEX_URL);
					String urlTitle = c.getString(COLUMN_INDEX_URL_TITLE);
					
					if (!TextUtils.isEmpty(url)) {
						gesture.setContactDetailValue(url);
						gesture.setName(urlTitle);
					}
				}					
			}
		} finally {
			if (c != null) {
	            c.close();
	            c = null;
	        }
		}
		return gestures;
	}
	
	
	public Cursor getAllContactGestures() {
		return getGesturesCursor(COLUMN_NAME_CID + " IS NOT NULL", null);
	}
	public Cursor getAllUrlGestures() {
		return getGesturesCursor(COLUMN_NAME_ACTION + "=?", new String[] {String.valueOf(GestureDetail.ACTION_BROWSE_WEB)});
	}
}
