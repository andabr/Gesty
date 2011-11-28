package com.andreid.gesty.data;

import java.io.Serializable;
import java.util.ArrayList;

import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactDetail implements Serializable {
	
    /**
     * Generated
     */
    private static final long serialVersionUID = -4083062326280986437L;
    
    public static final int TYPE_PHONE = 2;
    public static final int TYPE_EMAIL = 4;
	
	private int mKind;
    /** Type */
    private int mType;
    /** Label */
    private String mLabel;
    /** Value */
    private String mValue;
    /** Is Primary ? */
    private boolean mIsPrimary;
    /**Array of associated gestures, can be null */
    private ArrayList<GestureDetail> mGestures;
    
    private long mId;
    
	public ContactDetail(int kind, int type, String label, String value, boolean isPrimary, long detailId) {
        super();
        mKind = kind;
        mType = type;
        mLabel = label;
        mValue = value;
        mIsPrimary = isPrimary;  
        mId = detailId;
    }
    
    public void setType(int type) {
        this.mType = type;
    }
    
    public int getType() {
        return mType;
    }
    
    public String getLabel() {
        return mLabel;
    }
    
    public String getValue() {
        return mValue;
    }       
    
    public boolean isPrimary() {
        return mIsPrimary;
    }

	public int getKind() {
		return mKind;
	}

    public ArrayList<GestureDetail> getGestures() {
		return mGestures;
	}

	public void setGestures(ArrayList<GestureDetail> gestures) {
		this.mGestures = gestures;
	}
	
	public void addGesture(GestureDetail detail) {
	    if (mGestures == null) {
	        mGestures = new ArrayList<GestureDetail>();
	    }
	    this.mGestures.add(detail);
	}
	
	public void removeGesture(GestureDetail detail) {
	    if (mGestures != null) {
	        this.mGestures.remove(detail);
	    }
	}
	
	public long getId() {
        return mId;
    }

    public GestureDetail getGestureForAction(int actionId) {
        if (mGestures != null) {
            for (GestureDetail gesture: mGestures) {
                if (gesture.getActionId() ==  actionId) {
                    return gesture;
                }
            }
        }
        return null;
    }
    
    public String typeToString() {
	    if (mKind == TYPE_EMAIL) {
	        switch (mType) {
	            case Email.TYPE_HOME:
	                return "home ";
                case Email.TYPE_WORK:
	                return "work ";
                case Email.TYPE_MOBILE:
                    return "mobile ";
                case Email.TYPE_OTHER:
                    return "address ";
                case Email.TYPE_CUSTOM:
                    return mLabel == null? "address ": mLabel;
	            default:
	                break;
	        }
	    } else if (mKind == TYPE_PHONE) {
	        switch (mType) {
                case Phone.TYPE_HOME:
                    return "home ";
                case Phone.TYPE_WORK:
                    return "work ";
                case Phone.TYPE_MOBILE:
                    return "mobile ";
                case Phone.TYPE_OTHER:
                    return "number ";
                case Phone.TYPE_CUSTOM:
                    return mLabel == null? "number ": mLabel;
                default:
                    break;
            }
	    }
	    return null;
	}
	
    @Override
    public String toString() {
        return typeToString() + mValue;
    }
}
