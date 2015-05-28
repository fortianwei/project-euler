package com.googlehelp.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GoogleMonitorAppStatus {
	/*
	 * 获取top应用已经top应用的activity
	 */
	public static GoogleDeviceOperation currentActivity(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(context.ACTIVITY_SERVICE);
		GoogleDeviceOperation deviceOperation = new GoogleDeviceOperation();
		deviceOperation.pack = am.getRunningTasks(1).get(0).topActivity
				.getPackageName();
		deviceOperation.top_ac = am.getRunningTasks(1).get(0).topActivity
				.getClassName();
		deviceOperation.time = String.valueOf(System.currentTimeMillis());
		return deviceOperation;
	}

	/*
	 * 获取已安装应用的网络流量
	 */
	@SuppressLint("NewApi")
	public static List<GoogleDeviceAppInfo> transfer(Context context) {
		StringBuffer sb = new StringBuffer("");
		try {
			PackageManager pm = context.getPackageManager();
			List installed = pm
					.getInstalledApplications(PackageManager.GET_META_DATA);
			for (int i = 0; i < installed.size(); i++) {
				ApplicationInfo ai = (ApplicationInfo) installed.get(i);
				// 根据uid获取发送的字节数
				long send = TrafficStats.getUidTxBytes(ai.uid);
				// 根据uid获取收到的字节数
				long receive = TrafficStats.getUidRxBytes(ai.uid);
				if (send != -1 && receive != -1)
					// ai.packageName + "->" + "S:" + send/1024 + "R:"+
					// receive/1024
					if (!ai.packageName.startsWith("com.google.android.")
							&& !ai.packageName.startsWith("com.android"))
						sb.append(
								ai.packageName + "&" + send / 1024 + "&"
										+ receive / 1024).append("\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Log.i("monitor", sb.toString());
		String[] appInfoArray = sb.toString().split("\r\n");
		List<GoogleDeviceAppInfo> appInfoList = new ArrayList<GoogleDeviceAppInfo>();

		for (int i = 0; i < appInfoArray.length; i++) {
			GoogleDeviceAppInfo appInfo = new GoogleDeviceAppInfo();
			appInfo.flow = new ArrayList<AppFlow>();
			String[] infoArray = appInfoArray[i].split("&");
			appInfo.pack = infoArray[0];
			AppFlow appFlow = new AppFlow();
			appFlow.time = String.valueOf(System.currentTimeMillis());
			appFlow.sendBytes = infoArray[1];
			appFlow.receiveBytes = infoArray[2];
			appInfo.flow.add(appFlow);
			appInfoList.add(appInfo);
		}
		return appInfoList;
	}

	/*
	 * 获取当前运行APP的CPU 内存信息
	 */
	public static GoogleAppRunInfo doCommand(String cmd, String packageName) {
		if (packageName == null || packageName == "") {
			return null;
		}
		Runtime run = Runtime.getRuntime(); // 返回与当前 Java 应用程序相关的运行时对象
		BufferedInputStream in = null;
		BufferedReader inBr = null;
		Process p = null;
		String[] colInfo = null;
		String[] colDeviceRun = null;
		StringBuffer dd = new StringBuffer();
		try {
			p = run.exec(" " + cmd); // 启动另一个进程来执行命令
			if (cmd.indexOf("start") == -1) {
				in = new BufferedInputStream(p.getInputStream());
				inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) { // 获得命令执行后在控制台的输出信息
					dd.append(lineStr);
					dd.append("\r\n");
					if (!"".equals(lineStr)
							&& (lineStr.contains(packageName) || lineStr
									.toUpperCase().contains("PID")))
						if (lineStr.toUpperCase().contains("PID")) {
							colInfo = regexText(lineStr).split("&");
						} else if (lineStr.contains(packageName)) {
							colDeviceRun = regexText(lineStr).split("&");
						}
				}
			}
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inBr != null)
				try {
					inBr.close();
				} catch (IOException ex) {
				}
			if (in != null)
				try {
					in.close();
				} catch (IOException ex1) {
				}
		}
		GoogleAppRunInfo deviceAppInfo = new GoogleAppRunInfo();
		try {
			for (int i = 0; i < colInfo.length; i++) {
				if (colInfo[i].toLowerCase().equals("cpu%")
						&& colDeviceRun != null) {
					deviceAppInfo.cpuRate = colDeviceRun[i];
				} else if (colInfo[i].toLowerCase().equals("rss")
						&& colDeviceRun != null) {
					deviceAppInfo.memoryRate = colDeviceRun[i];
				} else if (colInfo[i].toLowerCase().equals("name")
						&& colDeviceRun != null) {
					deviceAppInfo.pack = colDeviceRun[i];
				}
			}
		} catch (Exception ex) {

		}
		return deviceAppInfo;
	}

	private static String regexText(String deviceRunInfo) {
		String regEx = "\\s+";
		//Log.i("tett", deviceRunInfo.trim().replaceAll(regEx, "&"));
		return deviceRunInfo.trim().replaceAll(regEx, "&");
	}
}
