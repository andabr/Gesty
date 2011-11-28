package com.andreid.gesty;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andreid.gesty.R;

/**
 * Preferences cache class 
 * Application level cached settings are to be stored and retrieved via this class.
 */
public class PreferencesCache {
	private static final String TAG = "PreferencesCache";
	
	public static final String GESTURE_COLOR_KEY = "color_key";
	
	public static final String GESTURE_THUMBNAIL_SIZE = "size_key";
	
	public static final String GESTURE_THUMBNAIL_INSET = "inset_key";
	
	public static final String GESTURE_PREDICTION_ACCURACY = "accuracy";

    /**
     * Sets a preference String value.
     * @param context The context to use
     * @param key The preference key to be set
     * @param value The preference value to be set
     */
    @SuppressWarnings("unused")
	private static void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);

        if (!editor.commit()) {
            throw new NullPointerException(TAG+".setLongValue() Failed to set key[" + key
                    + "] with value[" + value + "]");
        }
    }
    
    /**
     * Sets a preference Boolean value.
     * @param context The context to use
     * @param key The preference key to be set
     * @param value The preference value to be set
     */
    @SuppressWarnings("unused")
	private static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(key, value);

        if (!editor.commit()) {
            throw new NullPointerException(TAG+".setBooleanValue() Failed to set key[" + key
                    + "] with value[" + value + "]");
        }
    }
    
    
    /**
     * Sets a preference Integer value.
     * @param context The context to use
     * @param key The preference key to be set
     * @param value The preference value to be set
     */
    private static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(key, value);

        if (!editor.commit()) {
            throw new NullPointerException(TAG+".setIntValue() Failed to set key[" + key
                    + "] with value[" + value + "]");
        }
    }
    
    /**
     * Gets a preference Integer value
     * @param context The context to use
     * @param key The preference key to retrieve
     * @param defaultValue The default value to assign to the preference in case its not found
     * @return
     */
    private static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key,
                defaultValue);
    }
    
    /**
     * Sets a preference Integer value.
     * @param context The context to use
     * @param key The preference key to be set
     * @param value The preference value to be set
     */
    private static void setFloat(Context context, String key, float value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putFloat(key, value);

        if (!editor.commit()) {
            throw new NullPointerException(TAG+".setIntValue() Failed to set key[" + key
                    + "] with value[" + value + "]");
        }
    }
    
    /**
     * Gets a preference Integer value
     * @param context The context to use
     * @param key The preference key to retrieve
     * @param defaultValue The default value to assign to the preference in case its not found
     * @return
     */
    private static float getFloat(Context context, String key, float defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key,
                defaultValue);
    }
    /**
     * Gets a preference String value
     * @param context The context to use
     * @param key The preference key to retrieve
     * @param defaultValue The default value to assign to the preference in case its not found
     * @return
     */
    @SuppressWarnings("unused")
	private static String getString(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key,
                defaultValue);
    }
    
    /**
     * Gets a preference Boolean value
     * @param context The context to use
     * @param key The preference key to retrieve
     * @param defaultValue The default value to assign to the preference in case its not found
     * @return The preference key value
     */
    @SuppressWarnings("unused")
	private static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }
       
    
    public static void setGestureColor(Context context, int color) {
    	setInt(context, GESTURE_COLOR_KEY, color);
    }
    
    public static int getGestureColor(Context context) {
    	return getInt(context, GESTURE_COLOR_KEY, context.getResources().getColor(R.color.vodafone_red_04));
    }
    
    public static void setGestureSize(Context context, int size) {
    	setInt(context, GESTURE_THUMBNAIL_SIZE, size);
    }
    
    public static int getGestureSize(Context context) {
    	//TODO:hard code! need px to dp
    	return getInt(context, GESTURE_THUMBNAIL_SIZE, 88);
    }
    
    public static void setGestureInset(Context context, int inset) {
    	setInt(context, GESTURE_THUMBNAIL_INSET, inset);
    }
    
    public static int getGestureInset(Context context) {
    	return getInt(context, GESTURE_THUMBNAIL_INSET, 8);
    }
    
    public static void setGestureAccuracy(Context context, float accuracy) {
    	setFloat(context, GESTURE_PREDICTION_ACCURACY, accuracy);
    }
    
    public static float getGestureAccuracy(Context context) {
    	return getFloat(context, GESTURE_PREDICTION_ACCURACY, 2);
    }
}
