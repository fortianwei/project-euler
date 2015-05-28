package com.dlnetwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DevNativeActivity extends Activity {
	private DianleConnect mConnect = null;
	private final static int SHOW_TOAST = 7;
	private final static int UPDATE_PROGRESS = 0;
	private final static int HIDE_PROGRESS = 1;
	private String infoList = " ".trim() + "读" + " ".trim() + "取奖" + " ".trim()
			+ "励列" + " ".trim() + "表";
	private String infoListEn = "loading...";
	private String infoContent = " ".trim() + "读取" + " ".trim() + "奖"
			+ " ".trim() + "励内" + " ".trim() + "容";
	private WebView mWebView = null;
	private ProgressBar progressBar;
	/** 显示取得的数据 */
	// static Activity activity;
	// private final String SCHEME_WTAI_MC = "wtai'://wp/mc;";
	boolean isZeroMoney;
	String currentMoneyName;
	boolean isShowMoney;
	final int LOADING_PERCENT = 40;
	private static boolean notCheckPermission = true;
	private static boolean isServiceCreate;
	private static boolean isOfferByNotification;
	private final String mWebViewReturnMark = ServerParams.url_return;// Utils.decode("nans.ra1uJ&tb~e4}r5h:q~yz+oRujRInAua|yi}2d");
																		// //"d"+" ".trim()+"i"
																		// +
																		// "      ".trim()
																		// +
																		// "an"+
																		// "jo"
																		// +
																		// "y:ret"+" ".trim()+"urn";
	private int status = 1;
	private String last_url = "";
	private String current_url = "";
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_TOAST:
				if (msg.getData() != null) {
					String toast = msg.getData().getString("toast");
					if (toast != null && !toast.trim().equals("")) {
						Toast.makeText(DevNativeActivity.this, toast,
								Toast.LENGTH_LONG).show();
					}
				}
				break;
			case UPDATE_PROGRESS:
				if (msg.obj != null) {
					Integer i = (Integer) msg.obj;
					textView.setText(i + "%");
				}
				break;
			case HIDE_PROGRESS:
				cover.setVisibility(View.GONE);
				textView.setText("0%");
				break;

			default:
				break;
			}
		}
	};

	final void showStopDownloadDialog(final Context context,
			final String title, final String text, final int notificationId) {
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(
						text + "，重新" + " ".trim() + "下" + " ".trim() + "载支持"
								+ " ".trim() + "断" + " ".trim() + "点续"
								+ " ".trim() + "传" + " ".trim() + "节"
								+ " ".trim() + "省流" + " ".trim() + "量")
				.setPositiveButton(
						MainConstants.DIALOG_STRING_STOP_DOWNLOAD_POSITIVE,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								Intent intent = new Intent(
										DevNativeActivity.this.getPackageName()
												+ "."
												+ ServerParams.STOP_DOWNLOADING_APP);
								intent.putExtra("title", title);
								intent.putExtra("mId", notificationId);
								sendBroadcast(intent);
								finish();
							}
						})
				.setNegativeButton(
						MainConstants.DIALOG_STRING_STOP_DOWNLOAD_NEGATIVE,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialogInterface) {
						finish();
					}
				}).show();
	}

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// activity = DianleOfferActivity.this;
		if (notCheckPermission) {
			notCheckPermission = false;
			int ok = PackageManager.PERMISSION_GRANTED;
			if (getPackageManager().checkPermission(
					"android.permission.INTERNET", getPackageName()) == ok
					&& getPackageManager().checkPermission(
							"android.permission.READ_PHONE_STATE",
							getPackageName()) == ok
					&& getPackageManager().checkPermission(
							"android.permission.ACCESS_NETWORK_STATE",
							getPackageName()) == ok
					&& getPackageManager().checkPermission(
							"android.permission.GET_TASKS", getPackageName()) == ok
					&& getPackageManager().checkPermission(
							"android.permission.WRITE_EXTERNAL_STORAGE",
							getPackageName()) == ok) {
			} else {
				handlerToast(MainConstants.NOTIFICATION_STRING + "\nAndr"
						+ " ".trim() + "oidMan" + " ".trim() + "ifest.xml里"
						+ " ".trim() + "面" + " ".trim() + "权" + " ".trim()
						+ "限设" + " ".trim() + "置不全\n\n请" + " ".trim() + "您查看最"
						+ " ".trim() + "新的文档!");
				DevNativeActivity.this.finish();
				return;
			}
		}
		mConnect = DianleConnect.getDianleConnect();
		createActivity();
		// Utils.clearCache(mWebView);
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.getBooleanExtra("userStop", false)) {
				showStopDownloadDialog(this, intent.getStringExtra("title"),
						intent.getStringExtra("text"),
						intent.getIntExtra("mId", -1));
				return;
			}
		}
		String s;
		Bundle b;
		isOfferByNotification = false;
		if (getIntent() != null
				&& (b = getIntent().getExtras()) != null
				&& (s = b
						.getString(MainConstants.ON_DOWNLOAD_FAILED_RELOAD_PAGE_KEY)) != null) {
			mWebView.loadUrl(s);
			isOfferByNotification = true;
		} else {
			mConnect.showOffers(mWebView, this);
		}
		Utils.setPreferenceStr(this, MainConstants.IS_ADD_POINT_JUSTNOW_KEY,
				"false");
		DevInit.getTotalMoney(this, new GetTotalMoneyListener() {

			@Override
			public void getTotalMoneySuccessed(String name, long amount) {
				if (amount == 0) {
					isZeroMoney = true;
				}
				currentMoneyName = name;
			}

			@Override
			public void getTotalMoneyFailed(String error) {

			}
		});
	}

	@Override
	protected final void onResume() {
		super.onResume();
		// WebView.enablePlatformNotifications();
		if (status == 1) {
			showAddPointSuccess();
			mWebView.reload();
		}
	}

	/**
	 * 创建webview，load url，给webview添加监听事件，
	 * 
	 */
	final RelativeLayout createActivity() {
		mainPanel = new RelativeLayout(this);

		mainPanel.setBackgroundColor(Color.WHITE);
		RelativeLayout.LayoutParams main_params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		mainPanel.setLayoutParams(main_params);

		mWebView = new WebView(this);
		RelativeLayout.LayoutParams wv_params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		mWebView.setLayoutParams(wv_params);
		mainPanel.addView(mWebView);

		cover = new RelativeLayout(this);
		cover.setBackgroundColor(0x99ffffff);
		mainPanel.addView(cover, wv_params);

		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams ll_params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		ll_params.addRule(RelativeLayout.CENTER_IN_PARENT);
		linearLayout.setLayoutParams(ll_params);
		cover.addView(linearLayout);

		RelativeLayout percentLayout = new RelativeLayout(this);
		progressBar = new ProgressBar(this);
		progressBar.bringToFront();
		progressBar.setMax(100);

		// progressBar=new ProgressBar(this,null,
		// android.R.attr.progressBarStyleLarge);

		// RelativeLayout.LayoutParams pb_params=new
		// RelativeLayout.LayoutParams(80,80);
		percentLayout.addView(progressBar);

		RelativeLayout.LayoutParams pb_params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		percentLayout.setLayoutParams(pb_params);

		textView = new TextView(this);
		textView.setText("0%");
		// textView.setTextSize(14);
		textView.setTextColor(0xff333333);

		pb_params.addRule(RelativeLayout.CENTER_IN_PARENT);
		percentLayout.addView(textView, pb_params);

		linearLayout.addView(percentLayout);

		infoView = new TextView(this);

		infoView.setTextColor(0xff333333);
		// Log.i("countercode", Locale.getDefault().getCountry());
		if (getCurrentLanguage()) {
			infoView.setText(infoListEn);
		} else {
			infoView.setText(infoList);

		}
		RelativeLayout.LayoutParams pt_params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		infoView.setGravity(Gravity.CENTER);
		infoView.setLayoutParams(pt_params);
		linearLayout.addView(infoView);

		setContentView(mainPanel);

		final WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setBlockNetworkImage(true);
		mWebView.clearCache(true);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		// webSettings.setBuiltInZoomControls(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.setFocusable(true);
		mWebView.getSettings().setDefaultTextEncodingName("gb2312");
		Intent service = new Intent(DevNativeActivity.this,
				DevInit.serviceClass);
		startService(service);
		mWebView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK) { // 表示按返回键时的操作
						if (current_url.contains("ad" + " ".trim() + "li"
								+ " ".trim() + "st.p" + " ".trim() + "hp")
								&& last_url.contains("ca" + " ".trim() + "rdvi"
										+ " ".trim() + "ew.ph" + " ".trim()
										+ "p")) {
							mWebView.reload();
							return false;
						}
						if (mWebView.canGoBack()) {
							mWebView.goBack(); // 后退
							if (status == 2) {
								status = 1;
								showAddPointSuccess();
							}
							return true; // 已处理
						} else {
							return isShowDialog();
						}
					}
				}
				return false;
			}
		});
		mWebView.setWebViewClient(new WebViewClient() {
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// finish the activity
				// Log.i("webviewurl", url);
				if (url.equals(mWebViewReturnMark)) {
					if (isShowDialog()) {

					} else {
						DevNativeActivity.this.finish();
					}
					return true;
				}
				if (url.contains("a" + " ".trim() + "dl" + " ".trim()
						+ "ist.ph" + " ".trim() + "p")) {
					// return false; 广播执行
					status = 1;
					showAddPointSuccess();
				}
				String path = SDPropertiesUtils.getSDPath();
				if (path == null) {
					if (getCurrentLanguage()) {
						handlerToast("N" + " ".trim() + "O SD" + " ".trim()
								+ "Card!");
					} else {
						handlerToast("无S" + " ".trim() + "D卡!");
					}

					return true;
				}
				// abroad offer enterance
				if (url.contains("o" + " ".trim() + "ver" + " ".trim()
						+ "sea=1")) {
					final DownedApp app = getDownedAppFromUrl(url);
					PackageManager localPackageManager = DevNativeActivity.this
							.getPackageManager();
					final Intent abroadIntent = localPackageManager
							.getLaunchIntentForPackage(app.getPackageName());
					if (abroadIntent != null) {
						startActivity(abroadIntent);
						if (app.getCanGivePoint().equals("0")) {
							Intent intent = new Intent(DevNativeActivity.this
									.getPackageName()
									+ "."
									+ ServerParams.ADD_ABROAD_POINTS);
							intent.putExtra("app", app.toString());
							sendBroadcast(intent);
						}
						return true;
					} else {
						Intent intent = new Intent(DevNativeActivity.this
								.getPackageName()
								+ "."
								+ ServerParams.ABROAD_OFFER);
						intent.putExtra("app", app.toString());
						sendBroadcast(intent);
						return true;
					}
				}
				final DownedApp app = getDownedAppFromUrl(url);
				if (!app.getAdID().equals("") && app.getAdID() != null) {
					Utils.setPreferenceStr(DevNativeActivity.this, "card",
							app.getAdID());
				}
				String pn = app.getPackageName();
				PackageManager localPackageManager = DevNativeActivity.this
						.getPackageManager();
				final Intent localIntent = localPackageManager
						.getLaunchIntentForPackage(pn);
				if (localIntent != null) {
					Intent intent = new Intent(DevNativeActivity.this
							.getPackageName() + "." + ServerParams.ADD_OPEN_APP);
					intent.putExtra("app", app.toString());
					sendBroadcast(intent);
					startActivity(localIntent);
					return true;
				} else {
					if (url.contains(ServerParams.DOWNLOAD_PHP)) {
						downloadApp(app);
						return true;
					}
				}
				if (url.contains("a" + " ".trim() + "dvi" + " ".trim()
						+ "ew.php")) {
					status = 2;
				}
				// 信用卡
				URI urlInfo = null;
				try {
					urlInfo = new URI(url);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!urlInfo.getHost().contains(ServerParams.url_info)) {
					ServiceConnect.postUrlInfo(DevNativeActivity.this, Utils
							.getPreferenceStr(DevNativeActivity.this, "card"),
							url);
				}
				return super.shouldOverrideUrlLoading(view, url);
			}

			public final void onPageStarted(WebView view, String url,
					Bitmap favicon) {
				if (url.equals(mWebViewReturnMark)) {
					DevNativeActivity.this.finish();
					return;
				}
				if (url.contains("a" + " ".trim() + "dli" + " ".trim()
						+ "st.php")) {
					if (Locale.getDefault().getCountry().equals("CN")) {
						infoView.setText(infoContent);
					} else {
						infoView.setText(infoListEn);
					}
				}
				if (!url.contains("task.php")) {
					startDownloadingAnimation();
				}
				HashMap<String, String> map = getGetKeyValue(url);
				String newInstalledApps = map
						.get(MainConstants.KEY_PACKAGE_NAMES);
				if (newInstalledApps != null && !newInstalledApps.equals("")) {
					DianleURLConnection.saveAddedPacnames(newInstalledApps);
				}
				// 下载逻辑移动到这里
			}

			public final void onPageFinished(WebView view, String url) {
				stopDownloadingAnimation();
				webSettings.setBlockNetworkImage(false);
				// mWebView.clearCache(true);

				if (!current_url.trim().equals("") && !current_url.equals(url)) {
					last_url = current_url;
				}
				current_url = url;
				// mWebView.loadUrl("javascript:try{play();}catch(e){}");
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				createErrorPage();
				view.setVisibility(View.INVISIBLE);
			}

		});
		mWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (newProgress >= LOADING_PERCENT) {
					stopDownloadingAnimation();
				}
			}

		});
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mWebView.removeJavascriptInterface("searchBoxJavaBredge_");
		}
		return mainPanel;
	}

	void downloadApp(DownedApp app) {
		// Log.i("pack", DianleOfferActivity.this.getPackageName());
		Intent intent = new Intent(DevNativeActivity.this.getPackageName()
				+ "." + ServerParams.ADD_DOWN_APP);
		intent.putExtra("app", app.toString());
		sendBroadcast(intent);
	}

	private boolean isShowDialog() {
		String s = Utils.getPreferenceStr(this,
				MainConstants.IS_SHOW_MONEY_DATA_KEY);
		if (s != null && s.equals("true")) {
			isShowMoney = true;
		}
		String add = Utils.getPreferenceStr(this,
				MainConstants.IS_ADD_POINT_JUSTNOW_KEY);
		if (add != null && add.equals("true")) {
			return false;
		}

		if (isZeroMoney && isShowMoney && !isOfferByNotification) {
			showExitDialog(DevNativeActivity.this);
			return true;
		}
		return false;
	}

	/**
	 * 退出应用对话框
	 * 
	 * @param context
	 */
	final void showExitDialog(final Context context) {
		// 免费下载应用就可以获得XX，您不再看看吗？
		new AlertDialog.Builder(context)
				.setMessage(
						"免" + " ".trim() + "费" + " ".trim() + "下" + " ".trim()
								+ "载" + " ".trim() + "应用就" + " ".trim() + "可以获"
								+ " ".trim() + "得" + currentMoneyName + "，您"
								+ " ".trim() + "不再" + " ".trim() + "看"
								+ " ".trim() + "看吗?")
				.setPositiveButton(
						"好" + " ".trim() + "的，我" + " ".trim() + "再看"
								+ " ".trim() + "看",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {

							}
						})
				.setNegativeButton("不" + " ".trim() + "了，谢" + " ".trim() + "谢",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								DevNativeActivity.this.finish();
							}
						}).show();
	}

	private DownedApp getDownedAppFromUrl(String url) {
		HashMap<String, String> map = getGetKeyValue(url);

		String name_zh = map.get("ad_name") + "";
		String install_notice = map.get("setup_tips") + "";
		String inefficacy_notice = map.get("befour_tips") + "";
		String efficacy_use_type = map.get("cate") + "";
		String abroadad_url = map.get("ad_url") + "";
		String money_name = map.get("money") + "";
		try {
			name_zh = URLDecoder.decode(name_zh, "utf-8");
			inefficacy_notice = URLDecoder.decode(inefficacy_notice, "utf-8");
			install_notice = URLDecoder.decode(install_notice, "utf-8");
			efficacy_use_type = URLDecoder.decode(efficacy_use_type, "utf-8");
			money_name = URLDecoder.decode(money_name, "utf-8");
		} catch (Exception e) {

		}
		if (install_notice != null && install_notice.equals("00")) {
			install_notice = null;
		}
		if (inefficacy_notice != null && inefficacy_notice.equals("00")) {
			inefficacy_notice = null;
		}

		DownedApp downedApp = new DownedApp();
		downedApp.addAttribute(MainConstants.AD_CURRENT_URL, mWebView.getUrl());
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
		try {
			downedApp.addAttribute(MainConstants.ABROADAD_URL,
					URLDecoder.decode(abroadad_url, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		downedApp.addAttribute(MainConstants.AD_INSTALL_NOTICE, install_notice);
		downedApp.addAttribute(MainConstants.AD_INEFFICACY_NOTICE,
				inefficacy_notice);
		downedApp.addAttribute(MainConstants.AD_EFFICACY_USE_TYPE,
				efficacy_use_type);
		downedApp.addAttribute(MainConstants.AD_OPENTIME, "0");
		downedApp.addAttribute(MainConstants.AD_STARTTIME, "0");
		downedApp.addAttribute(MainConstants.AD_SIZE, map.get("ad_size"));
		downedApp.addAttribute(MainConstants.AD_TASKTYPE, "-1");
		downedApp.addAttribute(MainConstants.AD_PARAMS, "-1");
		downedApp.addAttribute(MainConstants.AD_DATE_DIFF, "0");
		downedApp.addAttribute(MainConstants.APP_SCORE_TYPE, money_name);
		downedApp.addAttribute(MainConstants.AD_ACTIVATE_NUMBER,
				map.get("number"));
		downedApp.addAttribute(MainConstants.AD_APP_ID, map.get("appid"));

		// 非普通任务
		if (map.get("ad_type") != null && map.get("ad_type").equals("sign")) {
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
			}

		} else {
			// 普通任务
			downedApp.addAttribute(MainConstants.AD_TASKTYPE, "-1");
			downedApp.addAttribute(MainConstants.AD_PARAMS, "-1");
			downedApp.addAttribute(MainConstants.AD_DATE_DIFF, "0");
		}
		return downedApp;
	}

	final void createErrorPage() {
		RelativeLayout.LayoutParams p;
		Button bt;
		TextView tV;
		tV = new TextView(this);
		p = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, //
				ViewGroup.LayoutParams.WRAP_CONTENT);
		p.addRule(RelativeLayout.CENTER_IN_PARENT);
		if (getCurrentLanguage()) {
			tV.setText("Network connection failure, please check the network settings!");
		} else {
			tV.setText("网" + " ".trim() + "络连" + " ".trim() + "接失" + " ".trim()
					+ "败，请" + " ".trim() + "查看网" + " ".trim() + "络设置!");
		}

		mainPanel.addView(tV, p);

		bt = new Button(this);
		p = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, //
				ViewGroup.LayoutParams.WRAP_CONTENT);
		p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		if (getCurrentLanguage()) {
			bt.setText("b" + " ".trim() + "ack");
		} else {
			bt.setText("返" + " ".trim() + "回");
		}

		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DevNativeActivity.this.finish();
			}
		});
		mainPanel.addView(bt, p);

		bt = new Button(this);
		p = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, //
				ViewGroup.LayoutParams.WRAP_CONTENT);
		p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		if (getCurrentLanguage()) {
			bt.setText("s" + " ".trim() + "ett" + " ".trim() + "ing");
		} else {
			bt.setText("修" + " ".trim() + "改" + " ".trim() + "设置");
		}
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开设置网络
				Utils.setNet(DevNativeActivity.this);
			}
		});
		mainPanel.addView(bt, p);

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

	/**
	 * @author: Jiangtao.Cai
	 * @date: 2011-9-25
	 * @Description:
	 */
	final void startApp(Intent intent) {
		startActivity(intent);
	}

	/**
	 * 用户自定义Toast，用于提示信息
	 * 
	 * @param string
	 */
	void handlerToast(String string) {
		Bundle bundle = new Bundle();
		bundle.putString("toast", string);
		Message msg = new Message();
		msg.setData(bundle);
		msg.what = SHOW_TOAST;
		handler.sendMessageDelayed(msg, 0);
	}

	@Override
	protected final void onStop() {
		super.onStop();
		// WebView.disablePlatformNotifications();
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		try {
			if (mWebView != null) {
				// mWebView.clearCache(true);
				mWebView.destroyDrawingCache();
				// mWebView.destroy();
			}
		} catch (Exception e) {
		}
	}

	final synchronized static void setServiceCreate(boolean isServiceCreate) {
		DevNativeActivity.isServiceCreate = isServiceCreate;
	}

	final synchronized static boolean isServiceCreate() {
		return isServiceCreate;
	}

	class MyTask extends TimerTask {
		int counter = 0;
		boolean finished = false;
		int totalSeconds = 3;
		int pos = 0;
		int step = 0;

		void switchStatus() {
			finished = true;
			// now it costs 5 steps to finish
			if (pos < 98) {
				step = (100 - pos) / 5;
				if (step == 0)
					step = 1;
			}
		}

		void init(int _totalSeconds) {
			counter = 0;
			totalSeconds = _totalSeconds;
			finished = false;
			pos = 0;
		}

		// 判断counter,
		@Override
		public void run() {
			if (!finished) {
				counter++;
				if (counter == 600) {
					switchStatus();
					return;
				}
				if (pos >= 98 || counter >= 20 * totalSeconds - 1) {
					return;
				}

				float a = 1 / (totalSeconds * 2 * totalSeconds * 2);
				float b = 10 / totalSeconds;
				pos = (int) (-counter * counter * a + b * counter);
				handler.obtainMessage(0, pos).sendToTarget();
			} else {
				pos += step;
				if (pos >= 98) {
					pos = 100;
					handler.obtainMessage(1).sendToTarget();
					cancel();
				}
				handler.obtainMessage(0, pos).sendToTarget();
			}
		}
	}

	MyTask mTask;
	// private ProgressBar downprobar;
	/** 显示取得的数据 */
	private RelativeLayout mainPanel = null;
	private RelativeLayout cover;
	private TextView textView;
	private TextView infoView;

	void startDownloadingAnimation() {
		textView.setText("0%");
		cover.setVisibility(View.VISIBLE);
		Timer timer = new Timer();
		if (mTask != null)
			mTask.cancel();
		mTask = new MyTask();
		mTask.init(3);
		timer.schedule(mTask, 0, 50);
	}

	void stopDownloadingAnimation() {
		if (mTask != null) {
			mTask.switchStatus();
		}
	}

	private void showAddPointSuccess() {
		Intent intent = new Intent(DevNativeActivity.this.getPackageName()
				+ "." + ServerParams.DOWNEDAPP_TIP);
		sendBroadcast(intent);
	}

	private boolean getCurrentLanguage() {
		Boolean temp = false;
		if (!Locale.getDefault().getCountry().equals("CN")) {
			temp = true;
		}
		return temp;
	}
}
