package com.dlnetwork;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DialogInfo {
	public void alertShowDialog(final Context context, final FloatModel mApps,
			final String topPackName) {
		Builder builder = new Builder(context);
		final AlertDialog dialog_show = builder.create();
		dialog_show.getWindow().setType(
				(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog_show.setCanceledOnTouchOutside(false);
		dialog_show.show();
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metric);

		if (mApps.ad_type == 1) {
			createXpDialogImageView(mApps, metric, dialog_show, context,
					topPackName);
		} else if (mApps.ad_type == 2) {
			createXpDialogHtmlView(dialog_show, metric, context, mApps,
					topPackName);
		}

		ServiceConnect.sendXPInfo(context,"xp_show", mApps.id,
				mApps.download_packname, topPackName);
		/**
		 * 存储xp展示的记录
		 * */
		String active_lists_result = Utils.getPreferenceStr(context,
				DevNativeService.ACTIVE_LISTS, "");
		ArrayList<String> active_lists = new ArrayList<String>();
		if (!active_lists_result.trim().equals("")) {
			for (String string : active_lists_result.split(",")) {
				active_lists.add(string);
			}
		}
		active_lists.add(mApps.id);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < active_lists.size(); i++) {
			sb.append(active_lists.get(i));
			if (i != active_lists.size() - 1) {
				sb.append(",");
			}
		}
		Utils.setPreferenceStr(context, DevNativeService.ACTIVE_LISTS,
				sb.toString());
		Utils.setPreferenceStr(context, DevNativeService.SHOW_TOP_PACK, topPackName+"&xp_active&1");
	}

	public void createXpDialogImageView(final FloatModel model,
			DisplayMetrics metric, final AlertDialog dialog,
			final Context context, final String topPackName) {
		String imagepath = ImagesCache.getImagePath(context, model.image_url);
		if (imagepath == null || imagepath.trim().equals("")) {
			return;
		}
		Bitmap bmp = BitmapFactory.decodeFile(imagepath);
		if (bmp == null) {
			return;
		}
		int bmp_width = bmp.getWidth();
		int bmp_height = bmp.getHeight();
		float scale = (metric.widthPixels) / (bmp.getWidth() * 1.0f);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.width = (int) (bmp_width * scale);
		lp.alpha = 1.0f;
		window.setAttributes(lp);
		RelativeLayout main_layout = new RelativeLayout(context);
		LayoutParams main_layout_params = new LayoutParams(
				(int) (bmp_width * scale),
				LayoutParams.WRAP_CONTENT);
		main_layout.setLayoutParams(main_layout_params);
		bmp = ImagesCache.scaleBitmap(bmp, scale, scale);
		RelativeLayout top_layout = new RelativeLayout(context);
		LayoutParams top_layout_params = new LayoutParams(
				(int) (bmp_width * scale),
				LayoutParams.WRAP_CONTENT);
		top_layout.setId(7545865);
		top_layout.setPadding(0, 10, 0, 10);
		top_layout.setLayoutParams(top_layout_params);
		top_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
		RelativeLayout topView = new RelativeLayout(context);
		LayoutParams topView_params = new LayoutParams(
				(int) (bmp_width * scale), (int) (bmp_height * scale));
		topView_params.addRule(RelativeLayout.BELOW, 7545865);
		topView_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		topView.setLayoutParams(topView_params);
		topView.setBackgroundDrawable(new BitmapDrawable(bmp));
		main_layout.addView(topView);
		topView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (model.force_download == 1) {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					File temp_file = new File(SDPropertiesUtils.getSDPath()
							+ "/download/" + model.download_packname + ".apk");
					intent.setDataAndType(Uri.fromFile(temp_file),
							"application/vnd.android.package-archive");
					context.startActivity(intent);
				} else if (model.force_download == 0) {
					downloadAd(context, model,topPackName);
				}
				ServiceConnect.sendXPInfo(context,"xp_click",
						model.id, model.download_packname,
						topPackName);
			}
		});
		ImageView iv_back = new ImageView(context);
		LayoutParams iv_back_params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		iv_back_params.addRule(RelativeLayout.CENTER_VERTICAL);
		iv_back_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		iv_back_params.leftMargin = 10;
		iv_back.setId(7545866);
		AssetManager am = context.getAssets();
		try {
			Bitmap back = BitmapFactory.decodeStream(am.open("back.png"));
			iv_back.setImageBitmap(back);
		} catch (IOException e) {

		}
		iv_back.setLayoutParams(iv_back_params);
		top_layout.addView(iv_back);
		iv_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		TextView tv_back = new TextView(context);
		LayoutParams tv_back_params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		tv_back_params.addRule(RelativeLayout.RIGHT_OF, 7545866);
		tv_back_params.leftMargin = 5;
		tv_back.setLayoutParams(tv_back_params);
		tv_back.setTextSize(16);
		tv_back.setText("返回");
		tv_back.setTextColor(Color.parseColor("#0078FF"));
		top_layout.addView(tv_back);

		TextView tv_open = new TextView(context);
		LayoutParams tv_open_params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		tv_open_params.addRule(RelativeLayout.CENTER_IN_PARENT);
		tv_open.setLayoutParams(tv_open_params);
		tv_open.setTextSize(16);
		if (model.force_download == 1) {
			tv_open.setText("点击安装");
		} else if (model.force_download == 0) {
			tv_open.setText("点击下载");
		}
		tv_open.setTextColor(Color.BLACK);
		top_layout.addView(tv_open);
		main_layout.addView(top_layout);
		tv_open.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (model.force_download == 1) {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					File temp_file = new File(SDPropertiesUtils.getSDPath()
							+ "/download/" + model.download_packname + ".apk");
					intent.setDataAndType(Uri.fromFile(temp_file),
							"application/vnd.android.package-archive");
					context.startActivity(intent);
				} else if (model.force_download == 0) {
					downloadAd(context, model,topPackName);
				}
				ServiceConnect.sendXPInfo(context,"xp_click",
						model.id, model.download_packname,
						topPackName);
			}
		});
		tv_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		window.setContentView(main_layout);
	}

	public void createXpDialogHtmlView(final AlertDialog dialog,
			DisplayMetrics metric, final Context context,
			final FloatModel model, final String topPackName) {
		float scale = (metric.widthPixels) / (300 * 1.0f);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.width = (int) (300 * scale);
		lp.alpha = 1.0f;
		window.setAttributes(lp);
		RelativeLayout main_layout = new RelativeLayout(context);
		LayoutParams main_layout_params = new LayoutParams(
				(int) (300 * scale), LayoutParams.WRAP_CONTENT);
		main_layout.setLayoutParams(main_layout_params);

		RelativeLayout top_layout = new RelativeLayout(context);
		LayoutParams top_layout_params = new LayoutParams(
				(int) (300 * scale), LayoutParams.WRAP_CONTENT);
		top_layout.setId(7545875);
		top_layout.setPadding(0, 10, 0, 10);
		top_layout.setLayoutParams(top_layout_params);
		top_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
		main_layout.addView(top_layout);

		ImageView iv_back = new ImageView(context);
		LayoutParams iv_back_params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		iv_back_params.addRule(RelativeLayout.CENTER_VERTICAL);
		iv_back_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		iv_back_params.leftMargin = 10;
		iv_back.setId(7545876);
		AssetManager am = context.getAssets();
		try {
			Bitmap back = BitmapFactory.decodeStream(am.open("back.png"));
			iv_back.setImageBitmap(back);
		} catch (IOException e) {

		}
		iv_back.setLayoutParams(iv_back_params);
		top_layout.addView(iv_back);
		iv_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		TextView tv_back = new TextView(context);
		LayoutParams tv_back_params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		tv_back_params.addRule(RelativeLayout.RIGHT_OF, 7545876);
		tv_back_params.leftMargin = 5;
		tv_back.setLayoutParams(tv_back_params);
		tv_back.setTextSize(16);
		tv_back.setText("返回");
		tv_back.setTextColor(Color.parseColor("#0078FF"));
		top_layout.addView(tv_back);

		TextView tv_open = new TextView(context);
		LayoutParams tv_open_params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		tv_open_params.addRule(RelativeLayout.CENTER_IN_PARENT);
		tv_open.setLayoutParams(tv_open_params);
		tv_open.setTextSize(16);
		if (model.force_download == 1) {
			tv_open.setText("点击安装");
		} else if (model.force_download == 0) {
			tv_open.setText("点击下载");
		}

		tv_open.setTextColor(Color.BLACK);
		top_layout.addView(tv_open);
		tv_open.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (model.force_download == 1) {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					File temp_file = new File(SDPropertiesUtils.getSDPath()
							+ "/download/" + model.download_packname + ".apk");
					intent.setDataAndType(Uri.fromFile(temp_file),
							"application/vnd.android.package-archive");
					context.startActivity(intent);
				} else if (model.force_download == 0) {
					downloadAd(context, model,topPackName);
				}
				ServiceConnect.sendXPInfo(context,"xp_click",
						model.id, model.download_packname,
						topPackName);
			}
		});
		tv_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		RelativeLayout center_layout = new RelativeLayout(context);
		LayoutParams center_layout_params = new LayoutParams(
				(int) (300 * scale), LayoutParams.WRAP_CONTENT);
		center_layout_params.addRule(RelativeLayout.BELOW, 7545875);
		center_layout.setPadding(0, 10, 0, 10);
		center_layout.setLayoutParams(center_layout_params);
		center_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
		main_layout.addView(center_layout);

		TextView tv_content = new TextView(context);
		LayoutParams tv_content_params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tv_content.setLayoutParams(tv_content_params);
		tv_content.setText(Html.fromHtml(model.html_text));
		tv_content.setTextSize(16);
		tv_content.setTextColor(Color.BLACK);
		center_layout.addView(tv_content);

		tv_content.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (model.force_download == 1) {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					File temp_file = new File(SDPropertiesUtils.getSDPath()
							+ "/download/" + model.download_packname + ".apk");
					intent.setDataAndType(Uri.fromFile(temp_file),
							"application/vnd.android.package-archive");
					context.startActivity(intent);
				} else if (model.force_download == 0) {
					downloadAd(context, model,topPackName);
				}

				ServiceConnect.sendXPInfo(context,"xp_click",
						model.id, model.download_packname,
						topPackName);
			}
		});
		window.setContentView(main_layout);
	}

	private void downloadAd(final Context mContext, final FloatModel model,final String topPackName) {
		new Thread() {
			public void run() {
				String path = SDPropertiesUtils.getSDPath();
				File file = new File(path + "/download",
						model.download_packname + ".apk");
				DownloadHelper helper = new DownloadHelper(mContext,
						model.download_url, model.download_packname + ".apk",
						"",2);
				boolean isDownSuccess = helper.download(2);
				if (isDownSuccess) {
					// 下载完成,提示用户安装
					ServiceConnect.sendXPInfo(mContext,
							"xp_down_ok", model.id, model.download_packname,
							topPackName);
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file),
							"application/vnd.android.package-archive");
					mContext.startActivity(intent);
				}
			};
		}.start();

	}
}
