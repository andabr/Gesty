package com.vodafone.gesty.data;

import java.io.Serializable;

/**
 * A wrapper that represents a gesture in the model.
 *
 */
public class GestureDetail implements Serializable {

    /**
     * Generated id.
     */
    private static final long serialVersionUID = -4597422912249786629L;
    /**
     * possible actions for a gesture
     */
    public static final int ACTION_CALL = 1;
    public static final int ACTION_TEXT = 2;
    public static final int ACTION_EMAIL = 3;
    public static final int ACTION_BROWSE_WEB = 4;
    public static final int ACTION_VIEW_CONTACT = 5;
    public static final int ACTION_LAUNCH_APP = 6;
    
    
    private long mContactId;
    private long mId;
    private int mActionId;
    private String mGestureId;
    private String mPackageName;
    private String mUrl;
    private String mClassName;
    /**
     * Constructor for a contact GestureDetail.
     * @param contactId - native contact id
     * @param detailId - native detail id
     * @param actionId - action id
     * @param gestureId - gesture id in the GestureLibrary
     */
    public GestureDetail(long contactId, long detailId, int actionId, String gestureId) {
        super();
        this.mContactId = contactId;
        this.mId = detailId;
        this.mActionId = actionId;
        this.mGestureId = gestureId;
    }
    /**
     * Constructor for an application GestureDetail.
     * @param actionId - action id
     * @param packageName - application package name
     * @param className - application class name
     * @param gestureId - gesture id in the GestureLibrary
     */
    public GestureDetail(int actionId, String packageName, String className, String gestureId) {
        super();
        this.mPackageName = packageName;
        this.mActionId = actionId;
        this.mGestureId = gestureId;
        this.mClassName = className;
    }
    /**
     * Constructor for an web URL GestureDetail.
     * @param url - URL address
     * @param actionId - action id
     * @param gestureId - gesture id in the GestureLibrary
     */
    public GestureDetail(String url, int actionId, String gestureId) {
        super();
        this.mUrl = url;
        this.mActionId = actionId;
        this.mGestureId = gestureId;
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

    public long getId() {
        return mId;
    }

    public void setDetailId(long detailId) {
        this.mId = detailId;
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

	@Override
	public String toString() {
		return "GestureDetail [mContactId=" + mContactId + ", mId="
				+ mId + ", mActionId=" + mActionId + ", mGestureId="
				+ mGestureId + ", mPackageName=" + mPackageName + ", mUrl="
				+ mUrl + ", mClassName=" + mClassName + "]";
	}
		
}
