package com.dlnetwork;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

final class NotifiMana extends Thread {
	private static final int NOTIFICATION_DOWNLOADING_ICON_ID = 17301633;
	private static final int NOTIFICATION_DOWNLOAD_END_ICON_ID = 17301634;

	private Context context;
	private String downPath;
	private String downUrl;
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private static int index = 1;
	private int mId;
	long mThreadAliveTime = System.currentTimeMillis();
	private static HashMap<String, Integer> noticMap = new HashMap<String, Integer>();
	private String currentPageUrl;
	private static int resId = 10000;
	private boolean downloading = false; // volatile
	private double temp;

	void stopDownloading() {
		downloading = false;
		temp = 0;

	}

	NotifiMana(Context context, String downUrl, String title) {
		try {
			createNotific(context, downUrl, title);
		} catch (Exception localException) {
		}
	}
	private void createNotific(Context context1, String downUrl, String title) {
		this.context = context1;
		File localFile = Environment.getExternalStorageDirectory();
		this.downPath = (localFile.getParent() + "/" + localFile.getName() + "/download");
		this.downUrl = downUrl;
		mNotificationManager = (NotificationManager) context1
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = new Notification(NOTIFICATION_DOWNLOAD_END_ICON_ID,
				title + ",准" + " ".trim() + "备" + " ".trim() + "下" + " ".trim() + "载!", System.currentTimeMillis());
		mId = index++;
		mNotification.setLatestEventInfo(context1, title, "正" + " ".trim() + "在等" + " ".trim() + "待下" + " ".trim() + "载", null);
	}

