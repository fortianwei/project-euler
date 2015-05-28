package com.dlnetwork;

final class ServerParams {
	static final String DIANLE_CID = "de" + " ".trim() + "fau" + " ".trim()
			+ "lt";
	static final String APP_ID = "a" + " ".trim() + "pp_i" + " ".trim() + "d";
	static final String dlnetwork = "com.d" + " ".trim() + "lnet" + " ".trim()
			+ "work.cid";
	static final String dianle = "com.di" + " ".trim() + "anl" + " ".trim()
			+ "e.cid";
	static final String url_info = "di" + " ".trim() + "an" + " ".trim() + "jo"
			+ " ".trim() + "y.com";
	static final String url_return = "di" + " ".trim() + "an" + " ".trim()
			+ "jo" + " ".trim() + "y:ret" + " ".trim() + "urn";
	static final String ver_id = "verify_id";
	/** url线上正式地址 */
	static final String PRE_STRING = "http://a.diXanjoTy.com/dev/";

	static final String PRE_FLOAT = "http://c.diXanjoTy.com/dev/";// "http://c.dianjoy.com/dev/";

	/** 获取软件列表地址 **/
	static final String TEST_GET_OFFERS_URL = PRE_STRING.replace("X", "")
			.replace("T", "")
			+ "api/ad"
			+ " ".trim()
			+ "li"
			+ " ".trim()
			+ "st/adlist.php?";
	/** 获取积分地址 **/
	static final String TEST_GET_POINTS_URL = PRE_STRING.replace("X", "")
			.replace("T", "") + "api/user_account.php?";
	/** 获取下载列表剩余虚拟货币总额地址 **/
	static final String TEST_GET_AD_AMOUNT_URL = PRE_STRING.replace("X", "")
			.replace("T", "") + "api/ad_account.php?";

	static final String SPEND_POINT_URL = PRE_STRING.replace("X", "").replace(
			"T", "")
			+ "api/spend.php?";

	static final String GIVE_POINT_URL = PRE_STRING.replace("X", "").replace(
			"T", "")
			+ "api/give.php?";
	/** 获取自定义参数地址 **/
	static final String GET_SETED_PARAMS = PRE_STRING.replace("X", "").replace(
			"T", "")
			+ "api/param.php?";
	/** 连接地址 **/
	static final String CONNECT_URL = PRE_STRING.replace("X", "").replace("T",
			"")
			+ "api/connect.php?";
	static final String RESPONSE_DOWN_OK_URL = PRE_STRING.replace("X", "")
			.replace("T", "") + "api/downok.php?";
	static final String GET_INSTALLOK = PRE_STRING.replace("X", "").replace(
			"T", "")
			+ "api/install.php?";
	static final String ABROAD_CLICK = PRE_STRING.replace("X", "").replace("T",
			"")
			+ "api/adl" + " ".trim() + "is" + " ".trim() + "t/click.php?";
	/** 增加积分地址 **/
	static final String TEST_ADD_MONEY_URL = PRE_STRING.replace("X", "")
			.replace("T", "") + "api/adnotify.php?";
	static final String RESPONSE_REMOTE_ROUTING = PRE_STRING.replace("X", "")
			.replace("T", "") + "api/remote_routing.php?";
	static final String DEPTHTASK__ADDPOINTS_URL = PRE_STRING.replace("X", "")
			.replace("T", "")
			+ "api/ad"
			+ " ".trim()
			+ "lis"
			+ " ".trim()
			+ "t/task_notify.php?";
	static final String ADINFO_URL = PRE_FLOAT.replace("X", "")
			.replace("T", "") + "api/adxp/popadinfo.php?";

	static final String XPADINFO_URL = PRE_FLOAT.replace("X", "").replace("T",
			"")
			+ "api/adxp/stat/xp_stat.php?";
	static final String DOWNLOAD_PHP = "download.php?";

	/** action */
	static final String ADD_OPEN_APP = "android.intent.action.add_open_app";
	static final String ADD_DOWN_APP = "android.intent.action.add_downloading_app";
	static final String STOP_DOWNLOADING_APP = "android.intent.action.stop_dowsnloading_app";
	static final String ADD_ABROAD_POINTS = "android.intent.action.add_abroad_points";
	static final String ABROAD_OFFER = "android.intent.action.downedapp.abroadoffer";
	static final String DOWNEDAPP_TIP = "android.intent.action.downedapp.tip";
	static final String DOWNDATA_RECEIVE = "com.downapp_receive";
	static final String CIRCLE_TIMER = "android.intent.action.downedapp.circletimer";
	static final String DOWNEDAPP_TASK_TIP = "android.intent.action.downedapp.tasktip";
	static final String ACTION_ADD_SCORE_SUCCESS = "action.add_score.success";
}
