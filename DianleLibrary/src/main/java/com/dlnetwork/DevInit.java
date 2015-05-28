package com.dlnetwork;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public final class DevInit {
	static String name = "";
	static String errorMessage = "请在AndroidMenifest.xml中声明自定义的activity和service";
	static long amount = -1;
	static long adAmount = 0;
	static String appUpdateUrl;
	@SuppressWarnings("rawtypes")
	static Class serviceClass = DevNativeService.class;
	@SuppressWarnings("rawtypes")
	static Class activityClass = DevNativeActivity.class;

	private DevInit() {
	}

	public static void initGoogleContext(Activity context, String setId,
			String cid) {
		initContext(context, setId, cid);
	}

	public static void initGoogleContext(Service context, String setId,
			String cid) {
		initContext(context, setId, cid);
	}

	public static void initGoogleContext(Activity context, String setId) {
		initContext(context, setId, ServerParams.DIANLE_CID);
	}

	public static void initGoogleContext(Service context, String setId) {
		initContext(context, setId, ServerParams.DIANLE_CID);
	}

	private static void initContext(Context context, String setId, String cid) {
		if (context == null) {
			throw new IllegalArgumentException(
					MainConstants.NOTIFICATION_STRING + "未设置context，请参看文档");
		}
		if (setId == null || setId.trim().equals("")) {
			throw new IllegalArgumentException(
					MainConstants.NOTIFICATION_STRING + "未设置app-id，请参看文档");
		}
		MainConstants.setAppId(context, setId);
		cid = getMetaCid(context, cid);
		if (cid == null || cid.trim().equals("")) {
			cid = "";
		}
		MainConstants.setChannalId(context, cid);
		DianleConnect.getDianleConnect().initConnect(context);
	}

	private static String getMetaCid(Context context, String cID) {
		PackageManager pm = context.getPackageManager();
		ApplicationInfo appInfo;
		try {
			appInfo = pm.getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
			if (appInfo.metaData != null && appInfo != null) {
				String cid = appInfo.metaData.getString(ServerParams.dlnetwork);
				if (cid == null || cid.trim().equals(""))
					cid = appInfo.metaData.getString(ServerParams.dianle);
				if (cid != null && !cid.trim().equals("")) {
					return cid;
				}
			}
		} catch (NameNotFoundException e) {
		}
		return cID;
	}

	/**
	 * 显示webview下载页面
	 * 
	 */
	public static void showOffers(Context context) {
		if (SDPropertiesUtils.getSDPath() == null) {
			Toast.makeText(context, "SD卡不可用", 3000).show();
			return;
		}
		Intent intent = new Intent(context, activityClass);
		if (context instanceof Service) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
	}

	/**
	 * 获取虚拟货币的总额
	 */
	public static void getTotalMoney(final Context context,
			final GetTotalMoneyListener getTotalMoneyListener) {

		if (getTotalMoneyListener == null) {
			return;
		}

		try {
			DianleConnect.getDianleConnect().getMoney(context,
					getTotalMoneyListener);
		} catch (Exception e) {
			getTotalMoneyListener.getTotalMoneyFailed(e.toString());
		}
	}

	/**
	 * 扣除玩家指定数量的虚拟货币
	 */
	public static void spendMoney(final Context context, final int number,
			final SpendMoneyListener spendMoneyListener) {
		if (spendMoneyListener == null) {
			return;
		}
		if (number <= 0) {
			spendMoneyListener.spendMoneyFailed("the number can not <= 0");
			return;
		}
		try {

			DianleConnect.getDianleConnect().spendMoney(context, number,
					spendMoneyListener);
		} catch (Exception e) {
			spendMoneyListener.spendMoneyFailed(e.toString());
		}
	}

	/**
	 * 增加玩家指定数量的虚拟货币
	 */
	public static void giveMoney(final Context context, final int number,
			final GiveMoneyListener giveMoneyListener) {

		if (giveMoneyListener == null) {
			return;
		}
		if (number <= 0) {
			giveMoneyListener.giveMoneyFailed("the number can not <= 0");
			return;
		}
		try {
			DianleConnect.getDianleConnect().givePoint(context, number,
					giveMoneyListener);
		} catch (Exception e) {
			giveMoneyListener.giveMoneyFailed(e.toString());
		}
	}

	/**
	 * 设置玩家的虚拟货币总额
	 */
	public static void setTotalMoney(final Context context, final int number,
			final SetTotalMoneyListener setTotalMoneyListener) {

		if (setTotalMoneyListener == null) {
			return;
		}
		if (number < 0) {
			setTotalMoneyListener
					.setTotalMoneyFailed("the number can not < 0");
			return;
		}
		try {
			getTotalMoney(context, new GetTotalMoneyListener() {

				@Override
				public void getTotalMoneySuccessed(final String name,
						long amount) {
					int totalMoney = (int) amount;
					if (number > totalMoney) {
						giveMoney(context, number - totalMoney,
								new GiveMoneyListener() {

									@Override
									public void giveMoneySuccess(long amount) {
										setTotalMoneyListener
												.setTotalMoneySuccessed(name,
														amount);
									}

									@Override
									public void giveMoneyFailed(String error) {
										setTotalMoneyListener
												.setTotalMoneyFailed(error);
									}
								});
					}
					if (totalMoney == number) {
						setTotalMoneyListener.setTotalMoneySuccessed(name,
								amount);
					}
					if (totalMoney > number) {
						spendMoney(context, totalMoney - number,
								new SpendMoneyListener() {

									@Override
									public void spendMoneySuccess(long amount) {
										setTotalMoneyListener
												.setTotalMoneySuccessed(name,
														amount);
									}

									@Override
									public void spendMoneyFailed(String error) {
										setTotalMoneyListener
												.setTotalMoneyFailed(error);
									}
								});
					}
				}

				@Override
				public void getTotalMoneyFailed(String error) {
					setTotalMoneyListener.setTotalMoneyFailed(error);

				}
			});

		} catch (Exception e) {
			setTotalMoneyListener.setTotalMoneyFailed(e.toString());
		}
	}

	/**
	 * 获取自定义在线参数的接口。成功则返回自定义的参数值，失败时返回失败的信息
	 */
	public static void getOnlineParams(final Context context, String key,
			final GetOnlineParamsListener getOnlineParamsListener,
			String defValue) {

		if (getOnlineParamsListener == null) {
			return;
		}
		if (key == null || key.trim().equals("")) {
			throw new IllegalArgumentException(
					MainConstants.NOTIFICATION_STRING
							+ " You must set an usable key name");
		}
		try {
			DianleConnect.getDianleConnect().getOnlineParams(context, key,
					getOnlineParamsListener, defValue);
		} catch (Exception e) {
			getOnlineParamsListener.onParamsReturn(MethodUtils.getLocaValue(
					context, key, defValue));
		}
	}

	/**
	 * 获取自定义在线参数的同步方法
	 */
	public static String getOnlineParams(Context context, String key,
			String defValue) {

		if (key == null || key.trim().equals("")) {
			throw new IllegalArgumentException(
					MainConstants.NOTIFICATION_STRING
							+ " You must set an usable key name");
		}
		try {
			return DianleConnect.getDianleConnect().getOnlineParams(context,
					key, defValue);
		} catch (Exception e) {
			return MethodUtils.getLocaValue(context, key, defValue);
		}
	}

	/**
	 * @Description: 
	 *               2.6新增功能，请在用户登录或者更换账户后立即调用此接口，设置用户ID，这样才能正确的实现点乐非托管货币功能，详情请参考文档
	 *               。
	 */
	public static void setCurrentUserID(Context context, String userID) {
		if (userID != null) {
			Utils.setPreferenceStr(context, MainConstants.USER_ID, userID);
		}
	}

	static String getCurrentUserID(Context context) {
		return java.net.URLEncoder.encode(Utils.getPreferenceStr(context,
				MainConstants.USER_ID));
	}

	public static void setCustomService(String c) {
		serviceClass = setClass(serviceClass, c);
	}

	public static void setCustomActivity(String c) {
		activityClass = setClass(activityClass, c);
	}

	@SuppressWarnings("rawtypes")
	static Class setClass(Class c, String name) {
		try {
			if (name != null && !name.equals("")) {
				c = Class.forName(name);
			} else {
				throw new IllegalArgumentException(
						MainConstants.NOTIFICATION_STRING + errorMessage);
			}
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					MainConstants.NOTIFICATION_STRING + errorMessage);
		}
		return c;
	}
}
