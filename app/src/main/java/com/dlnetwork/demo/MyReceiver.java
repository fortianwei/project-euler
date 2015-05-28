package com.dlnetwork.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * 注册一个广播,用来监听用户成功激活了一个App
 * */
public class MyReceiver extends BroadcastReceiver {
	//用户成功激活广播的Action
	private static final String ACTION_ADD_SCORE_SUCCESS = "action.add_score.success";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(context.getPackageName() + "."
				+ ACTION_ADD_SCORE_SUCCESS)) {
			//虚拟货币的名称
			String name = intent.getStringExtra("name");
			//虚拟货币的额度
			String number = intent.getStringExtra("number");
			//App的名称
			String app_name=intent.getStringExtra("app_name");
			//App的包名
			String pack_name=intent.getStringExtra("pack_name");
			Toast.makeText(context, app_name+"成功激活,赠送" + number + name+pack_name, 0).show();
			System.out.println(app_name+"成功激活,赠送" + number + name);
		}
	}

}
