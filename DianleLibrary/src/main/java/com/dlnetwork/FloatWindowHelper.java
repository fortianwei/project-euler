package com.dlnetwork;

import android.content.Context;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FloatWindowHelper {
	public static void initFloatContent(final Context context) {
		// TODO Auto-generated method stub
		String app_id = MainConstants.getAppId(context);
		MainConstants.setFloatAppId(context, app_id);
		String content = DianleURLConnection.connectToURL(
				ServerParams.ADINFO_URL, Utils.getDeviceInfo(context, app_id)
						+ "&pplib_version="
						+ MainConstants.POPO_LIBRARY_VERSION);
		Utils.setPreferenceStr(context,
				MainConstants.CONTENT_TEMPLATE_SAVE_TIME,
				String.valueOf(System.currentTimeMillis()));
		if (content != null && !content.trim().equals("")
				&& !content.trim().equals("null")) {
			Utils.setPreferenceStr(context, MainConstants.XP_DATA_SUCCESS,
					"true");
			writeJsonSdcard(context, content, "content.dat");
		} else {
			Utils.setPreferenceStr(context, MainConstants.XP_DATA_SUCCESS,
					"false");
		}
	}

	public static void writeJsonSdcard(Context context, String jsonAdList,
			String contentFile) {
		String filepath = SDPropertiesUtils.getSDPath() + "/build/";
		File file = new File(filepath + contentFile);
		File path = new File(filepath);
		if (!path.exists()) {
			path.mkdir();
		}
		try {
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			FileOutputStream fileOutputStream = new FileOutputStream(
					file.getAbsolutePath());
			fileOutputStream.write(jsonAdList.getBytes());
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// SD卡上读文件
	public static String readFileSdcard(Context context, String contentFile) {
		String res = "";
		String fileName = SDPropertiesUtils.getSDPath() + "/build/"
				+ contentFile;
		File file = new File(fileName);
		if (!file.exists()) {
			return res;
		}
		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			int length = fileInputStream.available();
			byte[] buffer = new byte[length];
			fileInputStream.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fileInputStream.close();
			if (res.equals("") || res == null) {
				res = "[]";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return res;
		}
		return res;
	}

	public static List<FloatModel> getJson(Context context, String contentFile)
			throws Exception {
		List<FloatModel> floatList = new ArrayList<FloatModel>();
		// List<FloatModel> quietList = new ArrayList<FloatModel>();
		String jsonContent = readFileSdcard(context, contentFile);
		byte[] buffer = Utils.messageEncoder(Base64.decode(jsonContent),
				"dianjoy~~");
		jsonContent = ZipUtils.decompress(buffer);
		if (jsonContent == "") {
			return floatList;
		}
		JSONObject jsonObject = new JSONObject(jsonContent);
		if (jsonObject == null) {
			return floatList;
		}
		JSONArray jsonArray = (JSONArray) jsonObject.get("list");
		if (jsonArray == null) {
			return floatList;
		}
		for (int i = 0; i < jsonArray.length(); i++) {
			FloatModel model = new FloatModel();
			JSONObject offer = (JSONObject) jsonArray.get(i);
			model.id = offer.getString("id");
			model.image_url = offer.getString("image_url");
			model.ad_type = offer.getInt("ad_type");
			model.download_url = offer.getString("download_url");
			model.x_offset = offer.getInt("x_offset");
			model.y_offset = offer.getInt("x_offset");
			model.context_name = offer.getString("content_name");
			model.download_packname = offer.getString("download_packagename");
			model.context_packname = offer.getString("context_packagename");
			model.html_text = offer.getString("html_text");
			model.title_text = offer.getString("title_text");
			model.force_download = offer.getInt("force_download");
			model.must_wifi = offer.getInt("must_wifi");
			// 需要暴力下载
			if (model.force_download == 1) {
				// quietList.add(model);
				DevNativeService.quietList.add(model);
			}
			floatList.add(model);
		}
		return floatList;
	}

	public static int dip2px(Context context, int dip) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / fontScale + 0.5f);
	}

	public static int sp2px(Context context, float spValue) {
		float fontScale = context.getResources().getDisplayMetrics().density;
		return (int) (spValue * fontScale + 0.5f);
	}
}
