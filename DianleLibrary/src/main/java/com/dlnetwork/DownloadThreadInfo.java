package com.dlnetwork;

import java.util.HashMap;

/*
 * 下载线程的信息,记录当前线程下载的位置，用户service被杀死后的断点续传
 */
public class DownloadThreadInfo {
	private HashMap<String, String> attributes = new HashMap<String, String>();
	public String packageName;
	public int thradId;
	public int downSize;
 
	String getPackageName() {
		return attributes.get("pack");
	}

	int getThradId() {
		return Integer.parseInt(attributes.get("threadId"));
	}

	int getDownSize() {
		return Integer.parseInt(attributes.get("downsize"));
	}

	@Override
	public String toString() {
		return Utils.mapToString(attributes);
	}
	void addAttribute(String key, String val) {
		if (val == null)
			val = "";
		attributes.put(key, val);
	}
	static DownloadThreadInfo parse(String s) {
		DownloadThreadInfo threadInfo = new DownloadThreadInfo();
		threadInfo.attributes = Utils.stringToMap(s);
		return threadInfo;
	}
}
