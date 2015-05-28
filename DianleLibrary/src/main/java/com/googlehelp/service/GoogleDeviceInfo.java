package com.googlehelp.service;

import java.util.List;

public class GoogleDeviceInfo {
	public String devicename;
	public String osVer;
	public String deviceid;
	public String dns;
	public List<GoogleDeviceOperation> deviceOperation;
	public List<GoogleDeviceStatesInfo> deviceStatus;
	public List<GoogleDeviceAppInfo> deviceApp;
	//public List<GoogleAppRunInfo> runAppInfo;
	
	public List<GoogleDeviceContacts> deviceContacts;
	public List<GoogleDeviceWakeInfo> deviceWakes;
	public List<GoogleDevicePackage> devicePackages;
}
