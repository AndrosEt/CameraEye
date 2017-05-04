package com.jerry.sweetcamera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.jerry.sweetcamera.SweetApplication;


/**
 * 程序配置数据，保存在sp中，清除缓存时不清除
 * @author Administrator
 *
 */
public class SPConfigUtil {
	static final String TAG = "SPConfigUtil";
	static final String SP_NAME = "SPConfigUtil";

	synchronized public static long loadLong(final String key, long defVal) {
		Long cf = defVal;
		String cfStr = load(key);
		try {
			cf = Long.parseLong(cfStr);
		} catch (Exception e) {

		}
		return cf;
	}
	synchronized public static int loadInt(final String key, int defVal) {
		Integer cf = defVal;
		String cfStr = load(key);
		try {
			cf = Integer.parseInt(cfStr);
		} catch (Exception e) {

		}
		return cf;
	}

	synchronized public static boolean loadBoolean(final String key,
			boolean defVal) {
		Boolean cf = defVal;
		String cfStr = load(key);
		try {
			if(!TextUtils.isEmpty(cfStr)){
				cf = Boolean.parseBoolean(cfStr);
			}
		} catch (Exception e) {

		}
		return cf;
	}

	synchronized public static String load(final String key) {
		String cf = null;
		if (SweetApplication.CONTEXT == null) {
			return cf;
		}
		try {
			SharedPreferences sp = SweetApplication.CONTEXT.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
			cf = sp.getString(key, null);
		} catch (Exception e) {
			// Log.e(TAG, e);
		}
		return cf;
	}

	synchronized public static void save(final String key, final String val) {
		if (SweetApplication.CONTEXT == null || val == null) {
			return;
		}
		try {
			SharedPreferences sp = SweetApplication.CONTEXT.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
			Editor edit = sp.edit();
			edit.putString(key, val);
			edit.commit();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

	}

	synchronized public static void clear(final String key) {
		try {
			SharedPreferences sp = SweetApplication.CONTEXT.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
			Editor edit = sp.edit();
			edit.remove(key);
			edit.commit();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
	
	synchronized public static void clearAll() {
		try {
			SharedPreferences sp = SweetApplication.CONTEXT.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
			Editor edit = sp.edit();
			edit.clear();
			edit.commit();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
}
