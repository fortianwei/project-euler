package com.dlnetwork;

import android.content.Context;
import android.os.Build;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Random;

public class ServiceConnect {

	static void sendDownOK(final Context context, final String data,
			final String app_id) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String urlParams = Utils.getDeviceInfo(context, app_id);
				String post = "c" + "=" + data;
				DianleURLConnection.connectToURL(
						ServerParams.RESPONSE_DOWN_OK_URL, urlParams + "&"
								+ post);

			}
		}).start();
	}

	static void installOK(final Context context, final String data,
			final String app_id) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String urlParams = Utils.getDeviceInfo(context, app_id)
						+ "&ad_id=" + data;
				DianleURLConnection.connectToURL(ServerParams.GET_INSTALLOK,
						urlParams);

			}
		}).start();
	}

	static void sendClick(final Context context, final String data,
			final String app_id) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String urlParams = Utils.getDeviceInfo(context, app_id) + "&"
						+ data;
				DianleURLConnection.connectToURL(ServerParams.ABROAD_CLICK,
						urlParams);
			}
		}).start();
	}

	static boolean addPoint(final Context context, DownedApp downedApp) {
		String connectURLParams = Utils.getDeviceInfo(context,
				downedApp.getAppId());
		connectURLParams += "&package_name=" + downedApp.getPackageName();
		connectURLParams += "&" + MainConstants.STEP_ID + "=1";
		connectURLParams += "&" + MainConstants.USER_ID + "="
				+ Utils.getPreferenceStr(context, MainConstants.USER_ID);

		connectURLParams += "&location=";
		connectURLParams += "&" + MainConstants.AD_ID + "="
				+ downedApp.getAdID();
		String tag = downedApp.isViaScreenOnNotify();
		if (tag != null && tag.equals("true")) {
			connectURLParams += "&" + MainConstants.AD_IS_VIA_SCREEN_ON_NOTIFY
					+ "=1";
		}
		if (Build.VERSION.SDK_INT >= 21) {
			connectURLParams += "&l=yes";
		}
		String params = connectURLParams
				+ createCode(ServerParams.TEST_ADD_MONEY_URL + connectURLParams);
		String result;
		try {
			result = DianleURLConnection.connectToURL(
					ServerParams.TEST_ADD_MONEY_URL, params);
		} catch (Exception e) {
			return false;
		}
		if (result == null) {
			return false;
		}
		return handleConnectResponse(result);
	}

	static void postUrlInfo(final Context context, final String adid,
			final String routeUrl) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// Log.i("tag", routeUrl);
				String token = Utils.MD5(routeUrl + Utils.getDevId(context)
						+ MainConstants.route);
				/*
				 * Log.i("tag", routeUrl + Utils.getDeviceId(context) +
				 * MainConstants.route);
				 */
				DianleURLConnection.connectToURL(
						ServerParams.RESPONSE_REMOTE_ROUTING,
						Utils.getDeviceInfo(context, "") + "&token=" + token
								+ "&ad_id=" + adid + "&p="
								+ URLEncoder.encode(routeUrl));

			}
		}).start();
	}

	static public boolean taskAddPoint(final Context context, String taskId,
			String adId, String app_id) {
		String params = Utils.getDeviceInfo(context, app_id)
				+ "&token="
				+ Utils.MD5(Utils.getDevId(context) + MainConstants.SIGNINTOKEN
						+ taskId) + "&ad_id=" + adId + "&task_id=" + taskId
				+ "&" + MainConstants.USER_ID + "="
				+ Utils.getPreferenceStr(context, MainConstants.USER_ID);
		if (Build.VERSION.SDK_INT >= 21) {
			params += "&l=yes";
		}
		String result;
		try {
			result = DianleURLConnection.connectToURL(
					ServerParams.DEPTHTASK__ADDPOINTS_URL, params);//
		} catch (Exception e) {
			return false;
		}
		if (result == null) {
			return false;
		}
		return handleConnectResponse(result);
	}

	static boolean signInAddPoint(String taskId) {
		// token=md5(deviceid+"dianjoysign"+taskid)
		String params = Utils.MD5(MainConstants.DEVICE_ID
				+ MainConstants.SIGNINTOKEN + taskId);
		String result;
		try {
			result = DianleURLConnection.connectToURL(
					ServerParams.TEST_ADD_MONEY_URL, params);
		} catch (Exception e) {
			return false;
		}
		// Handle the response for a connect call.
		if (result == null) {
			return false;
		}
		return handleConnectResponse(result);
	}

	static private String createCode(String connectURL) {

		long t = System.currentTimeMillis();
		t = t * new Random().nextInt(10000);
		connectURL += "&t=" + t;
		String result = "&t=" + t + "&auth=" + getAuth(connectURL);
		return result;
	}

	static private String getAuth(String connectURLParams) {

		String string = Base64
				.encodeBytes((MainConstants.className
						+ MainConstants.ENCODE_PRE_STRING + MainConstants.ENCODE_END_STRING)
						.getBytes());
		return Utils.MD5(connectURLParams + Utils.MD5(string));
	}

	static boolean handleConnectResponse(String response) {
		try {
			if (response == null || response == "" || response.trim() == "") {
				return false;
			}
			if (response.indexOf("{") == -1 || response.indexOf("}") == -1) {
				return false;
			}
			response = response.substring(response.indexOf("{"),
					response.lastIndexOf("}") + 1);
			JSONObject jsonObj = new JSONObject(response);
			if (jsonObj != null) {
				if (Integer.parseInt(jsonObj.getString("status")) == 1) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	static void sendXPInfo(final Context context, final String action,
			final String id, final String pack_name, final String trigger_pack) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String app_id = MainConstants.getFloatAppId(context);
				String data = "action=" + action + "&ad_id=" + id
						+ "&pack_name=" + pack_name + "&trigger_pack="
						+ trigger_pack;
				String urlParams = Utils.getDeviceInfo(context, app_id) + "&"
						+ data;
				DianleURLConnection.connectToURL(ServerParams.XPADINFO_URL,
						urlParams);
			}
		}).start();
	}

}