	void notifyText(String text, String appname) {
		mNotification.contentView.setTextViewText(
				getAndroidLayID(context, "com.android.internal.R$id", "text"),
				text);
		Intent intent = new Intent();
		/*
		 * 取消下载用户手动取消下载功能 if (Dianle.activityClass != DianleOfferActivity.class)
		 * { intent.setClass(context, Dianle.activityClass); } else if
		 * (Dianle.activityClass != OfferWallActivity.class) {
		 * intent.setClass(context, Dianle.activityNativeClass); }
		 */
		intent.putExtra("userStop", true);
		intent.putExtra("title", appname);
		intent.putExtra("text", text);
		intent.putExtra("mId", mId);
		// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mNotification.contentIntent = PendingIntent.getActivity(context,
				resId++, intent, 0);
		if (downloading) {
			mNotificationManager.notify(mId, mNotification);
		}
	}

	HttpURLConnection buildConnection(long start) throws IOException {
		//Log.i("url",this.downUrl);
		URL localURL = new URL(this.downUrl);
		// Log.i("urlTest", this.downUrl);
		HttpURLConnection connection = (HttpURLConnection) localURL
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setFollowRedirects(true);
		connection.setConnectTimeout(5000);
		connection.setRequestProperty("Accept-Encoding", "identity");
		connection.setRequestProperty("Range", "bytes=" + start + "-");
		return connection;
	}

	boolean download(String path, final String appname) {
		Timer timerTask = new Timer();
		try {
			downloading = true;
			// if it exists, download from the end
			File file = new File(this.downPath, path);
			long start = 0;
			if (file.exists()) {
				// if completed, return success
				if (Utils.checkPackageCompleted(context, path))
					return true;
				start = file.length();
			}
			// notifyText("正在建立连接，请稍候", appname+"分隔符"+start);
			timerTask.schedule(new TimerTask() {
				int count = 1;

				@Override
				public void run() {

					temp = Math.floor((1 - Math.exp(-0.01 * count)) * 100);
					notifyText(
							"已" + " ".trim() + "经下" + " ".trim() + "载"
									+ String.valueOf(
											Math.floor((1 - Math.exp(-0.01
													* count)) * 100)).split(
											"[.]")[0] + "%", appname);
					if (Math.floor((1 - Math.exp(-0.01 * count)) * 100) >= 99.0)
						cancel();
					count++;
					/*
					 * if (!downloading) { cancel(); temp = 0;
					 * mNotificationManager.cancel(mId); }
					 */
				}
			}, 1000, 1000);
			InputStream localInputStream = null;
			int i1 = 0;
			int i2 = (int) start;
			int i3 = 0;
			int currentTemp = 0;
			try {
				HttpURLConnection connection = buildConnection(start); // 这个时候，虽然连接并未真正建立，只是做了初始化。还是初始的url
				// 貌似先取长度，重定向到dnion-cdn，而直接取inputstream，总是跑到一个没有内容的节点。获取实际信息的时候，会进行重定向。但是先取长度好一些！
				currentTemp = connection.getContentLength();
				i3 = (int) start + connection.getContentLength();
				localInputStream = connection.getInputStream();
			} catch (IOException e) {// if the file of this url is unreachable,
										// the connection could be created, but
										// inputStream will throw an exception
										// of filenotfound
				Log.e("network", "try again to connect");
				//Log.i("network——one",e.toString());
				HttpURLConnection connection = buildConnection(start);
				// if it fails, try again at once
				i3 = (int) start + connection.getContentLength();
				localInputStream = connection.getInputStream();
			}
			if (currentTemp == -1) {
				Log.e("network", "try again to connect");
				HttpURLConnection connection = buildConnection(start);
				// if it fails, try again at once
				i3 = (int) start + connection.getContentLength();
				localInputStream = connection.getInputStream();
			}
			/*
			 * if (!downloading) { // user stops when connecting temp = 0;
			 * timerTask.cancel(); mNotificationManager.cancel(mId); return
			 * false; }
			 */
			int i4 = (int) (i2 * 100.0F / i3);
			FileOutputStream localFileOutputStream = new FileOutputStream(file,
					true); // append
			byte[] arrayOfByte = new byte[1024 * 4];
			mThreadAliveTime = System.currentTimeMillis();
			long lastNotifyTime = System.currentTimeMillis();
			try {
				while (downloading
						&& (i1 = localInputStream.read(arrayOfByte)) > 0) {
					mThreadAliveTime = System.currentTimeMillis();
					localFileOutputStream.write(arrayOfByte, 0, i1);
					i2 += i1; // i3是总长度 i2是开始值吧 i1是当前读到的字节数
					i4 = (int) (i2 * 100.0F / i3);
					long now = mThreadAliveTime;
					if (now - lastNotifyTime > 500l) {// notify的最小时间间隔是3秒
						lastNotifyTime = now;
						if (i4 > temp) {
							temp = 0;
							timerTask.cancel();
							notifyText(String.format("已" + " ".trim() + "经下" + " ".trim() + "载%d%%", i4), appname);
						}
					}
				}
			} catch (IOException e) {
				//Log.i("network——two",e.toString());
				e.printStackTrace();
			} finally {
				localInputStream.close();
				localFileOutputStream.close();
				downloading = false;
				temp = 0;
				timerTask.cancel();
			}
			Thread.sleep(200);
			setNotifOnclickListener(mNotification, file);
			saveNoticID(file);
		} catch (Exception e) {
			//Log.i("network——three",e.toString());
			temp = 0;
			timerTask.cancel();
			String errorMessage = "下" + " ".trim() + "载中" + " ".trim() + "断";
			if (e instanceof MalformedURLException) {
				errorMessage += ":URL错" + " ".trim() + "误";
			} else if (e instanceof FileNotFoundException) {
				errorMessage += ":文" + " ".trim() + "件" + " ".trim() + "错" + " ".trim() + "误";
			} else if (e instanceof IOException) {
				errorMessage += ":打" + " ".trim() + "开连" + " ".trim() + "接错" + " ".trim() + "误";
			} else if (e instanceof InterruptedException) {
				errorMessage += ":中" + " ".trim() + "断错" + " ".trim() + "误";
			} else if (e instanceof ProtocolException) {
				errorMessage += ":协" + " ".trim() + "议错" + " ".trim() + "误";
			} else {
				errorMessage += ":请" + " ".trim() + "检查" + " ".trim() + "S" + " ".trim() + "D卡";
			}
			mNotification.contentView.setTextViewText(
					getAndroidLayID(context, "com.android.internal.R$id",
							"text"), errorMessage);
			Intent i = new Intent(context, DevNativeActivity.class);
			Bundle b = new Bundle();
			b.putString(MainConstants.ON_DOWNLOAD_FAILED_RELOAD_PAGE_KEY,
					getCurrentPageUrl());
			i.putExtras(b);
			mNotification.contentIntent = PendingIntent.getActivity(context,
					resId++, i, 0);
			mNotificationManager.notify(mId, mNotification);
			return false;
		}
		return true;
	}

	static int getAndroidLayID(Context context, String className, String name) {
		try {
			@SuppressWarnings("rawtypes")
			Class localClass = Class.forName(className);
			Field localField = localClass.getField(name);
			int value = Integer.parseInt(localField.get(localField.getName())
					.toString());
			return value;
		} catch (Exception localException) {
			return 0;
		}
	}

	private void saveNoticID(File file) {
		String pn = getPackageInfo(context, file.getAbsolutePath()).get(
				"packageName");
		getNoticMap().put(pn, mId);
	}

	private void setNotifOnclickListener(Notification n, File f) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(f),
				"application/vnd.android.package-archive");
		mNotification.contentIntent = PendingIntent.getActivity(this.context,
				0, intent, 0);
		mNotification.contentView.setTextViewText(
				getAndroidLayID(context, "com.android.internal.R$id", "text"),
				"点" + " ".trim() + "击安" + " ".trim() + "装");

		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
		n.icon = 17301633 + 1;
		this.mNotificationManager.notify(mId, mNotification);
	}

	static HashMap<String, String> getPackageInfo(Context context,
			String filePath) {
		HashMap<String, String> infoMap = new HashMap<String, String>();
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(filePath,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			String packageName = appInfo.packageName;
			infoMap.put("packageName", packageName);
			String versionName = info.versionName;
			infoMap.put("versionName", versionName);
			int versionCode = info.versionCode;
			infoMap.put("versionCode", versionCode + "");
			return infoMap;
		} else {
			return null;
		}
	}

	public synchronized static HashMap<String, Integer> getNoticMap() {
		return noticMap;
	}

	public void setCurrentPageUrl(String currentPageUrl) {
		this.currentPageUrl = currentPageUrl;
	}

	public String getCurrentPageUrl() {
		return currentPageUrl;
	}
}
