package com.vodafone.gesty.data;

/**
 * This wrapper represents a gesture to be displayed in the AdapterView.
 *
 */

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