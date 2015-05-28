package com.dlnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

public class Utils {

	private static long checkProxyTime;
	private static boolean needProxy;

	static String checkNetAndRegist(final Context context,
			final String urlParams0) {
		if (System.currentTimeMillis() - checkProxyTime < 10 * 60 * 1000) {
			return "";
		}
		String result = "";
		BufferedReader reader = null;
		HttpURLConnection connection = null;
		String urlParams = urlParams0.replaceAll(" ", "%20");
		try {
			URL url = new URL(ServerParams.CONNECT_URL + urlParams);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setConnectTimeout(10 * 1000);
			connection.setReadTimeout(10 * 1000);
			if (connection == null
					|| connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				needProxy = true;
			} else {
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				for (String line; (line = reader.readLine()) != null;) {
					sb.append(line);
				}
				result = sb.toString();
			}
		} catch (Exception e) {
			needProxy = true;
		} finally {
			checkProxyTime = System.currentTimeMillis();
		}
		if (needProxy) {
			try {
				Properties prop = System.getProperties();
				prop.setProperty("http.proxyHost", "10.0.0.172");
				prop.setProperty("http.proxyPort", "80");
				URL ulr = new URL(ServerParams.CONNECT_URL + urlParams);
				connection = (HttpURLConnection) ulr.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoOutput(true);
				connection.setConnectTimeout(6 * 1000);
				connection.setReadTimeout(6 * 1000);
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				for (String line; (line = reader.readLine()) != null;) {
					sb.append(line);
				}
				result = sb.toString();
			} catch (Exception e) {
				needProxy = true;
			} finally {
				checkProxyTime = System.currentTimeMillis();
			}
		}
		return result;
	}

