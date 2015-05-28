package com.dlnetwork;

import android.os.Handler;

import java.util.HashMap;

final class DownedApp {
	static final int HAS_NOT_ADD_POINT = 0;
	static final int HAS_ADD_POINT = 1;
	static final int HAS_SHOW_SUCCESS_TOAST = 2;
	static final int FAILED_HAS_ADD_POINT = 3;

	private HashMap<String, String> attributes = new HashMap<String, String>();

	int addPointStep = HAS_NOT_ADD_POINT;
	/** the time of app downloaded */
	// long downedTime;

	long app_put_in_list_time = 0;
	/** the seconds of app activing */
	int activeSeconds = 0;
	int requestAddPointTimes;
	NotifiMana mNotifiMana;

	@Override
	public String toString() {
		return Utils.mapToString(attributes);
	}

	static DownedApp parse(String s) {
		DownedApp downedApp = new DownedApp();
		downedApp.attributes = Utils.stringToMap(s);
		return downedApp;
	}

	void addAttribute(String key, String val) {
		if (val == null)
			val = "";
		attributes.put(key, val);
	}

	String getAdID() {
		return attributes.get(MainConstants.AD_ID);
	}

	String getPackageName() {
		return attributes.get(MainConstants.AD_PACK_NAME);
	}

	String getName() {
		return attributes.get(MainConstants.AD_NAME);
	}

	String getUrl() {
		return attributes.get(MainConstants.AD_URL);
	}

	String getCurrentUrl() {
		return attributes.get(MainConstants.AD_CURRENT_URL);
	}

	String getCanGivePoint() {
		return attributes.get(MainConstants.AD_CAN_GIVE_POINT);
	}

	String getInstallNotice() {
		return attributes.get(MainConstants.AD_INSTALL_NOTICE);
	}

	String getEfficacyUseType() {
		return attributes.get(MainConstants.AD_EFFICACY_USE_TYPE);
	}

	String getAddShortCut() {
		return attributes.get(MainConstants.AD_ADD_SHORTCUT);
	}

	String getOpenTime() {
		return attributes.get(MainConstants.AD_OPENTIME);
	}

	String getActivateNumber() {
		return attributes.get(MainConstants.AD_ACTIVATE_NUMBER);
	}

	String getAppScoreName() {
		return attributes.get(MainConstants.APP_SCORE_TYPE);
	}

	boolean equals(DownedApp app) {
		return (getAdID() != null) && (getAdID().equals(app.getAdID()));
	}

	int getActiveTime() {
		String active_time = attributes.get(MainConstants.AD_ACTIVE_TIME);
		int activeTime = 0;
		if (active_time != null) {
			try {
				activeTime = Integer.parseInt(active_time);
			} catch (NumberFormatException e) {
				activeTime = 0;
			}
		}
		return activeTime;
	}

	double getAdSize() {
		String ad_size = attributes.get(MainConstants.AD_SIZE);
		double adSize = 0;
		if (ad_size.equals("") || ad_size.equals(null)) {
			return adSize;
		} else {
			ad_size = ad_size.replace(" ", "").toLowerCase();
			if (ad_size.contains("m")) {
				ad_size = ad_size.substring(0, ad_size.indexOf("m"));
			} else if (ad_size.contains("k")) {
				ad_size = ad_size.substring(0, ad_size.indexOf("k"));
			}
			adSize = Double.parseDouble(ad_size);
			return adSize;
		}
	}

	int getTaskType() {
		return Integer.parseInt(attributes.get(MainConstants.AD_TASKTYPE));
	}

	int getTaskParams() {
		return Integer.parseInt(attributes.get(MainConstants.AD_PARAMS));
	}

	int getStartTime() {
		return Integer.parseInt(attributes.get(MainConstants.AD_STARTTIME));
	}

	int getNumber() {
		return Integer.parseInt(attributes.get(MainConstants.AD_NUMBER));
	}

	String getTaskId() {
		return attributes.get(MainConstants.AD_TASKID);
	}

	String getDeadLine() {
		return attributes.get(MainConstants.AD_DEADLINE_TIME);
	}

	int getDateDiff() {
		return Integer.parseInt(attributes.get(MainConstants.AD_DATE_DIFF));
	}

	String getAbroadAdUrl() {
		return attributes.get(MainConstants.ABROADAD_URL);
	}

	String isViaScreenOnNotify() {
		return attributes.get(MainConstants.AD_IS_VIA_SCREEN_ON_NOTIFY);
	}

	String getDownOkTime() {
		return attributes.get(MainConstants.AD_DOWN_OK);
	}

	String getShowStatus() {
		return attributes.get(MainConstants.AD_SHOW_STATUS);
	}
	String getAppId(){
		return attributes.get(MainConstants.AD_APP_ID);
	}
	Handler handler;

	public void setHandler(Handler _handler) {
		handler = _handler;
	}

	public Handler getHandler() {
		return handler;
	}
}
