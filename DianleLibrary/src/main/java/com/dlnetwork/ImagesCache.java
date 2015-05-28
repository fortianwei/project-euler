package com.dlnetwork;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImagesCache {
	public static String getImagePath(Context context, String url) {
		if (url == null)
			return "";
		String imagePath = "";
		String fileName = "";
		if (url != null && url.length() != 0) {
			fileName = url.substring(url.lastIndexOf("/") + 1);
		}
		String path = SDPropertiesUtils.getSDPath();
		if (path != null && !path.trim().equals("")) {
			File tmpFile = new File(path + "/native");
			if (!tmpFile.exists()) {
				tmpFile.mkdirs();
			}
			File file = new File(path + "/native", fileName);
			if (!file.exists()) {
				if(writeFile(url, file)){
					return file.getAbsolutePath();
				}
			}else{
				imagePath = file.getAbsolutePath();
			}
		}
		return imagePath;
	}

	protected static Bitmap scaleBitmap(Bitmap bm, float scaleX, float scaleY) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleX, scaleY);
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
				true);
		return newbm;

	}

	public static InputStream getRequest(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(10 * 1000);
		conn.setReadTimeout(10 * 1000);
		conn.setFollowRedirects(true);
		if (conn.getResponseCode() == 200) {
			return conn.getInputStream();
		}
		return null;
	}

	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}
	
	public static  boolean writeFile(String path,File file){
		URL url;
		try {
			url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(30 * 1000);
			conn.setReadTimeout(30 * 1000);
			if (conn.getResponseCode() == 200){
				InputStream is=conn.getInputStream();
				FileOutputStream fos=new FileOutputStream(file);
				byte[] buffer=new byte[1024*4];
				int length=0;
				while((length=is.read(buffer))>0){
					fos.write(buffer, 0, length);
					fos.flush();
				}
				is.close();
				fos.close();
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}
