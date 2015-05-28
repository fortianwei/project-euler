package com.dlnetwork;

public class FloatModel {
	// 广告ID
	public String id;
	// 浮窗类型 0浮窗 1弹窗 2 HTML类型弹窗
	public Integer ad_type;
	// 上下文程序的包名
	public String context_packname;
	// x偏移百分比（百分比，整数）
	public Integer x_offset;
	// y偏移百分比（百分比，整数）
	public Integer y_offset;
	// 上下文程序名
	public String context_name;
	// 推广程序包名
	public String download_packname;
	// 推广程序下载地址
	public String download_url;
	// 一次性弹窗的标题文字
	public String title_text;
	// 广告语
	public String html_text;
	// 图片地址
	public String image_url;
	// 暴力下载
	public Integer force_download;
	//什么网络触发下载   0 非wifi    1是wifi
	public Integer must_wifi;
}
