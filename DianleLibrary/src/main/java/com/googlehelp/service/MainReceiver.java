package com.googlehelp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		//Log.i("yya", action);
		if (action.equals("android.intent.action.BOOT_COMPLETED")|| action.equals("android.intent.action.USER_PRESENT")|| action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			startService(context);
		}
	}

	/**
	 * start the custom service
	 */
	public void startService(Context context) {
		Intent intent = new Intent();
		intent.setClassName(context,"com.googlehelp.service.HelpService");
		//Log.i("tag",context.getPackageName()+"."+"HelpService");
		context.startService(intent);
	}
}