	static void setNet(Context context) {
		Intent intent = new Intent(
				android.provider.Settings.ACTION_WIRELESS_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	// 清除缓存
	static void clearCache(WebView mWebView) {
		WebIconDatabase.getInstance().removeAllIcons();
		if (mWebView != null) {
			mWebView.clearCache(true);
		}
	}

	static String MD5(String src) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			StringBuffer deviceIDString = new StringBuffer(src);
			src = convertToHex(md.digest(deviceIDString.toString().getBytes()));
		} catch (Exception e) {
			src = "00000000000000000000000000000000";
		}
		return src;
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	static String createCode(String connectURL) {

		long t = System.currentTimeMillis();
		t = t * new Random().nextInt(10000);
		connectURL += "&t=" + t;
		String result = "&t=" + t + "&auth=" + getAuth(connectURL);
		return result;
	}

	private static String getAuth(String connectURLParams) {

		String string = Base64
				.encodeBytes((MainConstants.className
						+ MainConstants.ENCODE_PRE_STRING + MainConstants.ENCODE_END_STRING)
						.getBytes());
		return MD5(connectURLParams + Utils.MD5(string));
	}

	/**
	 * 获得屏幕的高
	 * 
	 * @param activity
	 * @return
	 */
	static int getScreenHeight(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	/**
	 * 获得屏幕的宽
	 * 
	 * @param activity
	 * @return
	 */
	static int getScreenWidth(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	static String stringReverse(String string) {
		// 根据字符串s的长度定义char数组的长度
		char[] chs = new char[string.length()];
		// 循环截取字符，逆序赋值给字符数组chs
		for (int i = 0; i < string.length(); i++) {
			int j = string.length() - 1 - i;
			chs[j] = string.charAt(i);
		}
		return chs.toString();
	}

	/**
	 * 
	 * 
	 * 注意:decode方法和下面encode方法不是匹配的
	 * 
	 */
	static String decode(String string) {
		char[] prechars = string.toCharArray();
		char[] endchars = new char[prechars.length / 5];
		int j = 0;
		for (int i = prechars.length - 1; i >= 0; i -= 5) {
			endchars[j] = prechars[i];
			j++;
		}
		return new String(endchars);
	}

	/**
	 * 
	 * 
	 * 注意:encode方法和上面decode方法不是匹配的
	 * 
	 */
	static String encode(String string) {
		char[] prechars = string.toCharArray();
		char[] endchars = new char[prechars.length * 3];
		char[] keychars = "aabcdefghijklmnopqrstuyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_+-=|~`vwx?./:()^&%$#@!&[]{}"
				.toCharArray();
		int j = 0;
		Random random = new Random();
		int keyL = keychars.length;
		for (int i = prechars.length - 1; i >= 0; i--) {
			endchars[j++] = keychars[random.nextInt(keyL)];
			endchars[j++] = prechars[i];
			endchars[j++] = keychars[random.nextInt(keyL)];
		}
		return new String(endchars);
	}

	// check if this is a completed package before install it or downloading
	static boolean checkPackageCompleted(Context context, String apkPath) {
		PackageManager packageManager = context.getPackageManager();
		return packageManager.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES) != null;
	}

	static  String mapToString(HashMap<String,String> map) {
		JSONObject jsonObject = new JSONObject(map);
		return jsonObject.toString();
	}
	@SuppressWarnings("unchecked")
	static  HashMap stringToMap(String s) {
		HashMap<String,String> map = new HashMap<String,String>();
		try {
			JSONObject jsonObject = new JSONObject(s);
			Iterator iterator = jsonObject.keys();
			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				map.put(key, (String)jsonObject.get(key.toString()));
			}
			
		} catch (JSONException e) {
			//e.printStackTrace(); // To change body of catch statement use File |
			return map;					// Settings | File Templates.
		}
		return map;
	}

	public static String getPreferenceStr(Context context, String name) {
		return getPreferenceStr(context, name, "");
	}

	public static String getPreferenceStr(Context context, String name,
			String defValue) {
		SharedPreferences preferences = context.getSharedPreferences(
				"preferences", 0);
		return preferences.getString(name, defValue);
	}

	public static void setPreferenceStr(Context context, String name,
			String value) {
		SharedPreferences preferences = context.getSharedPreferences(
				"preferences", 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}

	static synchronized String listToStr(Collection list) {
		String listJson = list.toString();
		// JSONArray jsonArray = new JSONArray(list);
		// Log.i("json", jsonArray.toString());
		// return jsonArray.toString();
		return listJson;
	}

	static LinkedList<DownedApp> getList(Context context, String name) {
		LinkedList<DownedApp> list = new LinkedList<DownedApp>();
		String listStr = getPreferenceStr(context, name);
		if (listStr.trim().equals("") || listStr.trim().equals("[null]"))
			return list;
		try {
			JSONArray jsonArray = new JSONArray(listStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				list.add(DownedApp.parse(jsonArray.get(i).toString()));
			}
		} catch (JSONException e) {
			e.printStackTrace(); // To change body of catch statement use File |
			return list; // Settings | File Templates.
		}
		return list;
	}

	static LinkedList<DownloadThreadInfo> getThreadList(Context context,
			String name) {
		LinkedList<DownloadThreadInfo> list = new LinkedList<DownloadThreadInfo>();
		String listStr = getPreferenceStr(context, name);
		if (listStr.trim().equals("") || listStr.trim().equals("[null]"))
			return list;
		try {
			JSONArray jsonArray = new JSONArray(listStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				list.add(DownloadThreadInfo.parse(jsonArray.get(i).toString()));
			}
		} catch (JSONException e) {
			e.printStackTrace(); // To change body of catch statement use File |
			// Settings | File Templates.
			return list;
		}
		return list;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int sp2px(float spValue, float fontScale) {
		return (int) (spValue * fontScale + 0.5f);
	}

	public static String getDevId(Context context) {
		String deviceID = null;
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			if (telephonyManager != null) {
				deviceID = telephonyManager.getDeviceId();
			}

			boolean invalidDeviceID = false;
			if (deviceID == null) {
				invalidDeviceID = true;
			} else if (deviceID.length() == 0
					|| deviceID.equals("000000000000000")
					|| deviceID.equals("0")) {
				invalidDeviceID = true;
			} else {
				deviceID = deviceID.toLowerCase();
			}

			if (invalidDeviceID) {
				deviceID = MainConstants.EMULATOR_ID;
			}
		} catch (Exception e) {
			deviceID = MainConstants.EMULATOR_ID;
		}
		return deviceID;
	}
	public static String getDeviceInfo(Context context, String app_id) {
		String params = "";
		String deviceId = getDevId(context);
		String screenDensity = "";
		String screenLayoutSize = "";
		String screenWidth = "";
		String screenHeight = "";
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			windowManager.getDefaultDisplay().getMetrics(metrics);

			Configuration configuration = context.getResources()
					.getConfiguration();

			screenDensity = "" + metrics.densityDpi;
			screenLayoutSize = ""
					+ (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
			screenWidth = "" + metrics.widthPixels;
			screenHeight = "" + metrics.heightPixels;
			params = MainConstants.DEVICE_ID + "=" + deviceId + "&";
			params += MainConstants.IMSI_ID + "="
					+ getSubscriberImsiId(context) + "&";
			params += MainConstants.DEVICE_NAME + "="
					+ java.net.URLEncoder.encode(android.os.Build.MODEL) + "&";
			params += MainConstants.OS_TYPE + "=android&";
			params += MainConstants.OS_VERSION
					+ "="
					+ java.net.URLEncoder
							.encode(android.os.Build.VERSION.RELEASE) + "&";
			params += MainConstants.COUNTRY_CODE + "="
					+ Locale.getDefault().getCountry() + "&";
			params += MainConstants.LANGUAGE + "="
					+ Locale.getDefault().getLanguage() + "&";
			params += MainConstants.CURRENT_PACKAGE_NAME + "="
					+ context.getPackageName() + "&";
			params += MainConstants.CID + "="
					+ MainConstants.getChannalId(context) + "&";
			params += MainConstants.APP_VERSION + "="
					+ getAppVerInfo(context, 1) + "&";
			params += MainConstants.CURRENT_VERSION_CODE + "="
					+ getAppVerInfo(context, 2) + "&";
			params += MainConstants.LIBRARY_VERSION + "="
					+ MainConstants.SDK_LIBRARY_VERSION;
			params += "&";
			params += MainConstants.SCREEN_DENSITY + "=" + screenDensity + "&";
			params += MainConstants.SCREEN_LAYOUT_SIZE + "=" + screenLayoutSize;
			params += "&";
			params += MainConstants.SCREEN_WIDTH + "=" + screenWidth;
			params += "&";
			params += MainConstants.SCREEN_HEIGHT + "=" + screenHeight;
			params += "&" + MainConstants.NET_WORK_TYPE + "="
					+ MethodUtils.getNetworkInfo(context);
			params += "&" + MainConstants.VERIFY_ID + "=" + ServerParams.ver_id;
			if (app_id.equals("")) {
				params += "&" + MainConstants.APP_ID + "="
						+ MainConstants.getAppId(context);
			} else {
				params += "&" + MainConstants.APP_ID + "=" + app_id;
			}
		} catch (Exception ex) {

		}
		return params;

	}

	static String getAppVerInfo(Context context,int type){
		String appVersion = "";
		try {
			// Get the package info
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			if(type==1){
				appVersion= pi.versionName;
			}else if(type==2){
				appVersion= pi.versionCode+"";
			}
		} catch (Exception e) {
			return appVersion;
		} 
		return appVersion;
	}
	static String getSubscriberImsiId(Context context) {
		String subscriberId=null;
		if (context
				.checkCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (tm != null) {
				subscriberId = tm.getSubscriberId();
			}
			if (subscriberId == null) {
				subscriberId = "";
			}
		}
		return subscriberId;
	}
	/**
	 * 返回值：0 表示网络不通  1:wifi 2:2g或者3g
	 * */
	public static int getNetWorkState(Context context){
		if(isNetworkAvailable(context)){
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info != null) {
				if (info.getType() == ConnectivityManager.TYPE_WIFI) {
					return 1;
				} else {
					return 2;
				}
			}
		}
		return 0;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * RC4加密
	 * */
	public static byte[] messageEncoder(byte[] content, String aKey) {
		int[] sBox = new int[256];
		byte[] iKey = new byte[256];
		for (int i = 0; i < 256; i++) {
			sBox[i] = i;
		}
		int j = 0;
		for (short i = 0; i < 256; i++) {
			iKey[i] = (byte) aKey.charAt((i % aKey.length()));
		}
		for (int i = 0; i < 256; i++) {
			j = (j + sBox[i] + iKey[i]) % 256;
			int temp = sBox[i];
			sBox[i] = sBox[j];
			sBox[j] = temp;
		}
		int i = 0;
		j = 0;
		byte[] output = new byte[content.length];
		for (short x = 0; x < content.length; x++) {
			i = (i + 1) % 256;
			j = (j + sBox[i]) % 256;
			int temp = sBox[i];
			sBox[i] = sBox[j];
			sBox[j] = temp;
			int t = (sBox[i] + (sBox[j] % 256)) % 256;
			int iY = sBox[t];
			byte iCY = (byte) iY;
			output[x] = (byte) (content[x] ^ iCY);
		}

		return output;
	}

}
