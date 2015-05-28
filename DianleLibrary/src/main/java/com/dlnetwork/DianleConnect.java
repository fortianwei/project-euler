package com.dlnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class DianleConnect {
	private static DianleConnect mDianleConnect = null;

	static DianleConnect getDianleConnect() {
		if (mDianleConnect == null) {
			mDianleConnect = new DianleConnect();
		}
		return mDianleConnect;
	}

	private DianleConnect() {
	}

	String getValueFromJson(String str, String keyName) {
		String value = "";
		try {
			if (str == null || str == "" || str.trim() == "") {
				return value = "-1";
			}
			if (str.indexOf("{") == -1 || str.indexOf("}") == -1) {
				return value = "-1";
			}
			str = str.substring(str.indexOf("{"), str.lastIndexOf("}") + 1);

			JSONObject jsonObj = new JSONObject(str);
			if (jsonObj != null) {
				value = jsonObj.getString(keyName);
				if (value != null && !"".equals(value)) {
					return value;
				}
			}
		} catch (Exception e) {
			return value;
		}
		return value;
	}

	String optValueFromJson(String str, String keyName) {
		String value = "";
		try {
			if (str == null || str == "" || str.trim() == "") {
				return value = "-1";
			}
			if (str.indexOf("{") == -1 || str.indexOf("}") == -1) {
				return value = "-1";
			}
			str = str.substring(str.indexOf("{"), str.lastIndexOf("}") + 1);

			JSONObject jsonObj = new JSONObject(str);
			if (jsonObj != null) {
				value = jsonObj.optString(keyName);
				if (value != null && !"".equals(value)) {
					return value;
				}
			}
		} catch (Exception e) {
			return value;
		}
		return value;
	}

	/**
	 * Show available offers using publisher provided userID. This method should
	 * be used if the application tracks users with a different value than the
	 * IMEI/MEID.
	 * 
	 * @param userID
	 *            Publisher defined userID.
	 */
	void showOffers(final WebView mWebView, final Context context) {
		String urlParams = getNowUrlParams(context);
		urlParams += "&snuid=" + DevInit.getCurrentUserID(context);

		String clickURL = ServerParams.TEST_GET_OFFERS_URL + urlParams;
		clickURL = clickURL.replaceAll(" ", "%20");
		final String url = clickURL;
		mWebView.loadUrl(url + "&"
				+ DianleURLConnection.getPackageNameListString(context));
	}

	String createParams(Context context) {
		String[] arr = getNowUrlParams(context).split("&");
		String jsonString = "var params={";
		for (int i = 0; i < arr.length; i++) {
			String[] kv = arr[i].split("=");
			if (kv.length == 1) {
				if (i == arr.length - 1) {
					jsonString += "\"" + kv[0] + "\"" + ":" + "\"" + "\"";
				} else {
					jsonString += "\"" + kv[0] + "\"" + ":" + "\"" + "\"" + ",";
				}

			} else {
				if (i == arr.length - 1) {
					jsonString += "\"" + kv[0] + "\"" + ":" + "\"" + kv[1]
							+ "\"";
				} else {
					jsonString += "\"" + kv[0] + "\"" + ":" + "\"" + kv[1]
							+ "\"" + ",";
				}
			}
		}
		jsonString = jsonString + "};";
		return jsonString;
	}

	/**
	 * MD5加密字符串
	 */
	String MD5(String string) {
		MessageDigest md = null;
		String dstr = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(string.getBytes());
			dstr = new BigInteger(1, md.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return dstr;
	}

	synchronized void getMoney(final Context context,
			final GetTotalMoneyListener getTotalMoneyListener) {
		this.getTotalMoneyListener = getTotalMoneyListener;
		new Thread(new Runnable() {

			@Override
			public void run() {
				long amount = -1;
				String result = DianleURLConnection.connectToURL(
						ServerParams.TEST_GET_POINTS_URL,
						getNowUrlParams(context));
				if (result == null || result.trim().equals("")) {
					error = result;
					mHandler.post(getMoneyFailed);
					return;
				}
				error = optValueFromJson(result, "status");
				if (error == null) {
					error = result;
					mHandler.post(getMoneyFailed);
					return;
				}
				if (!error.equals("1")) {
					mHandler.post(getMoneyFailed);
					return;
				}
				String value = getValueFromJson(result, "user_account");
				String name = getValueFromJson(result, "currency_name");
				String isShowMoney = getValueFromJson(result, "display");
				if (isShowMoney != null) {
					if (isShowMoney.equals("1")) {
						Utils.setPreferenceStr(context,
								MainConstants.IS_SHOW_MONEY_DATA_KEY, "true");
					} else if (isShowMoney.equals("0")) {
						Utils.setPreferenceStr(context,
								MainConstants.IS_SHOW_MONEY_DATA_KEY, "false");

					}
				}
				if (value == "") {
					mHandler.post(getMoneyFailed);
				} else {
					amount = Long.parseLong(value);
					// if (amount < 0) {
					// mHandler.post(getMoneyFailed);
					// } else {
					setNameAndAmount(amount, name);
					mHandler.post(getMoneySuccess);
					// }
				}
			}

		}).start();
	}

	synchronized void spendMoney(final Context context, final int number,
			final SpendMoneyListener spendMoneyListener) {
		this.spendMoneyListener = spendMoneyListener;
		new Thread(new Runnable() {

			@Override
			public void run() {
				long amount = -1;
				String urlParams = getNowUrlParams(context);
				urlParams += "&" + MainConstants.SPEND_POINT + "=" + number;
				urlParams += Utils.createCode(urlParams);
				String result = DianleURLConnection.connectToURL(
						ServerParams.SPEND_POINT_URL, urlParams);
				if (result == null || result.trim().equals("")) {
					error = result;
					mHandler.post(spendMoneyFailed);
					return;
				}
				error = optValueFromJson(result, "status");
				if (error == null) {
					error = result;
					mHandler.post(spendMoneyFailed);
					return;
				}
				if (!error.equals("1")) {
					mHandler.post(spendMoneyFailed);
					return;
				}
				String value = getValueFromJson(result, "user_account");
				if (value == "") {
					mHandler.post(spendMoneyFailed);
				} else {
					amount = Long.parseLong(value);
					if (amount < 0) {
						mHandler.post(spendMoneyFailed);
					} else {
						setAmount(amount);
						mHandler.post(spendMoneySuccess);
					}
				}
			}
		}).start();
	}

	synchronized void givePoint(Context context, int number,
			GiveMoneyListener giveMoneyListener) {
		this.giveMoneyListener = giveMoneyListener;
		String urlParams = getNowUrlParams(context);
		urlParams += "&" + MainConstants.GIVE_POINT + "=" + number;
		urlParams += "&" + MainConstants.USER_ID + "="
				+ DevInit.getCurrentUserID(context);
		urlParams += createMethodAddCode(ServerParams.GIVE_POINT_URL
				+ urlParams);
		final String urlparams = urlParams;
		new Thread(new Runnable() {

			@Override
			public void run() {
				long amount = -1;
				String result = DianleURLConnection.connectToURL(
						ServerParams.GIVE_POINT_URL, urlparams);
				if (result == null || result.trim().equals("")) {
					error = result;
					mHandler.post(giveMoneyFailed);
					return;
				}
				error = optValueFromJson(result, "status");
				if (error == null) {
					error = result;
					mHandler.post(giveMoneyFailed);
					return;
				}
				if (!error.equals("1")) {
					mHandler.post(giveMoneyFailed);
					return;
				}
				String value = getValueFromJson(result, "user_account");
				if (value == "") {
					mHandler.post(giveMoneyFailed);
				} else {
					amount = Long.parseLong(value);
					// if (amount < 0) {
					// mHandler.post(giveMoneyFailed);
					// } else {
					setAmount(amount);
					mHandler.post(giveMoneySuccess);
					// }
				}
			}
		}).start();
	}

	private String createMethodAddCode(String connectURL) {
		long t = System.currentTimeMillis();
		connectURL += "&t=" + t;
		String result = "&t="
				+ t
				+ "&auth="
				+ Utils.MD5(connectURL
						+ Utils.MD5(Base64
								.encodeBytes((MainConstants.key
										+ MainConstants.ENCODE_PRE_STRING + MainConstants.ENCODE_END_STRING)
										.getBytes())));
		return result;
	}

	void getOnlineParams(final Context context, final String key,
			final GetOnlineParamsListener getOnlineParamsListener,
			final String defValue) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				String urlParams = getNowUrlParams(context);
				urlParams += "&" + MainConstants.THE_KEY_OF_GET_GETPARAMS + "="
						+ key;
				String result = DianleURLConnection.connectToURL(
						ServerParams.GET_SETED_PARAMS, urlParams, 5000, 5000);
				if (result == null || result.trim().equals("")) {
					result = "";
					error = result;
					onParamsReturn(MethodUtils.getLocaValue(context, key,
							defValue));
					return;
				}
				error = optValueFromJson(result, "status");
				if (error == null) {
					error = result;
					onParamsReturn(MethodUtils.getLocaValue(context, key,
							defValue));
					return;
				}
				if (!error.equals("1")) {
					onParamsReturn(MethodUtils.getLocaValue(context, key,
							defValue));
					return;
				}
				String params = getValueFromJson(result, "p_value");

				if (params == null || params.trim().equals("")) {
					onParamsReturn(MethodUtils.getLocaValue(context, key,
							defValue));
				} else {
					MethodUtils.setLocaValue(context, key, params);
					onParamsReturn(params);
				}
			}

			private void onParamsReturn(final String params) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						getOnlineParamsListener.onParamsReturn(params);
					}
				});
			}

		}).start();

	}

	String getOnlineParams(final Context context, String key, String defValue) {
		String urlParams = getNowUrlParams(context);
		urlParams += "&" + MainConstants.THE_KEY_OF_GET_GETPARAMS + "=" + key;
		String result = DianleURLConnection.connectToURL(
				ServerParams.GET_SETED_PARAMS, urlParams, 2000, 2000);
		String error = "";
		if (result == null || result.trim().equals("")) {
			error = result;
			return MethodUtils.getLocaValue(context, key, defValue);
		}
		error = optValueFromJson(result, "status");
		if (error == null) {
			error = result;
			return MethodUtils.getLocaValue(context, key, defValue);
		}
		if (!error.equals("1")) {
			return MethodUtils.getLocaValue(context, key, defValue);
		}
		String params = getValueFromJson(result, "p_value");

		if (params == null || params.trim().equals("")) {
			return MethodUtils.getLocaValue(context, key, defValue);
		} else {
			MethodUtils.setLocaValue(context, key, params);
			return params + "";
		}
	}

	String checkNet(Context context) {
		return Utils.checkNetAndRegist(context, getNowUrlParams(context));

	}

	void initConnect(final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String setups = DianleURLConnection
						.getPackageNameListString(context);
				if (setups.length() <= 7) {
					return;
				}
				String result = DianleURLConnection.connectToURLPost(
						ServerParams.CONNECT_URL, getNowUrlParams(context),
						setups);
				String error = "";
				if (result == null || result.trim().equals("")) {
					error = result;
					return;
				}
				error = optValueFromJson(result, "status");
				if (error == null) {
					error = result;
					return;
				}
				if (!error.equals("1")) {
					return;
				}
				String prefix = "&" + MainConstants.KEY_PACKAGE_NAMES + "=";
				int length = prefix.length();
				if (setups.trim().length() > length + 10) {
					String newInstalledApps = setups.substring(length - 1);
					if (newInstalledApps != null
							&& !newInstalledApps.equals("")) {
						DianleURLConnection.saveAddedPacnames(newInstalledApps);
					}
				}
			}

		}).start();
	}

	String getMetaData(Context context) {
		PackageManager pm = context.getPackageManager();
		JSONObject jo = new JSONObject();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			if (appInfo != null && appInfo.metaData != null) {
				Set<String> set = appInfo.metaData.keySet();
				for (String str : set) {
					jo.put(str, appInfo.metaData.get(str));
				}
			} else {
				jo.put("meta", "meta is null");
			}
		} catch (Exception e) {
			jo = new JSONObject();
			try {
				jo.put("exception", e.toString());
			} catch (JSONException e1) {
			}
		}
		String json = jo.toString() + "";

		return Utils.encode(json);
	}

	void downloadApp(Context context, final Map<String, String> appInfo) {
		String url = null;
		url = (String) appInfo.get("download");
		url = ServerParams.PRE_STRING.replace("X", "").replace("T", "")
				+ "api/ad" + " ".trim() + "li" + " ".trim() + "st/" + url;
		DownedApp app = getDownedAppFromUrl(url);
		PackageManager localPackageManager = context.getPackageManager();
		final Intent localIntent = localPackageManager
				.getLaunchIntentForPackage(app.getPackageName());
		if (localIntent != null) {
			// if
			// (DianleURLConnection.getFailedAppList().contains(app.getPackageName()))
			// {
			Intent intent = new Intent(context.getPackageName() + "."
					+ ServerParams.ADD_OPEN_APP);
			intent.putExtra("app", app.toString());
			context.sendBroadcast(intent);
			// }
			context.startActivity(localIntent);
		} else {
			// Log.i("package", context.getPackageName());
			Intent intent = new Intent(context.getPackageName() + "."
					+ ServerParams.ADD_DOWN_APP);
			intent.putExtra("app", app.toString());
			context.sendBroadcast(intent);
		}
	}

	private DownedApp getDownedAppFromUrl(String url) {
		HashMap<String, String> map = getGetKeyValue(url);

		String name_zh = map.get("ad_name") + "";
		String install_notice = map.get("setup_tips") + "";
		String inefficacy_notice = map.get("befour_tips") + "";
		String efficacy_use_type = map.get("cate") + "";
		try {
			name_zh = URLDecoder.decode(name_zh, "utf-8");
			inefficacy_notice = URLDecoder.decode(inefficacy_notice, "utf-8");
			install_notice = URLDecoder.decode(install_notice, "utf-8");
			efficacy_use_type = URLDecoder.decode(efficacy_use_type, "utf-8");
		} catch (Exception e) {

		}
		if (install_notice != null && install_notice.equals("00")) {
			install_notice = null;
		}
		if (inefficacy_notice != null && inefficacy_notice.equals("00")) {
			inefficacy_notice = null;
		}
		DownedApp downedApp = new DownedApp();
		// 这里需要check一下
		url = url.substring(0, url.indexOf("&adType"));
		downedApp.addAttribute(MainConstants.AD_CURRENT_URL, url);
		downedApp
				.addAttribute(MainConstants.AD_CAN_GIVE_POINT, map.get("down"));
		downedApp
				.addAttribute(MainConstants.AD_ADD_SHORTCUT, map.get("add_kj"));
		downedApp.addAttribute(MainConstants.AD_ID, map.get("ad_id"));
		downedApp.addAttribute(MainConstants.AD_NAME, name_zh);
		downedApp
				.addAttribute(MainConstants.AD_PACK_NAME, map.get("pack_name"));
		downedApp.addAttribute(MainConstants.AD_ACTIVE_TIME,
				map.get("active_time"));
		downedApp.addAttribute(MainConstants.AD_URL, url);
		downedApp.addAttribute(MainConstants.AD_INSTALL_NOTICE, install_notice);
		downedApp.addAttribute(MainConstants.AD_INEFFICACY_NOTICE,
				inefficacy_notice);
		downedApp.addAttribute(MainConstants.AD_EFFICACY_USE_TYPE,
				efficacy_use_type);
		downedApp.addAttribute(MainConstants.AD_OPENTIME, "0");
		downedApp.addAttribute(MainConstants.AD_STARTTIME, "0");
		downedApp.addAttribute(MainConstants.AD_APP_ID, map.get("appid"));
		// 非普通任务
		if (map.get("adType").equals("sign")) {
			downedApp.addAttribute(MainConstants.AD_TASKID, map.get("task_id"));
			downedApp.addAttribute(MainConstants.AD_TASKTYPE,
					map.get("task_type"));
			downedApp.addAttribute(MainConstants.AD_PARAMS, map.get("param"));
			if (map.get("task_type").equals("2")
					|| map.get("task_type").equals("0")) {
				downedApp.addAttribute(MainConstants.AD_DEADLINE_TIME,
						map.get("due_date"));
				downedApp.addAttribute(MainConstants.AD_DATE_DIFF,
						map.get("date_diff"));
				// downedApp.addAttribute(MainConstants.AD_ID,map.get("ad_id"));

			}
		} else {
			// 普通任务
			downedApp.addAttribute(MainConstants.AD_TASKTYPE, "-1");
			downedApp.addAttribute(MainConstants.AD_PARAMS, "-1");
			downedApp.addAttribute(MainConstants.AD_DATE_DIFF, "0");
		}
		return downedApp;
	}

	private static HashMap<String, String> getGetKeyValue(String getUrlString) {
		getUrlString = getUrlString.substring(getUrlString.indexOf("?")
				+ "?".length());
		String[] arr = getUrlString.split("&");
		HashMap<String, String> map = new HashMap<String, String>();
		for (String string : arr) {
			String[] kv = string.split("=");
			if (kv.length == 1) {
				map.put(kv[0], "");
			} else {
				map.put(kv[0], kv[1]);
			}
		}
		return map;
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 0:
				String error = (String) message.obj;
				getAdListListener.getAdListFailed(error);
				break;
			case 1:
				List list = (List) message.obj;
				getAdListListener.getAdListSucceeded(list);
				break;
			case 2:
				String errorTask = (String) message.obj;
				getAdTaskListListener.getAdListFailed(errorTask);
				break;
			case 3:
				List listTask = (List) message.obj;
				getAdTaskListListener.getAdListSucceeded(listTask);
				break;
			}
		}
	};
	private GiveMoneyListener giveMoneyListener;
	private GetTotalMoneyListener getTotalMoneyListener;
	private SpendMoneyListener spendMoneyListener;
	private GetAdListListener getAdListListener;
	private GetAdTaskListListener getAdTaskListListener;
	private long amount = -1;
	private long adAmount = 0;
	private String name = "";
	private String error = "";
	// Create runnable for posting
	private final Runnable giveMoneyFailed = new Runnable() {
		public void run() {
			giveMoneyListener.giveMoneyFailed(error);
		}
	};
	private final Runnable giveMoneySuccess = new Runnable() {
		public void run() {
			giveMoneyListener.giveMoneySuccess(amount);
		}
	};
	private final Runnable spendMoneyFailed = new Runnable() {
		public void run() {
			spendMoneyListener.spendMoneyFailed(error);
		}
	};
	private final Runnable spendMoneySuccess = new Runnable() {
		public void run() {

			spendMoneyListener.spendMoneySuccess(amount);
		}
	};
	private final Runnable getMoneyFailed = new Runnable() {
		public void run() {
			getTotalMoneyListener.getTotalMoneyFailed(error);
		}
	};
	private final Runnable getMoneySuccess = new Runnable() {
		public void run() {
			getTotalMoneyListener.getTotalMoneySuccessed(name, amount);
		}
	};

	private void setNameAndAmount(long amount, String name) {
		DevInit.amount = amount;
		DevInit.name = name;
		DianleConnect.this.name = name;
		DianleConnect.this.amount = amount;
	}

	private void setNameAndADAmount(long amount, String name) {
		DevInit.adAmount = amount;
		DevInit.name = name;
		DianleConnect.this.name = name;
		DianleConnect.this.adAmount = amount;
	}

	private void setAmount(long amount) {
		DevInit.amount = amount;
		DianleConnect.this.amount = amount;
	}

	String getNowUrlParams(Context context) {
		String result = Utils.getDeviceInfo(context, "");
		return result;
	}
}
