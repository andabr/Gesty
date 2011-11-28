package com.andreid.gesty.data;

import java.io.Serializable;

public class GestureDetail implements Serializable {

    /**
     * Generated id.
     */
    private static final long serialVersionUID = -4597422912249786629L;
    
    public static final int ACTION_CALL = 1;
    public static final int ACTION_TEXT = 2;
    public static final int ACTION_EMAIL = 3;
    public static final int ACTION_BROWSE_WEB = 4;
    public static final int ACTION_VIEW_CONTACT = 5;
    public static final int ACTION_LAUNCH_APP = 6;
    
    
    private long mContactId;
    private long mDetailId;
    private int mActionId;
    private String mGestureId;
    private String mPackageName;
    private String mUrl;
    private String mUrlTitle;

    private String mClassName;
    
    public GestureDetail(long contactId, long detailId, int actionId, String gestureId) {
        super();
        this.mContactId = contactId;
        this.mDetailId = detailId;
        this.mActionId = actionId;
        this.mGestureId = gestureId;
    }
    
    public GestureDetail(int actionId, String packageName, String className, String gestureId) {
        super();
        this.mPackageName = packageName;
        this.mActionId = actionId;
        this.mGestureId = gestureId;
        this.mClassName = className;
    }
    
    public GestureDetail(String url, String title, int actionId, String gestureId) {
        super();
        this.mUrl = url;
        this.mActionId = actionId;
        this.mGestureId = gestureId;
        this.mUrlTitle = url;
    }

    public GestureDetail() {
        super();
    }

    public long getContactId() {
        return mContactId;
    }

    public void setContactId(long contactId) {
        this.mContactId = contactId;
    }

    public long getDetailId() {
        return mDetailId;
    }

    public void setDetailId(long detailId) {
        this.mDetailId = detailId;
    }

    public int getActionId() {
        return mActionId;
    }

    public void setActionId(int actionId) {
        this.mActionId = actionId;
    }

    public void setGestureId(String gestureId) {
        this.mGestureId = gestureId;
    }

    public String getGestureId() {
        return mGestureId;
    }

  	public void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}

	public String getPackageName() {
		return mPackageName;
	}
	
	public void setClassName(String className) {
		this.mClassName = className;
	}

	public String getClassName() {
		return mClassName;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrlTitle(String mUrlTitle) {
		this.mUrlTitle = mUrlTitle;
	}

	public String getUrlTitle() {
		return mUrlTitle;
	}

	@Override
	public String toString() {
		return "GestureDetail [mContactId=" + mContactId + ", mDetailId="
				+ mDetailId + ", mActionId=" + mActionId + ", mGestureId="
				+ mGestureId + ", mPackageName=" + mPackageName + ", mUrl="
				+ mUrl + ", mUrlTitle=" + mUrlTitle + ", mClassName="
				+ mClassName + "]";
	}
		
}
