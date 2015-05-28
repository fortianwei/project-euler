package com.dlnetwork;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
* @author Jiangtao.Cai
* @qq:75905171
* @version create date ：2011-12-9 下午01:56:55
*/
final class MethodUtils {

	static String getNetworkType(Context context) {
		if (context.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
			return "";
		}

		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager == null) {
			return "";
		}
		NetworkInfo netInfo = connManager.getActiveNetworkInfo();
		if (netInfo == null) {
			return "";
		}
		return netInfo.getTypeName();
	}

	public static String getNetworkInfo(Context context) {
		ConnectivityManager localConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
		NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
		if (localNetworkInfo == null)
			return getNetworkType(context);
		if (localNetworkInfo.getType() == 1)
			return getNetworkType(context);
		String str = getNetworkType(context) + "," + localNetworkInfo.getExtraInfo() + "," + localNetworkInfo.getSubtypeName() + ","
				+ localNetworkInfo.getSubtype();
		return java.net.URLEncoder.encode(str);
	}

	static String getLocaValue(Context context, String key, String defValue) {
		String result = Utils.getPreferenceStr(context,key, defValue);
		if (result != null && result.equals(defValue)) {
			Utils.setPreferenceStr(context,key, defValue);
		}
		return result;
	}

	static void setLocaValue(Context context, String key, String value) {
		Utils.setPreferenceStr(context,key, value);
	}

}
