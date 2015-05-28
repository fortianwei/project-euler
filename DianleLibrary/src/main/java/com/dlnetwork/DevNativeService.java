package com.dlnetwork;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DevNativeService extends Service {
	/** the app list that has downed */
	private LinkedList<DownedApp> downedAppList = new LinkedList<DownedApp>();
	/** the app list that is downing */
	private LinkedList<DownedApp> downingAppTaskList = new LinkedList<DownedApp>();
	/** the abroad app list that is clicked , but not active enough time */
	private static LinkedList<DownedApp> abroadOfferList = new LinkedList<DownedApp>();
	/** the app list that is opened , but not active enough time */
	private LinkedList<DownedApp> openAppTaskList = new LinkedList<DownedApp>();
	public static LinkedList<DownedApp> openAppTipList = new LinkedList<DownedApp>();
	public static LinkedList<DownedApp> resfuseTaskList = new LinkedList<DownedApp>();
	public static DownedApp downedApp = null;
	private static Executor threadPool;
	private static IntentFilter receiveSystemIntentFilter;
	private static IntentFilter receiveDefinedIntentFilter;
	private static IntentFilter receiveInstallTipsIntentFilter;
	Intent startIntent;
	private static boolean trialStatus = false;

	/** 浮窗SDK所需变量 */
	private CustomProgressBar view;
	private boolean isContain = false;
	private boolean doubleClick = false;
	private static List<FloatModel> floatList = new ArrayList();
	public static List<FloatModel> quietList = new ArrayList<FloatModel>();
	RelativeLayout mFloatLayout;
	WindowManager.LayoutParams wmParams;
	WindowManager mWindowManager;
	public static int progress = 0;
	public static final String ACTIVE_LISTS = "al";
	public static final String SHOW_TOP_PACK = "stp";
	/**
	 * 用来排除XPDialog是否弹两次的状况
	 * */
	private boolean isXpShow = false;

	/** 以上是浮窗SDK所需变量 */

	@Override
	public final IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public final void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		// Log.i("service_start", "starting");
		if (MainConstants.step == 1) {
			MainConstants.className += "onStart";
			MainConstants.step++;
		}
		startIntent = intent;
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		unregisterReceiver(receiveData);
		unregisterReceiver(instalTips);
		if (startIntent != null) {
			startService(startIntent);
		}
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		// Log.i("service_create", "creating");
		if (MainConstants.step == 0) {
			MainConstants.className += "onCreate";
			MainConstants.step++;
		}
		// receiver to response the message of the activity
		initReceive();
		getPreferences();
		threadPool = Executors.newFixedThreadPool(1);
		openPackageNotOpened();
		handleThreadCache();
		/** 浮窗SDK */
		int ok = PackageManager.PERMISSION_GRANTED;
		if (getPackageManager().checkPermission(
				"android.permission.SYSTEM_ALERT_WINDOW", getPackageName()) == ok) {
			initFloat();
			quietDownload();
		}
	}

	private void initReceive() {
		receiveSystemIntentFilter = new IntentFilter(
				ServerParams.DOWNDATA_RECEIVE);
		receiveSystemIntentFilter.addDataScheme("package");
		receiveSystemIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		receiveSystemIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		receiveSystemIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		registerReceiver(receiveData, receiveSystemIntentFilter);

		receiveDefinedIntentFilter = new IntentFilter();
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.ADD_DOWN_APP);
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.STOP_DOWNLOADING_APP);
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.ADD_OPEN_APP);
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.DOWNEDAPP_TIP);
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.CIRCLE_TIMER);
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.DOWNEDAPP_TASK_TIP);
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.ABROAD_OFFER);
		receiveDefinedIntentFilter.addAction(DevNativeService.this
				.getPackageName() + "." + ServerParams.ADD_ABROAD_POINTS);
		registerReceiver(receiver, receiveDefinedIntentFilter);

		receiveInstallTipsIntentFilter = new IntentFilter();
		receiveInstallTipsIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(instalTips, receiveInstallTipsIntentFilter);
	}

	private void asyncDownAppTask(final DownedApp app) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					String path = SDPropertiesUtils.getSDPath();
					NotifiMana notifiMana = app.mNotifiMana;
					notifiMana.mThreadAliveTime = System.currentTimeMillis();
					boolean isDownSuccess = new DownloadHelper(
							DevNativeService.this, app.getUrl(), app
									.getPackageName() + ".apk", app.getName(),
							1).download(1);
					// boolean
					// isDownSuccess=notifiMana.download(app.getPackageName() +
					// ".apk", app.getName());
					if (!isDownSuccess) {
						handleThreadCache();
						return;
					}
					// send out the downOk message
					ServiceConnect.sendDownOK(DevNativeService.this,
							app.getAdID(), app.getAppId());
					// install the package
					File file = new File(path + "/download", app
							.getPackageName() + ".apk");
					if (file.exists()
							&& Utils.checkPackageCompleted(
									DevNativeService.this, file.getPath())) {
						app.addAttribute(MainConstants.AD_DOWN_OK,
								String.valueOf(System.currentTimeMillis()));
						app.addAttribute(MainConstants.AD_SHOW_STATUS, "0");
						//downedAppList去重
						for (int i = 0; i < downedAppList.size(); i++) {
							if (downedAppList.get(i).getPackageName()
									.equals(app.getPackageName())) {
								downedAppList.remove(i);
								break;
							}
						}
						downedAppList.add(app);
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// Log.i("remove", "remove");
					for (int temp = 0; temp < downingAppTaskList.size(); temp++) {
						if (downingAppTaskList.get(temp).getPackageName()
								.equals(app.getPackageName())) {
							downingAppTaskList.remove(temp);
							setPreferences();
							break;
						}
					}
				}
			}
		});
	}

	private synchronized void handleThreadCache() {
		LinkedList<DownloadThreadInfo> threadInfo = Utils.getThreadList(this,
				"threadDownSize");
		if (threadInfo.size() > 0) {
			for (int i = 0; i < threadInfo.size(); i++) {
				String packageName = threadInfo.get(i).getPackageName();
				String tempPath = SDPropertiesUtils.getSDPath() + "/download";
				File tempFile = new File(tempPath, packageName);
				if (tempFile.exists()) {
					tempFile.delete();
				}
				threadInfo.remove(i);
			}
			// threadInfo.clear();
			Utils.setPreferenceStr(this, "threadDownSize", "");
		}
	}

	synchronized void setPreferences() {
		String listStr = Utils.listToStr(openAppTaskList);
		Utils.setPreferenceStr(this, "openAppTaskList", listStr);
		// Utils.setPreferenceStr(this,
		// "downingAppTaskList",Utils.listToStr(downingAppTaskList));
		Utils.setPreferenceStr(this, "downedAppList",
				Utils.listToStr(downedAppList));
	}

	void getPreferences() {
		openAppTaskList = Utils.getList(this, "openAppTaskList");
		// downingAppTaskList = Utils.getList(this, "downingAppTaskList");
		downedAppList = Utils.getList(this, "downedAppList");
	}

	boolean appInList(LinkedList<DownedApp> list, DownedApp app) {
		//&& list.get(i).getTaskType() == app.getTaskType()
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getPackageName().equals(app.getPackageName())) {
				// list.get(i).handler = app.handler;
				return true;
			}
		}
		return false;
	}

	void addOpenApp(DownedApp app) {
		// 深度任务激活 假如当用户当前直接打开的是深度任务，在这里直接发送此深度任务的激活,并且不能添加到openAppTaskList这个列表里
		for (int i = 0; i < openAppTaskList.size(); i++) {
			if (openAppTaskList.get(i).getPackageName()
					.equals(app.getPackageName())) {
				openAppTaskList.remove(i);
				break;
			}
		}
		if (Build.VERSION.SDK_INT >= 21) {
			DianleURLConnection.addFailedOpenName(app.getPackageName());
			sendAdNotify(app, DevNativeService.this);
			return;
		}
		// app.getTaskType()==-1 激活 app.getTaskType()==0 签到 app.getTaskType()==1
		// 试用一定时间 app.getTaskType()==2 多少天不卸载
		if (app.getTaskType() == 1) {
			openAppTaskList.add(app);
			setPreferences();
			// DianleURLConnection.addFailedOpenName(app.getPackageName());
		} else if (app.getDateDiff() > 0
				&& (app.getTaskType() == 0 || app.getTaskType() == 2)) {
		} else {
			openAppTaskList.add(app);
			setPreferences();
			DianleURLConnection.addFailedOpenName(app.getPackageName());
		}
	}

	void stopDownloading(String title, int notificationId) {
		for (int j = 0; j < downingAppTaskList.size(); j++) {
			DownedApp app = downingAppTaskList.get(j);
			if (app.getName().equals(title)) {
				app.mNotifiMana.stopDownloading();
				if (notificationId >= 0)
					((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
							.cancel(notificationId);
				// downingAppTaskList.remove(j);
				break;
			}
		}
	}

	synchronized void downloadApp(DownedApp app) {
		String path = SDPropertiesUtils.getSDPath();
		File tmpFile = new File(path + "/download");
		if (!tmpFile.exists() && !tmpFile.mkdirs()) {
			return;
		}
		// check if it exists in the sdcard
		File file = new File(path + "/download", app.getPackageName() + ".apk");
		if (file.exists() && Utils.checkPackageCompleted(this, file.getPath())) {
			installApkFile(file, app);
			// app.addAttribute(MainConstants.AD_OPENTIME, "1");
			// downedAppList.add(app);
			int temp = 0;
			for (int i = 0; i < downedAppList.size(); i++) {
				if (app.getPackageName().equals(
						downedAppList.get(i).getPackageName())) {
					downedAppList.get(i).addAttribute(
							MainConstants.AD_OPENTIME, "1");
					temp = 1;
					break;
				}
			}
			if (temp == 0) {
				app.addAttribute(MainConstants.AD_OPENTIME, "1");
				downedAppList.add(app);
			}
			setPreferences();
			return;
		}
		try {
			if (appInList(downingAppTaskList, app)) {
				Toast.makeText(
						this,
						app.getName() + " 已加" + " ".trim() + "入下" + " ".trim()
								+ "载任" + " ".trim() + "务中或已下" + " ".trim()
								+ "载完" + " ".trim() + "成！", Toast.LENGTH_LONG).show();
				return;
			}
			Toast.makeText(
					this,
					app.getName() + "已加" + " ".trim() + "入下" + " ".trim()
							+ "载队" + " ".trim() + "列...请稍候...", Toast.LENGTH_LONG).show();
			// Log.i("url",app.getUrl());
			DianleURLConnection.addFailedOpenName(app.getPackageName());
			app.mNotifiMana = new NotifiMana(this, app.getUrl(), app.getName());
			app.mNotifiMana.setCurrentPageUrl(app.getCurrentUrl());
			downingAppTaskList.add(app);
			setPreferences();
			asyncDownAppTask(app);
		} catch (Exception e) {
			// Log.e("exception", e.toString());
		}
	}

	private void openPackageNotOpened() {
		new Timer().schedule(new TimerTask() {
			long nowTime;

			@Override
			public void run() {
				// Log.i("timer", "timer is running");
				if (trialStatus
						&& System.currentTimeMillis() - nowTime > 60 * 1000) {
					trialStatus = false;
				}
				if (!downedAppList.isEmpty() && !trialStatus) {
					for (int i = 0; i < downedAppList.size(); i++) {
						// Log.i("package",
						// downedAppList.get(i).getPackageName()+
						// downedAppList.get(i).getOpenTime());
						if (downedAppList.get(i).getOpenTime().equals("0")) {
							nowTime = System.currentTimeMillis();
							String path = SDPropertiesUtils.getSDPath();
							File file = new File(path + "/download",
									downedAppList.get(i).getPackageName()
											+ ".apk");
							trialStatus = true;
							downedAppList.get(i).addAttribute(
									MainConstants.AD_OPENTIME, "1");
							setPreferences();
							installApkFile(file, downedAppList.get(i));
							break;
						}
					}
				}
				// 打开X秒后才给分
				ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
				ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
				String name = cn.getPackageName();
				if (!openAppTaskList.isEmpty()) {
					for (int i = 0; i < openAppTaskList.size(); i++) {
						DownedApp downedApp = openAppTaskList.get(i);
						if (downedApp.getPackageName().equals(name)) {
							if (downedApp.getStartTime() == 0
									&& downedApp.getTaskParams() < 0) {
								computerTimer(downedApp);
							}
							openAppTaskList.get(i).addAttribute(
									MainConstants.AD_STARTTIME, "1");
							trialStatus = true;
							setPreferences();
							if (downedApp.getTaskType() == 1) {
								if (downedApp.activeSeconds == 0) {
									String appActiveSeconds = Utils
											.getPreferenceStr(
													DevNativeService.this,
													downedApp.getPackageName());
									if (appActiveSeconds != "") {
										downedApp.activeSeconds = Integer
												.parseInt(Utils
														.getPreferenceStr(
																DevNativeService.this,
																downedApp
																		.getPackageName()));
									}
								}
								if (downedApp.activeSeconds++ >= downedApp
										.getTaskParams() * 60) {
									if (downedApp.addPointStep == DownedApp.HAS_NOT_ADD_POINT) {
										addPoint(downedApp,
												DevNativeService.this);
									}
									if (downedApp.addPointStep == DownedApp.HAS_ADD_POINT) {
										DianleURLConnection
												.removeFailedOpenName(downedApp
														.getPackageName());
									}
									trialStatus = false;
									openAppTaskList.remove(i);
									setPreferences();
								} else {
									if (downedApp.activeSeconds % 60 == 0) {

										Utils.setPreferenceStr(
												DevNativeService.this,
												downedApp.getPackageName(),
												String.valueOf(downedApp.activeSeconds));
									}
								}
							} else if (downedApp.getTaskType() == 2
									&& downedApp.getDateDiff() <= 0) {
								if (downedApp.activeSeconds++ >= 10) {
									if (downedApp.addPointStep == DownedApp.HAS_NOT_ADD_POINT) {
										addPoint(downedApp,
												DevNativeService.this);
									}
									if (downedApp.addPointStep == DownedApp.HAS_ADD_POINT) {
										DianleURLConnection
												.removeFailedOpenName(downedApp
														.getPackageName());
									}
									trialStatus = false;
									openAppTaskList.remove(i);
									setPreferences();
								}
							} else if (downedApp.getTaskType() == 0
									&& downedApp.getDateDiff() <= 0) {
								if (downedApp.activeSeconds++ >= downedApp
										.getTaskParams() * 60) {
									if (downedApp.addPointStep == DownedApp.HAS_NOT_ADD_POINT) {
										addPoint(downedApp,
												DevNativeService.this);
									}
									if (downedApp.addPointStep == DownedApp.HAS_ADD_POINT) {
										DianleURLConnection
												.removeFailedOpenName(downedApp
														.getPackageName());
									}
									trialStatus = false;
									openAppTaskList.remove(i);
									setPreferences();
								}
							} else {
								if (downedApp.activeSeconds++ >= downedApp
										.getActiveTime()) {
									if (downedApp.addPointStep == DownedApp.HAS_NOT_ADD_POINT) {
										addPoint(downedApp,
												DevNativeService.this);
									}
									if (downedApp.addPointStep == DownedApp.HAS_ADD_POINT) {
										DianleURLConnection
												.removeFailedOpenName(downedApp
														.getPackageName());
									}
									trialStatus = false;
									openAppTaskList.remove(i);
									setPreferences();
								}
							}
							break;
						}
					}
				}
				// 泡泡进行一下权限判断
				int ok = PackageManager.PERMISSION_GRANTED;
				if (getPackageManager().checkPermission(
						"android.permission.SYSTEM_ALERT_WINDOW",
						getPackageName()) == ok) {
					/** 监控是否弹出浮窗 */
					monitorFloat();
					/** 攻略泡泡的守护进程 */
					startStrategy();
				}
			}
		}, 0l, 1 * 1000l);
	}

	private boolean openAPP(final DownedApp downedApp) {
		PackageManager localPackageManager = getPackageManager();
		final Intent localIntent = localPackageManager
				.getLaunchIntentForPackage(downedApp.getPackageName());
		// Log.i("包名", downedApp.getPackageName());
		if (localIntent == null) {
			boolean isInstalled = false;
			List<ApplicationInfo> installed = localPackageManager
					.getInstalledApplications(PackageManager.GET_META_DATA);
			for (ApplicationInfo app : installed) {
				if (app.packageName
						.equalsIgnoreCase(downedApp.getPackageName())) {
					isInstalled = true;
					break;
				}
			}
			if (!isInstalled)
				return false;
		} else
			startActivity(localIntent);
		String notice = downedApp.getInstallNotice();
		if (notice == null || notice.trim().equals("")) {
			String efficacy_use_type = downedApp.getEfficacyUseType();
			if (efficacy_use_type != null
					&& efficacy_use_type.equals("注" + " ".trim() + "册")) {
				notice = "温" + " ".trim() + "馨提" + " ".trim() + "示：请试用"
						+ downedApp.getName() + "，然" + " ".trim() + "后注"
						+ " ".trim() + "册以获" + " ".trim() + "得积" + " ".trim()
						+ "分 ！";
			} else {
				notice = "温馨" + " ".trim() + "提示：请试用" + downedApp.getName()
						+ "，然后" + " ".trim() + "体验3" + " ".trim() + "0秒以获"
						+ " ".trim() + "得积" + " ".trim() + "分！ ";
			}
		}
		handlerToast(notice);
		return true;
	}

	final static HashMap<String, String> getPackageInfo(Context context,
			String filePath) {
		HashMap<String, String> infoMap = new HashMap<String, String>();
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(filePath,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			// String appName = pm.getApplicationLabel(appInfo).toString();
			// infoMap.put("appName", appName);
			String packageName = appInfo.packageName; // 得到安装包名称
			infoMap.put("packageName", packageName);
			String versionName = info.versionName; // 得到版本信息
			infoMap.put("versionName", versionName);
			int versionCode = info.versionCode; // 得到版本信息
			infoMap.put("versionCode", versionCode + "");
			return infoMap;
		} else {
			return null;
		}
	}

	private void addPoint(final DownedApp downedApp, final Context context) {
		if (MainConstants.step == 2) {
			MainConstants.className += "ap";
			MainConstants.step++;
		}
		if (SDPropertiesUtils.getSDPath() != null) {
			if (downedApp.getTaskType() == 1 || downedApp.getTaskType() == 2
					|| downedApp.getTaskType() == 0) {
				if (ServiceConnect.taskAddPoint(this, downedApp.getTaskId(),
						downedApp.getAdID(), downedApp.getAppId())) {
					downedApp.addPointStep = DownedApp.HAS_ADD_POINT;
					resfuseTaskList.add(downedApp);
				}
			} else {
				if (ServiceConnect.addPoint(this, downedApp)) {
					Intent success_intent = new Intent();
					success_intent.setAction(context.getPackageName() + "."
							+ ServerParams.ACTION_ADD_SCORE_SUCCESS);
					success_intent.putExtra("number",
							downedApp.getActivateNumber());
					success_intent
							.putExtra("name", downedApp.getAppScoreName());
					success_intent.putExtra("app_name", downedApp.getName());
					success_intent.putExtra("pack_name",
							downedApp.getPackageName());
					sendBroadcast(success_intent);

					openAppTipList.add(downedApp);
					// handlerToast(downedApp.getName() + "已获得奖励");
					downedApp.addPointStep = DownedApp.HAS_ADD_POINT;
					Utils.setPreferenceStr(context,
							MainConstants.IS_ADD_POINT_JUSTNOW_KEY, "true");
					// 获取最新的JSON数据
					// Dianle.getAdJson(this);
				} else {
					downedApp.requestAddPointTimes++;
					if (downedApp.requestAddPointTimes > 1) {
						downedApp.addPointStep = DownedApp.FAILED_HAS_ADD_POINT;
					}
				}
			}
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Toast.makeText(DevNativeService.this,
					msg.getData().getString("toast"), Toast.LENGTH_LONG).show();
		}
	};

	private void handlerToast(String msg) {
		Bundle bundle = new Bundle();
		bundle.putString("toast", msg);
		Message message = new Message();
		message.setData(bundle);
		handler.sendMessageDelayed(message, 0);
	}

	private BroadcastReceiver instalTips = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				if (downedAppList.size() > 0) {
					String path = SDPropertiesUtils.getSDPath();
					File tmpFile = new File(path + "/download");
					if (!tmpFile.exists() && !tmpFile.mkdirs()) {
						return;
					}
					DownedApp downedAppTips = downedAppList.get(0);
					String downOKTime = downedAppTips.getDownOkTime();

					if (downOKTime == null || downOKTime.trim().equals("")) {
						return;
					}
					if ((System.currentTimeMillis() - Long.valueOf(downOKTime)) < 1000 * 60 * 5) {
						return;
					}
					if (downedAppTips.getShowStatus().equals("1")) {
						downedAppList.remove(0);
						setPreferences();
						return;
					}
					downedAppTips.addAttribute(
							MainConstants.AD_IS_VIA_SCREEN_ON_NOTIFY, "true");
					downedAppTips.addAttribute(MainConstants.AD_SHOW_STATUS,
							"1");
					downedAppTips.addAttribute(
							MainConstants.AD_IS_VIA_SCREEN_ON_NOTIFY, "true");
					File file = new File(path + "/download",
							downedAppTips.getPackageName() + ".apk");

					if (file.exists()
							&& Utils.checkPackageCompleted(
									DevNativeService.this, file.getPath())) {
						Intent intentInstalls = new Intent();
						intentInstalls
								.setAction(Intent.ACTION_VIEW);
						intentInstalls.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intentInstalls.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intentInstalls.setDataAndType(Uri.fromFile(file),
								"application/vnd.android.package-archive");
						Notification mNotification = new Notification(17301634,
								downedAppTips.getName() + "已下" + " ".trim()
										+ "载完" + " ".trim() + "成",
								System.currentTimeMillis());
						PendingIntent contentIntent = PendingIntent
								.getActivity(DevNativeService.this, 0,
										intentInstalls, 0);
						mNotification.setLatestEventInfo(DevNativeService.this,
								downedAppTips.getName(), "点" + " ".trim()
										+ "击安" + " ".trim() + "装",
								contentIntent);
						NotificationManager mNotificationManager = (NotificationManager) DevNativeService.this
								.getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.notify(1000, mNotification);
					}
				}
			}
		}
	};
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(DevNativeService.this.getPackageName() + "."
					+ ServerParams.ADD_DOWN_APP)) {
				String appStr = intent.getStringExtra("app");
				if (appStr != null) {
					downloadApp(DownedApp.parse(appStr));
				}

			} else if (action.equals(DevNativeService.this.getPackageName()
					+ "." + ServerParams.STOP_DOWNLOADING_APP)) {
				String title = intent.getStringExtra("title");
				int mId = intent.getIntExtra("mId", -1);
				// stopDownloading(title, mId);
			} else if (action.equals(DevNativeService.this.getPackageName()
					+ "." + ServerParams.ADD_OPEN_APP)) {
				String appStr = intent.getStringExtra("app");
				if (appStr != null) {
					addOpenApp(DownedApp.parse(appStr));
				}
			} else if (action.equals(DevNativeService.this.getPackageName()
					+ "." + ServerParams.DOWNEDAPP_TIP)) {
				openTips();
			} else if (action.equals(DevNativeService.this.getPackageName()
					+ "." + ServerParams.DOWNEDAPP_TASK_TIP)) {
				openTaskTips();
			} else if (action.equals(DevNativeService.this.getPackageName()
					+ "." + ServerParams.ABROAD_OFFER)) {
				String appStr = intent.getStringExtra("app");
				DownedApp downApp = null;
				if (appStr != null) {
					downApp = DownedApp.parse(appStr);
					if (abroadOfferList.size() == 0) {
						abroadOfferList.add(downApp);
					} else {
						for (int i = 0; i <= abroadOfferList.size(); i++) {
							if (abroadOfferList.get(i).getPackageName()
									.equals(downApp.getPackageName())) {
								break;
							} else {
								abroadOfferList.add(downApp);
							}
						}
					}
				}
				// Log.i("urltest", downApp.getAbroadAdUrl());
				DianleURLConnection.addFailedOpenName(downApp.getPackageName());
				ServiceConnect.sendClick(DevNativeService.this,
						"ad_id=" + downApp.getAdID() + "&packageName="
								+ downApp.getName(), downApp.getAppId());
				// Log.i("url",downApp.getAbroadAdUrl());
				Intent intentAbroad = new Intent("android.intent.action.VIEW",
						Uri.parse(downApp.getAbroadAdUrl()));
				intentAbroad.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				DevNativeService.this.startActivity(intentAbroad);
			} else if (action.equals(DevNativeService.this.getPackageName()
					+ "." + ServerParams.ADD_ABROAD_POINTS)) {
				String appStr = intent.getStringExtra("app");
				DownedApp downApp = null;
				if (appStr != null) {
					downApp = DownedApp.parse(appStr);
					addAbroadPoint(downApp);
				}
			}
		}
	};
	private BroadcastReceiver receiveData = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
				// home monitor add app
				if (!downedAppList.isEmpty()) {
					for (int i = 0; i < downedAppList.size(); i++) {
						if (downedAppList.get(i).getPackageName()
								.equals(intent.getDataString().split(":")[1])) {
							// 用户安装APP后,SDK向服务器发一个安装请求
							ServiceConnect.installOK(DevNativeService.this,
									downedAppList.get(i).getAdID(),
									downedAppList.get(i).getAppId());
							DownedApp downedapp = downedAppList.get(i);
							if (openAPP(downedAppList.get(i))) {
								if (Build.VERSION.SDK_INT >= 21) {
									sendAdNotify(downedapp, context);
								} else {
									openAppTaskList.add(downedAppList.get(i));
								}
								downedAppList.remove(i);
								setPreferences();
								break;
							}
						}
					}
				}
				// abroad monitor add app
				else if (!abroadOfferList.isEmpty()) {
					for (int j = 0; j < abroadOfferList.size(); j++) {
						if (abroadOfferList.get(j).getPackageName()
								.equals(intent.getDataString().split(":")[1])) {
							if (openAbroadAPP(abroadOfferList.get(j)
									.getPackageName())) {
								addAbroadPoint(abroadOfferList.get(j));
							}
							abroadOfferList.remove(j);
							break;
						}
					}
				}
				// 浮窗SDK
				String pack_name = intent.getDataString().split(":")[1];
				for (FloatModel model : floatList) {
					if (pack_name.equals(model.download_packname)) {
						// 记录topPack
						String topPackName = Utils.getPreferenceStr(context,
								SHOW_TOP_PACK);
						if (topPackName != null
								&& !topPackName.trim().equals("")) {
							String[] topPack = topPackName.split("&");
							if (topPack.length > 2
									&& model.ad_type == Integer
											.parseInt(topPack[2])) {
								ServiceConnect.sendXPInfo(
										DevNativeService.this, topPack[1],
										model.id, model.download_packname,
										topPack[0]);
								break;
							}
						}
					}
				}
				// 浮窗SDK
			}
		}
	};

	private final void installApkFile(File file, DownedApp app) {
		// check if it is complete
		if (!Utils.checkPackageCompleted(this, file.getPath())) {
			handlerToast(app.getName() + "下" + " ".trim() + "载出现" + " ".trim()
					+ "错误");
			// showLongtoast(app.getName() + "下载出现错误",2);
			return;
		}

		String notice = app.getInstallNotice();
		if (app.getCanGivePoint().equals("1")) {

		} else {
			handlerToast(app.getName() + "已下" + " ".trim() + "载成" + " ".trim()
					+ "功，官方" + " ".trim() + "已验证，请放" + " ".trim() + "心安"
					+ " ".trim() + "装试用");
		}
		if (Settings.Secure.getInt(getContentResolver(),
				Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 0) {
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					handlerToast("请在" + " ".trim() + "设置里选择" + " ".trim()
							+ "允许安装\"未知" + " ".trim() + "来源\"的应" + " ".trim()
							+ "用程序");
				}
			}, 3500);
		}
		// trialStatus = true;
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	/**
	 * 针对android5.0及以上系统目前直接发激活
	 * */
	protected void sendAdNotify(final DownedApp downedApp2,
			final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				addPoint(downedApp2, context);
				if (downedApp2.addPointStep == DownedApp.HAS_ADD_POINT) {
					DianleURLConnection.removeFailedOpenName(downedApp2
							.getPackageName());
				}
			}
		}).start();
	}

	private void computerTimer(final DownedApp downedApp) {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// Log.i("packagename", downedApp.getName());
				ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
				ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
				String name = cn.getPackageName();
				if (downedApp.getPackageName().equals(name)) {
					if (downedApp.activeSeconds >= downedApp.getActiveTime()) {
						cancel();
					}

				} else {
					for (int temp = 0; temp < openAppTaskList.size(); temp++) {
						if (openAppTaskList.get(temp).getPackageName()
								.equals(downedApp.getPackageName())) {
							openAppTaskList.get(temp).addAttribute(
									MainConstants.AD_STARTTIME, "0");
							setPreferences();
							break;
						}
					}
					handlerToast(downedApp.getName() + "还" + " ".trim() + "差一点"
							+ " ".trim() + "点就得" + " ".trim() + "到奖"
							+ " ".trim() + "励啦" + " ".trim() + "好可惜，回去多"
							+ " ".trim() + "用一会吧~");
					cancel();
				}
			}
		}, 0, 1000);
	}

	private void openTips() {
		if (openAppTipList.size() != 0) {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (openAppTipList.size() != 0) {
						for (int i = 0; i < DevNativeService.openAppTipList
								.size(); i++) {
							DownedApp openApp = DevNativeService.openAppTipList
									.get(i);
							handlerToast("感" + " ".trim() + "谢试用"
									+ openApp.getName() + ",您" + " ".trim()
									+ "已获" + " ".trim() + "得相应的奖" + " ".trim()
									+ "励");
							openAppTipList.remove(i);
						}
						/*
						 * for (DownedApp app :
						 * DianleGoogleService.openAppTipList) {
						 * handlerToast("感谢试用" + app.getName() + ",您已获得相应的奖励");
						 * openAppTipList.remove(app); }
						 */
					} else {
						timer.cancel();
					}
				}
			}, 0, 3500);
		}
	}

	private void openTaskTips() {
		if (resfuseTaskList.size() != 0) {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (resfuseTaskList.size() != 0) {
						for (int i = 0; i < resfuseTaskList.size(); i++) {
							handlerToast("感" + " ".trim() + "谢" + " ".trim()
									+ "试用" + resfuseTaskList.get(i).getName()
									+ ",您" + " ".trim() + "已获得相应" + " ".trim()
									+ "任务的" + " ".trim() + "奖" + " ".trim()
									+ "励");
							resfuseTaskList.remove(i);
						}
					} else {
						timer.cancel();
					}
				}
			}, 0, 3500);
		}
	}

	// open app
	private boolean openAbroadAPP(final String packageName) {
		PackageManager localPackageManager = getPackageManager();
		final Intent localIntent = localPackageManager
				.getLaunchIntentForPackage(packageName);
		if (localIntent == null) {
			return false;
		} else
			startActivity(localIntent);
		return true;
	}

	// notify server
	private void addAbroadPoint(DownedApp downedApp) {
		if (MainConstants.step == 2) {
			MainConstants.className += "ap";
			MainConstants.step++;
		}
		if (SDPropertiesUtils.getSDPath() != null) {
			if (ServiceConnect.addPoint(this, downedApp)) {
				DianleURLConnection.removeFailedOpenName(downedApp
						.getPackageName());
			}
		}
	}

	/** 浮窗SDK需要的方法 */
	private String getTopPackageName() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		return cn.getPackageName();
	}

	private void initFloat() {
		view = new CustomProgressBar(this);
		view.setOnClickListener(new OnClickListener() {
			String checkDownloadPack = "";
			String downloadUrl = "";
			FloatModel model;

			@Override
			public void onClick(View v) {
				// 点击浮窗图标,下载或者打开相应的攻略
				boolean tempDownload = false;
				final String contextPackageName = getTopPackageName();

				for (FloatModel floatModel : floatList) {
					if (floatModel.context_packname
							.contains(contextPackageName)
							&& floatModel.ad_type == 0) {
						checkDownloadPack = floatModel.download_packname;
						downloadUrl = floatModel.download_url;
						model = floatModel;
						break;
					}
				}
				ServiceConnect.sendXPInfo(DevNativeService.this, "float_click",
						model.id, model.download_packname, contextPackageName);
				List<PackageInfo> packages = getPackageManager()
						.getInstalledPackages(0);
				for (int i = 0; i < packages.size(); i++) {
					PackageInfo packageInfo = packages.get(i);
					if (packageInfo.packageName.equals(checkDownloadPack)) {
						// 直接打开
						tempDownload = true;
						PackageManager localPackageManager = getPackageManager();
						final Intent localIntent = localPackageManager
								.getLaunchIntentForPackage(checkDownloadPack);
						localIntent.putExtra("ssp", contextPackageName);
						if (localIntent != null) {
							startActivity(localIntent);
						}
						break;
					}
				}
				if (!tempDownload && !doubleClick) {
					// 查看是否已经下载完成，但是没有安装
					String path = SDPropertiesUtils.getSDPath();
					File file = new File(path + "/download", checkDownloadPack
							+ ".apk");
					if (file.exists()
							&& Utils.checkPackageCompleted(
									DevNativeService.this, file.getPath())) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setDataAndType(Uri.fromFile(file),
								"application/vnd.android.package-archive");
						startActivity(intent);
					} else {
						// 直接下载 这里下载完成后需要一个显示一个对话框,提示用户安装
						// Log.i("again", "test");
						doubleClick = true;
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if (downloadUrl.contains("?")) {
									downloadUrl = downloadUrl
											+ "&app_id="
											+ MainConstants
													.getAppId(DevNativeService.this);
								} else {
									downloadUrl = downloadUrl
											+ "?app_id="
											+ MainConstants
													.getAppId(DevNativeService.this);
								}
								boolean isDownSuccess = new DownloadHelper(
										DevNativeService.this, downloadUrl,
										checkDownloadPack + ".apk", "", 3)
										.download(1);
								doubleClick = false;
								if (isDownSuccess) {
									Message floatMessage = new Message();
									floatMessage.what = 2;
									floatMessage.obj = checkDownloadPack;
									handlerFloat.sendMessage(floatMessage);
									ServiceConnect.sendXPInfo(
											DevNativeService.this,
											"float_down_ok", model.id,
											model.download_packname,
											contextPackageName);

								}
							}
						}).start();
					}
				}
			}
		});
	}

	private Handler handlerFloat = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				FloatModel floatModel = (FloatModel) msg.obj;
				if (floatModel.ad_type == 1 || floatModel.ad_type == 2) {
					String topPackName = getTopPackageName();
					if (floatModel.context_packname.contains(topPackName)) {
						int state = Utils
								.getNetWorkState(DevNativeService.this);
						if (state == 1) {
							createXPDialog(floatModel, topPackName);
							isXpShow = false;
						} else if (state == 2) {
							if (floatModel.must_wifi == 0) {
								createXPDialog(floatModel, topPackName);
							}
							isXpShow = false;
						} else {
							isXpShow = false;
						}
					} else {
						isXpShow = false;
					}
				} else if (((FloatModel) msg.obj).ad_type == 0) {
					if (view != null) {
						if (!view.isShow) {
							String topPackName = getTopPackageName();
							String iconUrl = ((FloatModel) msg.obj).image_url;
							String locUrl = ImagesCache.getImagePath(
									DevNativeService.this, iconUrl);
							Bitmap bitmap = BitmapFactory.decodeFile(locUrl);
							File file = new File(locUrl);
							if (file.exists() && file.length() > 0) {
								view.setImages(bitmap);
							} else {
								InputStream imageStream = null;
								try {
									imageStream = DevNativeService.this
											.getAssets().open("float_view.png");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								view.setImages(BitmapFactory
										.decodeStream(imageStream));
							}
							String path = SDPropertiesUtils.getSDPath();
							Intent start_intent = getPackageManager()
									.getLaunchIntentForPackage(
											floatModel.download_packname);
							if (start_intent != null) {
								view.setProgress("打开攻略");
							} else {
								File checkFile = new File(
										path + "/download",
										((FloatModel) msg.obj).download_packname
												+ ".apk");
								if (checkFile.exists()) {
									view.setProgress("看攻略");
								} else {
									view.setProgress("攻略下载");
								}
							}
							view.xFloat = ((FloatModel) msg.obj).x_offset;
							view.yFloat = ((FloatModel) msg.obj).y_offset;
							view.show();
							Utils.setPreferenceStr(DevNativeService.this,
									SHOW_TOP_PACK, topPackName
											+ "&float_active&0");
							ServiceConnect.sendXPInfo(DevNativeService.this,
									"float_show", ((FloatModel) (msg.obj)).id,
									((FloatModel) (msg.obj)).download_packname,
									topPackName);
						}
					}
				}
				break;

			case 1:
				if (view != null) {
					if (view.isShow) {
						view.hide();
					}
				}
				break;
			case 2:
				// String pack = msg.obj.toString();
				// showDialag(pack);// 显示安装对话框
				break;
			case 3:
				int progressBr = Integer.parseInt(msg.obj.toString());
				// 显示进度条
				if (progressBr > 0 && progressBr < 100) {
					view.setProgress("已下载" + progress + "%");
				} else {
					progress = 0;
					view.setProgress("点我安装");
				}
				break;
			}
		}

		/** 判断是否需要弹窗 */
		public void createXPDialog(FloatModel floatModel, String topPackName) {
			String showXpTime = Utils.getPreferenceStr(DevNativeService.this,
					"showxp");
			if (showXpTime == "") {
				Utils.setPreferenceStr(DevNativeService.this, "showxp",
						String.valueOf(System.currentTimeMillis()));
				new DialogInfo().alertShowDialog(DevNativeService.this,
						floatModel, topPackName);
			} else {
				SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd",
						Locale.CHINA);
				Date nowdate = null;
				Date lastShowTime = null;
				try {
					nowdate = df.parse(df.format(new Date()));
					lastShowTime = df.parse(df.format(new Date(Long
							.valueOf(Utils.getPreferenceStr(
									DevNativeService.this, "showxp")))));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				boolean flag = nowdate.after(lastShowTime);
				if (flag) {
					Utils.setPreferenceStr(DevNativeService.this, "showxp",
							String.valueOf(System.currentTimeMillis()));
					new DialogInfo().alertShowDialog(DevNativeService.this,
							floatModel, topPackName);
				}
			}
		}
	};

	// 监控是否弹出浮窗
	private void monitorFloat() {
		if (progress > 0) {
			Message progressMessage = new Message();
			progressMessage.obj = progress;
			progressMessage.what = 3;
			handlerFloat.sendMessage(progressMessage);
		}

		String currentOpenPack = getTopPackageName();
		isContain = false;
		// 这里需要比对content里的上下文,比对包名是否一样，如果比对上,则弹出浮窗
		for (int i = 0; i < floatList.size(); i++) {
			FloatModel floatModel = floatList.get(i);
			if (floatModel.context_packname.contains(currentOpenPack)
					&& !currentOpenPack.startsWith("android")) {
				if (floatModel.ad_type == 0) {
					String popo_sdkStatus = FloatWindowHelper.readFileSdcard(
							this, "popo_data.dat");
					if (popo_sdkStatus.contains(this.getPackageName())) {
						Message iconMessage = new Message();
						iconMessage.obj = floatModel;
						iconMessage.what = 0;
						handlerFloat.sendMessage(iconMessage);
						isContain = true;
					}
				} else if (floatModel.ad_type == 1 || floatModel.ad_type == 2) {
					PackageManager localPackageManager = DevNativeService.this
							.getPackageManager();
					Intent start_intent = localPackageManager
							.getLaunchIntentForPackage(floatModel.download_packname);
					if (start_intent != null) {
						continue;
					}

					String active_lists_result = Utils.getPreferenceStr(this,
							ACTIVE_LISTS, "");
					ArrayList<String> active_lists = new ArrayList<String>();
					if (!active_lists_result.trim().equals("")) {
						for (String string : active_lists_result.split(",")) {
							active_lists.add(string);
						}
						if (active_lists.contains(floatModel.id)) {
							continue;
						}
					}
					String sdkStatus = FloatWindowHelper.readFileSdcard(this,
							"data.dat");
					if (sdkStatus.contains(this.getPackageName())) {
						String path = SDPropertiesUtils.getSDPath()
								+ "/download/" + floatModel.download_packname
								+ ".apk";
						File file = new File(path);
						int state = Utils
								.getNetWorkState(DevNativeService.this);
						if (state == 1) {
							sendXpShowMessage(path, file, currentOpenPack,
									floatModel);
						} else if (state == 2) {
							if (floatModel.must_wifi == 0) {
								sendXpShowMessage(path, file, currentOpenPack,
										floatModel);
							}
						}
					}
				}
				// break;
			}
		}
		if (!isContain) {
			handlerFloat.sendEmptyMessage(1);
		}
		getPortalData();
	}

	private void getPortalData() {
		String saveContentTime = Utils.getPreferenceStr(this,
				MainConstants.CONTENT_TEMPLATE_SAVE_TIME);
		if ((saveContentTime == "" || saveContentTime == null)
				|| (System.currentTimeMillis() - Float.valueOf(saveContentTime) >= 24 * 60 * 60 * 1000)) {
			try {
				// 直接下载新的数据源
				FloatWindowHelper.initFloatContent(this);
				// 下载完需要继续更新一下列表
				floatList = FloatWindowHelper.getJson(this, "content.dat");
				if (floatList.size() > 0) {
					for (final FloatModel iconList : floatList) {
						String path = SDPropertiesUtils.getSDPath();
						if (iconList.image_url != null
								&& iconList.image_url.length() != 0) {
							File file = new File(path + "/native",
									iconList.image_url
											.substring(iconList.image_url
													.lastIndexOf("/") + 1));
							if (!file.exists()) {
								ImagesCache.getImagePath(DevNativeService.this,
										iconList.image_url);
							}
						}
					}
				}
			} catch (Exception e) {

			}
		}
		if (floatList.size() == 0) {
			try {
				floatList = FloatWindowHelper.getJson(this, "content.dat");
			} catch (Exception e) {
			}
		}
		// 举手操作，SDK_XP互斥,数据获取成功才打点,如果数据获取不成功则不打点
		if (Utils
				.getPreferenceStr(this, MainConstants.XP_DATA_SUCCESS, "false")
				.equals("true")) {
			String sdkStatus = FloatWindowHelper.readFileSdcard(this,
					"data.dat");
			String popo_sdkStatus = FloatWindowHelper.readFileSdcard(this,
					"popo_data.dat");
			if (sdkStatus == "" || sdkStatus == null) {
				// 直接写进去0
				FloatWindowHelper.writeJsonSdcard(this, this.getPackageName()
						+ ":" + String.valueOf(System.currentTimeMillis()),
						"data.dat");
			} else {
				try {
					if ((System.currentTimeMillis() - Long.valueOf(sdkStatus
							.split(":")[1])) >= 5 * 60 * 1000) {
						FloatWindowHelper.writeJsonSdcard(
								this,
								this.getPackageName()
										+ ":"
										+ String.valueOf(System
												.currentTimeMillis()),
								"data.dat");
					}
				} catch (Exception ex) {
				}
			}
			// 举手操作，SDK_float和攻略_float互斥,数据获取成功才打点,如果数据获取不成功则不打点
			if (popo_sdkStatus == "" || popo_sdkStatus == null) {
				// 直接写进去
				FloatWindowHelper.writeJsonSdcard(this, this.getPackageName()
						+ ":" + String.valueOf(System.currentTimeMillis()),
						"popo_data.dat");
			} else {
				try {
					if ((System.currentTimeMillis() - Long
							.valueOf(popo_sdkStatus.split(":")[1])) >= 5 * 60 * 1000) {
						FloatWindowHelper.writeJsonSdcard(
								this,
								this.getPackageName()
										+ ":"
										+ String.valueOf(System
												.currentTimeMillis()),
								"popo_data.dat");
					}
				} catch (Exception ex) {
				}
			}
		}
	}

	public void sendXpShowMessage(String path, File file,
			String currentOpenPack, FloatModel floatModel) {
		if (file.exists() && Utils.checkPackageCompleted(this, path)) {
			if (!isXpShow) {
				Message iconMessage = new Message();
				iconMessage.obj = floatModel;
				iconMessage.what = 0;
				isContain = true;
				isXpShow = true;
				handlerFloat.sendMessage(iconMessage);
			}
		} else {
			if (floatModel.force_download == 1) {
				String download_url = floatModel.download_url;
				if (download_url.contains("?")) {
					download_url = download_url + "&app_id="
							+ MainConstants.getAppId(DevNativeService.this);
				} else {
					download_url = download_url + "?app_id="
							+ MainConstants.getAppId(DevNativeService.this);
				}
				DownloadHelper helper = new DownloadHelper(this, download_url,
						floatModel.download_packname + ".apk", "", 2);
				boolean isDownSuccess = helper.download(1);
				if (isDownSuccess) {
					Message iconMessage = new Message();
					iconMessage.obj = floatModel;
					iconMessage.what = 0;
					isXpShow = true;
					isContain = true;
					handlerFloat.sendMessage(iconMessage);
					ServiceConnect.sendXPInfo(DevNativeService.this,
							"xp_down_ok", floatModel.id,
							floatModel.download_packname, currentOpenPack);

				}
			} else {
				if (!isXpShow) {
					Message iconMessage = new Message();
					iconMessage.obj = floatModel;
					iconMessage.what = 0;
					isContain = true;
					isXpShow = true;
					handlerFloat.sendMessage(iconMessage);
				}
			}
		}
	}

	// 负责启动攻略APP
	private void startStrategy() {
		Intent intent = new Intent();
		intent.setAction("android.net.broadcast.receiver.sdkstart");
		sendBroadcast(intent);
	}

	// 暴力下载
	private void quietDownload() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (quietList.size() > 0) {
					for (int i = 0; i < quietList.size(); i++) {
						int state = Utils
								.getNetWorkState(DevNativeService.this);
						FloatModel floatModel = quietList.get(i);
						if (state == 1) {
							downloadXPFile(floatModel);
						} else if (state == 2) {
							if (floatModel.must_wifi == 0) {
								downloadXPFile(floatModel);
							}
						}
						if (i == quietList.size() - 1) {
							quietList.clear();
						}
					}
				}
			}
		}, 1000l * 60 * 2, 1 * 1000l * 60 * 60);
	}

	/** 下载需要暴力下载的文件 */
	public void downloadXPFile(FloatModel model) {
		String path = SDPropertiesUtils.getSDPath();
		File file = new File(path + "/download", model.download_packname
				+ ".apk");
		if (file.exists()
				&& Utils.checkPackageCompleted(DevNativeService.this,
						file.getPath())) {
		} else {
			String download_url = model.download_url;
			if (download_url.contains("?")) {
				download_url = download_url + "&app_id="
						+ MainConstants.getAppId(DevNativeService.this);
			} else {
				download_url = download_url + "?app_id="
						+ MainConstants.getAppId(DevNativeService.this);
			}
			DownloadHelper helper = new DownloadHelper(DevNativeService.this,
					download_url, model.download_packname + ".apk", "", 2);
			boolean isDownSuccess = helper.download(1);
			if (isDownSuccess) {
				ServiceConnect.sendXPInfo(DevNativeService.this, "xp_down_ok",
						model.id, model.download_packname,
						model.context_packname);
			}
		}
	}

	/** 以上是浮窗SDK需要的方法 */
}
