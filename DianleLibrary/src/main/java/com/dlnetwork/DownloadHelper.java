package com.dlnetwork;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadHelper {
	private static final int NOTIFICATION_DOWNLOAD_END_ICON_ID = 17301634;
	private static int resId = 10000;
	private Context context;
	private LinkedList<DownloadThreadInfo> threadInfo = new LinkedList<DownloadThreadInfo>();
	/* 已下载文件长度 */
	private int downloadSize = 0;

	/* 原始文件长度 */
	private int fileSize = 0;

	/* 线程数 */
	private DownloadThread[] threads;

	/* 本地保存文件 */
	private File saveFile;

	/* 缓存各线程下载的长度 */
	private Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();

	/* 每条线程下载的长度 */
	private int block;

	/* 下载路径 */
	private String downloadUrl;
	/* 下载APP的包名 */
	private String packageName;

	/* 重试次数 */
	private int retryCount = 0;

	/* 通知栏 */
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private static int index = 1;
	private int mId;
	private String appName;
	// 积分墙1,静默下载2，泡泡3
	private int downloadType = 0;

	/* 获取线程数 */
	public int getThreadSize() {
		return threads.length;
	}

	/* 获取文件大小 */
	public int getFileSize() {
		return fileSize;
	}

	/* 累计已下载大小 */
	protected synchronized void append(int size) {
		downloadSize += size;
		// Log.i("size", downloadSize + "");
	}

	/**
	 * 更新指定线程最后下载的位置
	 * 
	 * @param threadId
	 *            线程id
	 * @param pos
	 *            最后下载的位置
	 */
	protected synchronized void update(int threadId, int pos,
			DownloadThreadInfo threadDownsize) {
		this.data.put(threadId, pos);
	}

	/**
	 * 
	 * @param downloadUrl
	 *            下载路径
	 * @param fileSaveDir
	 *            文件保存目录
	 * @param threadNum
	 *            下载线程数
	 */
	public DownloadHelper(Context context, String downloadUrl, String saveName,
			String title, int downType) {
		this.context = context;
		this.downloadUrl = downloadUrl;
		this.packageName = saveName;
		this.appName = title;
		this.downloadType = downType;
	}

	/**
	 * 开始下载文件
	 * 
	 * @return 已下载文件大小
	 * @throws Exception
	 */
	public boolean download(int threadNum) {
		String tempPath = SDPropertiesUtils.getSDPath() + "/download";
		File tempFile = new File(tempPath);
		if (!tempFile.exists()) {
			tempFile.mkdirs();
		}
		File localFile = Environment.getExternalStorageDirectory();
		String downPath = (localFile.getParent() + "/" + localFile.getName() + "/download");

		// 通知栏下载提示
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = new Notification(NOTIFICATION_DOWNLOAD_END_ICON_ID,
				this.appName + ",准备下载!", System.currentTimeMillis());
		mId = index++;
		// mNotificationManager.notify(mId, mNotification);
		mNotification.setLatestEventInfo(context, this.appName, "正在等待下载", null);
		if (!appName.trim().equals("")) {
			notifyText("正在等待下载", appName);
		}
		this.threads = new DownloadThread[threadNum];
		this.saveFile = new File(downPath, this.packageName);
		if (redirectHandler()) {
			// 计算每条线程下载的数据长度
			this.block = (this.fileSize % this.threads.length) == 0 ? this.fileSize
					/ this.threads.length
					: this.fileSize / this.threads.length + 1;
		} else {
			return false;
		}
		try {
			int currentTotal;
			RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rw");
			if (this.fileSize > 0)
				randOut.setLength(this.fileSize);
			randOut.close();
			URL url = new URL(this.downloadUrl);
			// Log.i("PaoPao", this.downloadUrl);
			if (this.data.size() != this.threads.length) {
				this.data.clear();
				for (int i = 0; i < this.threads.length; i++) {
					this.data.put(i + 1, 0);// 初始化每条线程已经下载的数据长度为0
				}
			}

			for (int i = 0; i < this.threads.length; i++) {// 开启线程进行下载
				int downLength = this.data.get(i + 1);

				if (downLength < this.block
						&& this.downloadSize < this.fileSize) {// 判断线程是否已经完成下载,否则继续下载
					this.threads[i] = new DownloadThread(this, url,
							this.saveFile, this.block, this.data.get(i + 1),
							i + 1);
					this.threads[i].setPriority(10);
					this.threads[i].start();
				} else {
					this.threads[i] = null;
				}
			}
			if (!appName.trim().equals("")) {
				threadInfo = Utils.getThreadList(context, "threadDownSize");
				DownloadThreadInfo downsizeInfo = new DownloadThreadInfo();
				downsizeInfo.addAttribute("pack", this.packageName);
				threadInfo.add(downsizeInfo);
				Utils.setPreferenceStr(context, "threadDownSize",
						Utils.listToStr(threadInfo));
			}
			boolean notFinish = true;// 下载未完成
			// operationQueue();
			while (notFinish) {// 循环判断所有线程是否完成下载
				Thread.sleep(1000);
				notFinish = false;// 假定全部线程下载完成
				for (int i = 0; i < this.threads.length; i++) {
					if (this.threads[i] != null && !this.threads[i].isFinish()) {// 如果发现线程未完成下载
						notFinish = true;// 设置标志为下载没有完成
						if (this.threads[i].getDownLength() == -1) {// 如果下载失败,再重新下载
							if (retryCount >= 60) {
								if (!appName.trim().equals("")) {
									notifyText("下载失败", appName);
								}
								notFinish = false;// 设置标志为下载结束 出现异常
								return false;
							}
							this.threads[i] = new DownloadThread(this, url,
									this.saveFile, this.block,
									this.data.get(i + 1), i + 1);
							this.threads[i].setPriority(10);
							this.threads[i].start();
							retryCount++;
						} else if (this.threads[i].getDownLength() == 1) {
							if (!appName.trim().equals("")) {
								notifyText("下载失败", appName);
							}
							notFinish = false;// 设置标志为下载结束 出现异常
							return false;
							// break;
						}
					}
				}
				// 通知目前已经下载完成的数据长度
				currentTotal = (int) ((this.downloadSize * 1.0F / this.fileSize) * 100);
				if (!appName.trim().equals("")) {
					notifyText(String.format("已经下载%d%%", currentTotal), appName);
				} else {
					if (downloadType != 2) {
						DevNativeService.progress = currentTotal;
					}
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			if (!appName.trim().equals("")) {
				notifyText("下载中断", appName);
			}
			return false;
		}
		File file = new File(downPath, packageName);
		if (!appName.trim().equals("")) {
			setNotifOnclickListener(mNotification, file);
		}
		threadInfo = Utils.getThreadList(context, "threadDownSize");
		for (int j = 0; j < threadInfo.size(); j++) {
			if (threadInfo.get(j).getPackageName().equals(this.packageName)) {
				threadInfo.remove(j);
				Utils.setPreferenceStr(context, "threadDownSize",
						Utils.listToStr(threadInfo));
				break;
			}
		}
		return true;
	}

	void notifyText(String text, String appname) {
		mNotification.contentView.setTextViewText(
				getAndroidLayID(context, "com.android.internal.R$id", "text"),
				text);
		Intent intent = new Intent();
		mNotification.contentIntent = PendingIntent.getActivity(context,
				resId++, intent, 0);
		mNotificationManager.notify(mId, mNotification);
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
				"点击安装");

		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
		n.icon = 17301633 + 1;
		this.mNotificationManager.notify(mId, mNotification);
	}

	private Boolean redirectHandler() {
		URL url;
		HttpURLConnection conn;
		boolean connectStatus = false;
		try {
			url = new URL(this.downloadUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(30 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept-Encoding", "identity");
			conn.setFollowRedirects(true);
			conn.connect();
			if (conn.getResponseCode() == 200) {
				this.fileSize = conn.getContentLength();
				if (this.fileSize <= 0) {
					if (!appName.trim().equals("")) {
						notifyText("网络异常", appName);
					}
				} else {
					connectStatus = true;
				}
			} else if (conn.getResponseCode() == 404) {
				if (!appName.trim().equals("")) {
					notifyText("资源暂时不存在", appName);
				}
			} else {
				if (!appName.trim().equals("")) {
					notifyText("网络异常", appName);
				}
			}
		} catch (Exception e) {
			if (!appName.trim().equals("")) {
				notifyText("网络异常", appName);
			}
			return connectStatus;
		}
		return connectStatus;
	}
}
