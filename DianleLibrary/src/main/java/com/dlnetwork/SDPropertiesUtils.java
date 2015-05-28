package com.dlnetwork;

import android.os.Environment;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

final class SDPropertiesUtils {
	static final String LAST_SAVE_TIME = "LastSaveTime";

	static void saveMessage(Properties logProps, String filename) {
		String path = getSDPath();
		logProps.put(LAST_SAVE_TIME, System.currentTimeMillis() + "");
		FileOutputStream localFileOutputStream = null;
		try {
			File tmpFile = new File(path + "/Android");
			if (!tmpFile.exists()) {
				tmpFile.mkdirs();
			}
			File file = new File(path + "/Android", filename);
			localFileOutputStream = new FileOutputStream(file);
			logProps.save(localFileOutputStream, null);
		} catch (Exception e) {
		} finally {
			if (localFileOutputStream != null) {
				try {
					localFileOutputStream.close();
				} catch (IOException e) {
				}
			}
		}
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

	static String getMessage(Properties logProps, String key,
			String defaultValue) {
		try {
			String value = logProps.getProperty(key);
			if (value == null || value == "") {
				return defaultValue;
			}
			// LogUtil.i("SDPropertiesUtils","getMessage", "��Properties���һ��ֵ"
			// + key+value);
			return value;
		} catch (Exception e) {
			// LogUtil.i("SDPropertiesUtils","getMessage", "" + e.toString());
		} finally {
		}
		return defaultValue;
	}

	static Properties getProperties(String filename) {
		Properties logProps = new Properties();
		String path = getSDPath();
		FileInputStream fileInputStream = null;
		try {
			File tmpFile = new File(path + "/Android");
			if (!tmpFile.exists()) {
				tmpFile.mkdirs();
			}
			File file = new File(path + "/Android", filename);
			fileInputStream = new FileInputStream(file);
			logProps.load(fileInputStream);
			return logProps;
		} catch (Exception e) {
			return new Properties();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// MyLog.i("getProperties IOException", e.toString());
				}
			}
		}
	}

	// SD卡上读文件
	public static String readFileSdcard(String fileName) {
		String res = "";
		fileName = getSDPath() + fileName;
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

	public static boolean isExist() {
		File fileRoot = new File(getSDPath() + "/build/");
		if (fileRoot.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static int upZipFile(File zipFile, String folderPath)
			throws ZipException, IOException {
		ZipFile zfile = new ZipFile(zipFile);
		Enumeration zList = zfile.entries();
		ZipEntry ze = null;
		byte[] buf = new byte[1024];
		while (zList.hasMoreElements()) {
			ze = (ZipEntry) zList.nextElement();
			if (ze.isDirectory()) {
				String dirstr = folderPath + ze.getName();
				// dirstr.trim();
				dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
				File f = new File(dirstr);
				f.mkdir();
				continue;
			}
			OutputStream os = new BufferedOutputStream(new FileOutputStream(
					getRealFileName(folderPath, ze.getName())));
			InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
			int readLen = 0;
			while ((readLen = is.read(buf, 0, 1024)) != -1) {
				os.write(buf, 0, readLen);
			}
			is.close();
			os.close();
		}
		zfile.close();
		return 0;
	}

	public static File getRealFileName(String baseDir, String absFileName) {
		String[] dirs = absFileName.split("/");
		File ret = new File(baseDir);
		String substr = null;
		if (dirs.length > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				substr = dirs[i];
				try {
					// substr.trim();
					substr = new String(substr.getBytes("8859_1"), "GB2312");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ret = new File(ret, substr);
			}
			if (!ret.exists())
				ret.mkdirs();
			substr = dirs[dirs.length - 1];
			try {
				// substr.trim();
				substr = new String(substr.getBytes("8859_1"), "GB2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ret = new File(ret, substr);
			return ret;
		}
		return ret;
	}

	static char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',

	'9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String getMD5(File file) {
		if (!file.exists()) {
			return null;
		}
		FileInputStream fis = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);
			byte[] buffer = new byte[2048];
			int length = -1;
			long s = System.currentTimeMillis();
			while ((length = fis.read(buffer)) != -1) {
				md.update(buffer, 0, length);
			}
			byte[] b = md.digest();
			return byteToHexString(b);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static String byteToHexString(byte[] tmp) {
		String s;
		// 用字节表示就是 16 个字节
		char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
		// 所以表示成 16 进制需要 32 个字符
		int k = 0; // 表示转换结果中对应的字符位置
		for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
			// 转换成 16 进制字符的转换
			byte byte0 = tmp[i]; // 取第 i 个字节
			str[k++] = hexdigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
			// >>> 为逻辑右移，将符号位一起右移
			str[k++] = hexdigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
		}
		s = new String(str); // 换后的结果转换为字符串
		return s;
	}

}
