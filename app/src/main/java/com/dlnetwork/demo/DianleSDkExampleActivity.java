package com.dlnetwork.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dlnetwork.DevInit;
import com.dlnetwork.GetOnlineParamsListener;
import com.dlnetwork.GetTotalMoneyListener;
import com.dlnetwork.GiveMoneyListener;
import com.dlnetwork.SetTotalMoneyListener;
import com.dlnetwork.SpendMoneyListener;

public class DianleSDkExampleActivity extends Activity implements
		View.OnClickListener, GetTotalMoneyListener, GiveMoneyListener,
		SpendMoneyListener, SetTotalMoneyListener, GetOnlineParamsListener {
	private long amount = 0l, startTime = 0l;
	private EditText mEditTextDelete, mEditTextCurrentID, mEditTextParms,
			mEditTextAdd, mEditTextSet;
	private TextView mTextView = null;
	private DianleSDkExampleActivity me;
	private String message = "", name = "";
	private Button giveMoneyButton, setMoneyButton, showOffers,
			getAmountButton, setCurrentIDButton, spendMoneyButton,
			getParamsButton;
	private final String mDianleDSK = "Dianle SDK";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在应用的入口处设置context
		// 如果是Activity,在第一个启动的Acitivty里面的onCreate的开始写上下面的代码；
		// 如果没有Acitivity，只有Service，在onBind或者onStart方法的开始写上下面的代码
		// 设置app id
		// "072cb4d9d9d5dfd23ed2981e5e33fe59"，是你的应用在点乐服务器注册生成的key,详情请看文档。
		// mDefaultCid : 渠道号，默认的是，可以不要这个参数，原来的Dianle.initDianleContext(this,
		// "072cb4d9d9d5dfd23ed2981e5e33fe59")方法也有效。。  指南针：25291a637696d99c57acc9cef54b9f4e
		//积分墙外放测试APP_ID:072cb4d9d9d5dfd23ed2981e5e33fe59
		//积分墙内部测试APP_ID:78cf8d20e82780c0b57f38b23421009b
		DevInit.initGoogleContext(this, "072cb4d9d9d5dfd23ed2981e5e33fe59");
		// 设置用户ID
		DevInit.setCurrentUserID(this, "1234");
		// webview广告墙
		DevInit.setCustomActivity("com.dlnetwork.demo.MyView");
		// 注册service
		DevInit.setCustomService("com.dlnetwork.demo.MyService");
		setContentView(R.layout.main);
		((TextView) findViewById(R.id.appid)).setText("内部测试的APPID:78cf8d20e82780c0b57f38b23421009b");
		//((TextView) findViewById(R.id.appid)).setText("对外的APPID:78cf8d20e82780c0b57f38b23421009b");
		me = this;
		((TextView) findViewById(R.id.libraryVersion)).setText(mDianleDSK
				+ " version： " + "3.4.6");
		((TextView) findViewById(R.id.libraryTime)).setText("2014/04/23");
		mTextView = (TextView) findViewById(R.id.mainContentTextView);
		showOffers = (Button) findViewById(R.id.showOffers);
		getAmountButton = (Button) findViewById(R.id.getAmountButton);
		setCurrentIDButton = (Button) findViewById(R.id.setCurrentIDButton);
		spendMoneyButton = (Button) findViewById(R.id.spendMoneyButton);
		giveMoneyButton = (Button) findViewById(R.id.giveMoneyButton);
		setMoneyButton = (Button) findViewById(R.id.setMoneyButton);
		getParamsButton = (Button) findViewById(R.id.getParamsButton);
		mEditTextCurrentID = (EditText) findViewById(R.id.scrollviewId)
				.findViewById(R.id.et0);
		mEditTextDelete = (EditText) findViewById(R.id.et1);
		mEditTextAdd = (EditText) findViewById(R.id.et2);
		mEditTextSet = (EditText) findViewById(R.id.et3);
		mEditTextParms = (EditText) findViewById(R.id.et4);
		showOffers.setText("webview样式广告墙" + name);
		// showActivityOffersButton.setText("本地原生Activity广告墙");
		showOffers.setOnClickListener(this);
		// showActivityOffersButton.setOnClickListener(this);
		getAmountButton.setText("查询我的" + name + "总额");
		getAmountButton.setOnClickListener(this);

		setCurrentIDButton.setOnClickListener(this);
		setMoneyButton.setOnClickListener(this);

		spendMoneyButton.setText("扣除" + name);
		spendMoneyButton.setOnClickListener(this);

		giveMoneyButton.setText("增加" + name);
		giveMoneyButton.setOnClickListener(this);

		getParamsButton.setOnClickListener(this);
		DevInit.getTotalMoney(this, new GetTotalMoneyListener() {

			@Override
			public void getTotalMoneySuccessed(String name0, long amount) {
				name = name0;
				//
			}

			@Override
			public void getTotalMoneyFailed(String error) {
			}
		});
		// Intent intent=new Intent(this, HelpService.class);
		// startService(intent);
	}

	public void onClick(View v) {
		if (v instanceof Button) {
			int id = ((Button) v).getId();
			switch (id) {
			case R.id.showOffers:
				DevInit.showOffers(this);
				break;
			case R.id.getAmountButton:
				startTime = System.currentTimeMillis();
				DevInit.getTotalMoney(this, me);
				break;
			case R.id.spendMoneyButton:
				startTime = System.currentTimeMillis();
				DevInit.spendMoney(this, readEditTextView(mEditTextDelete), me);
				break;
			case R.id.setCurrentIDButton:
				startTime = System.currentTimeMillis();
				DevInit.setCurrentUserID(this, mEditTextCurrentID.getText()
						.toString());
				message = "  2.6新增非托管货币功能，请在用户登录或者更换账户后立即调用此接口来设置用户ID，详情请参考文档。";
				mTextView.setText(message);
				break;
			case R.id.giveMoneyButton:
				startTime = System.currentTimeMillis();
				DevInit.giveMoney(this, readEditTextView(mEditTextAdd), me);
				break;
			case R.id.setMoneyButton:
				startTime = System.currentTimeMillis();
				DevInit.setTotalMoney(this, readEditTextView(mEditTextSet), me);
				break;
			case R.id.getParamsButton:
				DevInit.getOnlineParams(this,
						readEditTextViewString(mEditTextParms), me, "false");
				String value = DevInit.getOnlineParams(this,
						readEditTextViewString(mEditTextParms), "true");
				message = "自定义参数的值为: " + value;
				mTextView.setText(message);
				break;
			default:
				break;

			}
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		DevInit.getTotalMoney(this, me);
	}

	public int readEditTextView(EditText editText) {
		int result = -1;
		String string = "";
		string = editText.getText().toString();
		try {
			result = Integer.parseInt(string);
			if (result < 0) {
				return -1;
			}
		} catch (NumberFormatException e) {
			mTextView.setText("请输入正整数");
		}
		return result;
	}

	public String readEditTextViewString(EditText editText) {
		String string = editText.getText().toString();
		if (string == null || string.trim().equals("")) {
			return null;
		} else {
			return string;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("result", "...Main activity onResume()...");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("result", "...Main activity onDestroy()...");
	}

	/********************************** 所有回调方法均插入主线程执行，可以直接操作UI **********************************/

	/**
	 * @Description: 获取虚拟货币余额的回调方法，成功则返回虚拟货币的名称，余额 * @param amount 虚拟货币的余额 name
	 *               虚拟货币的名称
	 */
	@Override
	public void getTotalMoneySuccessed(String name0, long amount0) {
		Log.w("result", ">>>>>>>><<<<<"
				+ (System.currentTimeMillis() - startTime) + ">>>>>>>><<<<<");
		name = name0;
		amount = amount0;
		message = name + "总额: " + amount + "(" + name + ")";
		mTextView.setText(message);
		// showOffers.setText("免费获取" + name);
		// getAmountButton.setText("查询我的" + name + "总额");
		// spendMoneyButton.setText("扣除" + name);
	}

	/**
	 * @Description: 获取虚拟货币余额的回调方法 * @param error 失败时返回失败的信息
	 */
	@Override
	public void getTotalMoneyFailed(String error) {
		message = "得到了错误信息：" + error;
		mTextView.setText(message);
	}

	/**
	 * 扣除虚拟货币的回调方法。扣除成功则返回余额
	 * 
	 * @param amount
	 *            虚拟货币的余额
	 */
	@Override
	public void spendMoneySuccess(long amount) {
		Log.w("result", ">>>>>>>><<<<<"
				+ (System.currentTimeMillis() - startTime) + ">>>>>>>><<<<<");
		this.amount = amount;
		message = name + "总额: " + amount + "(" + name + ")";
		mTextView.setText(message);
	}

	/**
	 * 扣除虚拟货币的回调方法。失败时返回失败的信息
	 * 
	 * @param error
	 *            失败时返回失败的信息
	 */
	@Override
	public void spendMoneyFailed(String error) {
		message = "得到了错误信息：" + error;
		mTextView.setText(message);
	}

	@Override
	/** @Description: 赠送 虚拟币的接口的回调方法 ,赠送成功则返回用户的余额 * @param amount 虚拟货币的余额*/
	public void giveMoneySuccess(long amount) {
		Log.w("result", ">>>>>>>><<<<<"
				+ (System.currentTimeMillis() - startTime) + ">>>>>>>><<<<<");
		this.amount = amount;
		message = name + "总额: " + amount + "(" + name + ")";
		mTextView.setText(message);
	}

	/** @Description: 赠送 虚拟币的接口的回调方法,失败则返回失败信息 * @param error 失败时返回失败的信息 */
	@Override
	public void giveMoneyFailed(String error) {
		message = "得到了错误信息：" + error;
		mTextView.setText(message);
	}

	/** @Description: 设置用户虚拟货币总额成功则返回虚拟货币的名称，余额 */
	@Override
	public void setTotalMoneySuccessed(String name, long amount) {
		Log.w("result", ">>>>>>>><<<<<"
				+ (System.currentTimeMillis() - startTime) + ">>>>>>>><<<<<");
		message = "设置" + name + "总额为: " + amount + "(" + name + ")";
		mTextView.setText(message);
	}

	/**
	 * @Description: 设置用户虚拟货币总额失败时返回失败的信息
	 */
	@Override
	public void setTotalMoneyFailed(String error) {
		message = "得到了错误信息：" + error;
		mTextView.setText(message);
	}

	/**
	 * @Description: 获取自定义在线参数，返回自定义的参数值，无网或请求失败返回的是实时的值，无网或请求失败返回本地保存的值。
	 */
	@Override
	public void onParamsReturn(String value) {
		Log.w("result", ">>>>>>>><<<<<"
				+ (System.currentTimeMillis() - startTime) + ">>>>>>>><<<<<");
		message = "自定义参数的值为: " + value;
		mTextView.setText(message);
	}
}
