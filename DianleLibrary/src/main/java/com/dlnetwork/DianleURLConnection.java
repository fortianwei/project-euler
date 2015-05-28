package com.dlnetwork;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

final class DianleURLConnection {

	// private String className = "DianleURLConnection";

	static String connectToURL(String url, String params) {
		return connectToURL(url, params, 10 * 1000, 10 * 1000);
	}

	public static String connectToURL(String url, String params, int conOutT,
			int readOutT) {
		// String methodName = "connectToURL";
		String httpResponse = null;
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			String requestURL = url + params;
			//Log.i("url", requestURL);
			requestURL = requestURL.replaceAll(" ", "%20");
			URL noeUrl = new URL(requestURL);
			connection = (HttpURLConnection) noeUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setConnectTimeout(conOutT);
			connection.setReadTimeout(readOutT);
			if (connection.usingProxy()) {
			}
			if (connection.getResponseCode() < 400) {

				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				for (String line; (line = reader.readLine()) != null;) {
					sb.append(line);
				}
				httpResponse = sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
			}
		}
		// Log.i("response", httpResponse);
		return httpResponse;
	}

	static String connectToURLPost(String url, String params, String post) {
		return connectToURLPost(url, params, post, 5 * 1000, 3 * 1000);
	}

	static String connectToURLPost(String url, String params, String post,
			int conOutT, int readOutT) {
		String httpResponse = null;
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			String requestURL = url + params;
			requestURL = requestURL.replaceAll(" ", "%20");
			URL noeUrl = new URL(requestURL);
			connection = (HttpURLConnection) noeUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setConnectTimeout(conOutT);
			connection.setReadTimeout(readOutT);

			Writer writer = null;
			OutputStream out = connection.getOutputStream();
			writer = new BufferedWriter(new OutputStreamWriter(out));
			writer.write(post);
			writer.flush();

			if (connection.usingProxy()) {
			}
			if (connection.getResponseCode() < 400) {

				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				for (String line; (line = reader.readLine()) != null;) {
					sb.append(line);
				}
				httpResponse = sb.toString();
			}
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
			}
		}

		return httpResponse;
	}

	public ByteArrayOutputStream getZip(String url) {
		Log.i("zip", url);
		InputStream httpResponse = null;
		ByteArrayOutputStream baos = null;
		HttpURLConnection connection = null;
		try {
			String requestURL = url;
			requestURL = requestURL.replaceAll(" ", "%20");
			URL noeUrl = new URL(requestURL);
			// LogUtil.e(className, methodName, requestURL);
			connection = (HttpURLConnection) noeUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(30 * 1000);
			connection.setReadTimeout(30 * 1000);
			if (connection.usingProxy()) {
			}
			if (connection.getResponseCode() < 400) {
				baos = new ByteArrayOutputStream();
				httpResponse = connection.getInputStream();
				byte[] buffer = new byte[1024];
				int len = -1;
				while ((len = httpResponse.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return baos;
	}

	static String key_installed_app = "key_installed_app";
	static String split = ",";

	static String getPackageNameListString(Context context) {
		StringBuilder packageNames = new StringBuilder();
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> installed = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		int len = installed.size();

		Properties logProps = SDPropertiesUtils
				.getProperties(MainConstants.PACKAGE_NAME_FILE);
		String failedAppListStr = logProps
				.getProperty(MainConstants.APP_OPEN_FAILED_PACKAGE_NAME_LIST);
		String installed_app_str = logProps.getProperty(key_installed_app);
		if (installed_app_str == null) {
			installed_app_str = "";
		}
		ArrayList<String> failedAppList = new ArrayList<String>();
		if (failedAppListStr == null || failedAppListStr.trim().equals("")) {
		} else {
			String[] appNameArr = failedAppListStr.split(split);
			for (int i = 0; i < appNameArr.length; i++) {
				failedAppList.add(appNameArr[i]);
			}
		}

		LinkedList<DownedApp> openAppTaskList = Utils.getList(context,
				"openAppTaskList");
		ArrayList<String> openApps = new ArrayList<String>();
		for (DownedApp downedApp : openAppTaskList) {
			openApps.add(downedApp.getPackageName());
		}
		for (int i = 0; i < len; i++) {
			ApplicationInfo app = installed.get(i);
			
			String packageName = app.packageName;
			String meName = context.getPackageName();
			if (!meName.equals(packageName)
					&& !packageName.startsWith("com.android.")
					&& !packageName.startsWith("com.google.android.")) {
				if (!failedAppList.contains(packageName)) {
					if (!openApps.contains(packageName)) {
						if (installed_app_str.indexOf(packageName) == -1) {
							packageNames.append(packageName);
							if (i < len - 1) {
								packageNames.append(split);
							}
						}
					}
				}
			}
		}
		String newInstalledApps = packageNames.toString() + "";
		//return "&" + MainConstants.KEY_PACKAGE_NAMES + "=" + newInstalledApps;
		return MainConstants.KEY_PACKAGE_NAMES + "=" + newInstalledApps;
	}

	static void saveAddedPacnames(String newInstalledApps) {
		Properties logProps = SDPropertiesUtils
				.getProperties(MainConstants.PACKAGE_NAME_FILE);
		String installed_app_str = logProps.getProperty(key_installed_app);
		if(installed_app_str!=null && !installed_app_str.trim().equals("")){
			if(installed_app_str.indexOf(newInstalledApps)==-1){
				logProps.put(key_installed_app, installed_app_str + split
						+ newInstalledApps);
			}
		}else{
			logProps.put(key_installed_app,newInstalledApps);
		}
		SDPropertiesUtils
		.saveMessage(logProps, MainConstants.PACKAGE_NAME_FILE);
	}

	static ArrayList<String> getFailedAppList() {
		Properties logProps = SDPropertiesUtils
				.getProperties(MainConstants.PACKAGE_NAME_FILE);
		String failedAppListStr = logProps
				.getProperty(MainConstants.APP_OPEN_FAILED_PACKAGE_NAME_LIST);
		ArrayList<String> failedAppList = new ArrayList<String>();
		if (failedAppListStr == null || failedAppListStr.trim().equals("")) {
			return failedAppList;
		} else {
			String[] appNameArr = failedAppListStr.split(split);
			for (int i = 0; i < appNameArr.length; i++) {
				failedAppList.add(appNameArr[i]);
			}
			return failedAppList;
		}
	}

	/**
	 * @date: 2011-12-19
	 * @Description: If the user don not used enough time after who download the
	 *               app , we stop the check process and record the app
	 *               packagename,so ,we will delete this packagename when the
	 *               next time we send installed packagename list to the
	 *               service.
	 */
	static void addFailedOpenName(String appName) {
		Properties logProps = SDPropertiesUtils
				.getProperties(MainConstants.PACKAGE_NAME_FILE);
		String failedAppListStr = logProps
				.getProperty(MainConstants.APP_OPEN_FAILED_PACKAGE_NAME_LIST);
		if (failedAppListStr == null || failedAppListStr.trim().equals("")) {
			failedAppListStr = split+appName;
		} else {
			if (!failedAppListStr.contains(appName)) {
				failedAppListStr += split;
				failedAppListStr += appName;
			}
		}
		logProps.put(MainConstants.APP_OPEN_FAILED_PACKAGE_NAME_LIST,
				failedAppListStr);
		SDPropertiesUtils
				.saveMessage(logProps, MainConstants.PACKAGE_NAME_FILE);
	}

	static void removeFailedOpenName(String appName) {
		Properties logProps = SDPropertiesUtils
				.getProperties(MainConstants.PACKAGE_NAME_FILE);
		String failedAppListStr = logProps
				.getProperty(MainConstants.APP_OPEN_FAILED_PACKAGE_NAME_LIST);
		if (failedAppListStr == null || failedAppListStr.trim().equals("")) {
			return;
		}
		failedAppListStr = failedAppListStr.replaceAll(split + appName, "");
		logProps.put(MainConstants.APP_OPEN_FAILED_PACKAGE_NAME_LIST,
				failedAppListStr);
		SDPropertiesUtils
				.saveMessage(logProps, MainConstants.PACKAGE_NAME_FILE);
	}

}