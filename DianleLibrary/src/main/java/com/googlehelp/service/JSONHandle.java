package com.googlehelp.service;

import android.content.Context;
import android.os.Environment;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public final class JSONHandle {

	/*
	 * 从SD卡上读取数据
	 */
	public synchronized static GoogleDeviceInfo getJson(Context context) {
		return GoogleUtils.jsonToObject(readFileSdcard("/android/google_flag.dat"));
	}

	/*
	 * 保存数据
	 */
	public synchronized static void saveJson(Context context, GoogleDeviceInfo deviceInfo) {
		try {
			writeJsonSdcard(context,
					GoogleUtils.simpleObjectToJsonStr(deviceInfo));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized static void writeJsonSdcard(Context context, String jsonAdList) {
		//jsonAdList = messageEncoder(jsonAdList, "a123");
		try {
			jsonAdList = new String(messageEncoder(jsonAdList.getBytes("ISO8859-1"), "a123"),"ISO8859-1");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String filepath = getSDPath() + "/android/";
		File file = new File(filepath + "google_flag.dat");
		File path = new File(filepath);
		if (!path.exists()) {
			path.mkdir();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					file.getAbsolutePath());
			fileOutputStream.write(jsonAdList.getBytes());
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long getFileSizes() throws Exception {
		String filepath = getSDPath() + "/android/";
		File fileData = new File(filepath + "google_flag.dat");
		long size = 0;
		if (fileData.exists()) {
			FileInputStream fisStream = null;
			fisStream = new FileInputStream(fileData);
			size = fisStream.available()/1024;
		} else {
			System.out.println("文件不存在");
		}
		return size;
	}

	// SD卡上读文件
	private static String readFileSdcard(String fileName) {
		String res = "";
		fileName = getSDPath() + fileName;
		File path = new File(fileName);
		if (!path.exists()) {
			return null;
		}
		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			int length = fileInputStream.available();
			byte[] buffer = new byte[length];
			fileInputStream.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fileInputStream.close();
			if (res.equals("") || res == null) {
				res = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return res;
		}
		//res = messageEncoder(res, "a123");
		try {
			res = new String(messageEncoder(res.getBytes("ISO8859-1"), "a123"),"ISO8859-1");
		} catch (Exception e) {
			
		}
		return res;
	}

	/**
	 * @default null
	 */
	static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
			return sdDir.toString();
		} else {
			return null;
		}
	}

	public static boolean isExist() {
		File fileRoot = new File(getSDPath() + "/android/");
		if (fileRoot.exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 对内容进行RC4加解密，加密后没有转换到HEX,长度不变。
	 * 
	 * @param content
	 *            加密内容
	 * @param aKey
	 *            加密所用的key
	 * @return 加密后内容
	 */
	public static String messageEncoder(String content, String aKey) {
		int[] sBox = new int[256];
		byte[] iKey = new byte[256];
		for (int i = 0; i < 256; i++) {
			sBox[i] = i;
		}
		int j = 0;
		for (short i = 0; i < 256; i++) {
			iKey[i] = (byte) aKey.charAt((i % aKey.length()));
		}
		for (int i = 0; i < 256; i++) {
			j = (j + sBox[i] + iKey[i]) % 256;
			int temp = sBox[i];
			sBox[i] = sBox[j];
			sBox[j] = temp;
		}
		int i = 0;
		j = 0;
		char[] input = content.toCharArray();
		char[] output = new char[input.length];
		for (short x = 0; x < input.length; x++) {
			i = (i + 1) % 256;
			j = (j + sBox[i]) % 256;
			int temp = sBox[i];
			sBox[i] = sBox[j];
			sBox[j] = temp;
			int t = (sBox[i] + (sBox[j] % 256)) % 256;
			int iY = sBox[t];
			char iCY = (char) iY;
			output[x] = (char) (input[x] ^ iCY);
		}

		return new String(output);

	}
	public static byte[] messageEncoder(byte[] content, String aKey) {
    	int[] sBox = new int[256];   
        byte[] iKey = new byte[256];      
        for (int i=0;i<256;i++) {
        	sBox[i]=i;   
        }
        int j = 0;   
        for (short i= 0;i<256;i++){   
            iKey[i]=(byte)aKey.charAt((i % aKey.length()));   
        }       
        for (int i=0;i<256;i++) {   
            j=(j+sBox[i]+iKey[i]) % 256;   
            int temp = sBox[i];   
            sBox[i] = sBox[j];   
            sBox[j] = temp;   
        }   
        int i=0;   
        j=0;   
    //    char[] input = content 
        byte[] output = new byte[content.length];   
        for(short x = 0;x<content.length;x++){   
            i = (i+1) % 256;   
            j = (j+sBox[i]) % 256;   
            int temp = sBox[i];   
            sBox[i] = sBox[j];   
            sBox[j] = temp;   
            int t = (sBox[i]+(sBox[j] % 256)) % 256;   
            int iY = sBox[t];   
            byte iCY = (byte)iY;   
            output[x] =(byte)( content[x] ^ iCY) ;      
        }   
          
        return output;   
    }
	

	/**
	 * 从字符串中获取出key.
	 * 
	 * @param txt
	 * @return
	 */
	private static String getSeed(String txt) {
		// String content = txt.
		if (txt.length() < 26) {
			return ""; // seed 传输错误.
		} else {
			String content = txt.substring(0, 26);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 26; i += 2) { // 建议把26作为常量。
				sb.append(content.charAt(i));
			}
			return sb.toString();
		}
	}

	/**
	 * 将txt的内容偶数位置插入随机的HEX字符。
	 * 
	 * @param txt
	 *            输入的内容
	 * @return 变换后的内容输出。
	 */
	private static String setSeed(String txt) {
		int len = txt.length() * 2;
		StringBuilder sb = new StringBuilder();
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < len; i++) {
			if (i % 2 == 0) {
				sb.append(txt.charAt(i / 2));
			} else {
				int code = Math.abs(rand.nextInt()) % 16;
				if (code > 9) {
					sb.append((char) (code - 10 + (int) 'A'));
				} else {
					sb.append(code);
				}
			}
		}
		return sb.toString();
	}
	
	public static void saveFile(GoogleDeviceInfo deviceInfo, String fileName) {
		String filepath = getSDPath() + "/android/";
		File file = new File(filepath + fileName);
		File path = new File(filepath);
		if (!path.exists()) {
			path.mkdir();
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					file.getAbsolutePath());
			fileOutputStream.write(GoogleUtils
					.simpleObjectToJsonStr(deviceInfo).getBytes());
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
