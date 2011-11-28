package com.andreid.gesty.data;

public interface IntentsData {

	String CONTACT_DETAIL = "detail";
	String GESTURE = "gesture";
	String GESTURE_RECOGNIZED ="recognized";
	String CONTACT = "contact";
	
	String MODE_DIRECT = "direct";
	String CONTACT_ID = "contactId";
	String ACTION_ID = "actionId";
	String PACKAGE_ID = "package";
	String URL_ID = "openurl";
	String CLASS_ID = "className";
	String CONTACT_DETAIL_ID = "detailId";
	String CONTACT_DETAIL_VALUE = "detailValue";
	String GESTURE_ID = "gesture_id";
	
	
    public static final String PREFS_NAME = "com.andreid.gesty";
    public static final String PREF_BG_PIC = "backgroundpicture";

    public static final String LIST_ALL_GESTURES_ACTION =
    	"com.andreid.gesty.action.LIST_ALL_APPS";

    public static final String LIST_ALL_CONTACTS_ACTION =
    	"com.andreid.gesty.action.LIST_ALL_CONTACTS";

    public static final String LIST_ALL_APPS_ACTION =
    	"com.andreid.gesty.action.LIST_ALL_APPS";
    
    public static final String LIST_ALL_URLS_ACTION =
    	"com.andreid.gesty.action.LIST_ALL_CONTACTS";

}
