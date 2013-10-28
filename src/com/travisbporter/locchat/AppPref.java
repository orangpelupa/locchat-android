
package com.travisbporter.locchat;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

//Helper for saving and restoring App Preferences.
public class AppPref {
	private static final String APP_SHARED_PREFS = AppPref.class.getSimpleName();
	private SharedPreferences sharedPrefs_;
	private Editor prefsEditor_;
	private Map<String,String> defaults_;
	
	public static final String KEY_PREFS_USER_NAME = "user_name";
	public static final String KEY_PREFS_SERVER_URL = "server_url";
	public static final String KEY_PREFS_DIST = "dist";
	public static final String KEY_PREFS_WAVE = "wave";
	public static final String KEY_PREFS_POLL = "poll";
	
	public AppPref(Context context){
		this.sharedPrefs_ = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.prefsEditor_ = sharedPrefs_.edit();
		defaults_ = new HashMap<String,String>();
		
		//DEFAULT VALUES CHANGED HERE
		defaults_.put(KEY_PREFS_USER_NAME, "Anon");
		defaults_.put(KEY_PREFS_SERVER_URL, "http://96.126.105.62:8080/");
		defaults_.put(KEY_PREFS_DIST, "30");
		defaults_.put(KEY_PREFS_WAVE, String.valueOf(true));
		defaults_.put(KEY_PREFS_POLL, "10");
		
	}
	
	public String getPref(String key){
		return sharedPrefs_.getString(key, defaults_.containsKey(key) ? defaults_.get(key) : "" );
	}
	
	public void savePref(String key, String data){
		prefsEditor_.putString(key, data);
		prefsEditor_.commit();
	}
}
