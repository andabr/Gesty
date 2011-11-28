package com.andreid.gesty.data;


public class GestureItem {
	private int actionId;
	private long cid;
	private long did;
	private String gestureId;
	
	private String name;
	private String actionValue;
	private long photoId;
	
	private String mPackage;
	private String mClassName;
	
	
	public GestureItem(int actionId, long cid, long did, String gestureId) {
		super();
		this.actionId = actionId;
		this.cid = cid;
		this.did = did;
		this.gestureId = gestureId;
	}
	
	public String getName() {
		return name;
	}

	public int getActionId() {
		return actionId;
	}
	public long getCid() {
		return cid;
	}
	public long getDid() {
		return did;
	}
	public String getGestureId() {
		return gestureId;
	}
	public String getContactDetailValue() {
		return actionValue;
	}
	public String getActionTitle() {
		
		String actionTitle = null;
    	switch (actionId) {
        case GestureDetail.ACTION_CALL:
            actionTitle = " call " + actionValue;
            break;
        case GestureDetail.ACTION_TEXT:
            actionTitle = " text " + actionValue;
            break;
        case GestureDetail.ACTION_VIEW_CONTACT:
        	actionTitle = " view contact profile";
            break;
        case GestureDetail.ACTION_EMAIL:
        	actionTitle = " email " + actionValue;
            break;
        case GestureDetail.ACTION_BROWSE_WEB:
        	actionTitle = " open in browser ";
        	break;
        case GestureDetail.ACTION_LAUNCH_APP:
        	actionTitle = " launch " + actionValue;
            break;
        default:
            break;
    	}
		return actionTitle;
	}
	public void setContactDetailValue(String actionValue) {
		this.actionValue = actionValue;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getAvatarId() {
		return photoId;
	}
	public void setAvatarId(long avatarId) {
		this.photoId = avatarId;
	}

	public String getPackage() {
		return mPackage;
	}

	public void setPackage(String packageName) {
		this.mPackage = packageName;
	}

	public String getClassName() {
		return mClassName;
	}

	public void setClassName(String className) {
		this.mClassName = className;
	}

	@Override
	public String toString() {
		return "GestureItem [actionId=" + actionId + ", cid=" + cid + ", did="
				+ did + ", gestureId=" + gestureId + ", name=" + name
				+ ", actionValue=" + actionValue + ", photoId=" + photoId
				+ ", mPackage=" + mPackage + ", mClassName=" + mClassName + "]";
	}
	
	
	
}