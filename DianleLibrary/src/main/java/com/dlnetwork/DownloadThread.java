package com.dlnetwork;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread  extends Thread {
	private File saveFile;
	private URL downUrl;
	private int block;
	
	/* 下载开始位置  */
	private int threadId = -1;	
	private int downLength;
	private boolean finish = false;
	private DownloadHelper downloader;
	HttpURLConnection http;
	/**
	 * @param downloader:执行下载操作的线程
	 * @param downUrl:下载地址
	 * @param saveFile:下载路径
	 * 
	 */
	public DownloadThread(DownloadHelper downloader, URL downUrl, File saveFile, int block, int downLength, int threadId) {
		this.downUrl = downUrl;
		this.saveFile = saveFile;
		this.block = block;
		this.downloader = downloader;
		this.threadId = threadId;
		this.downLength = downLength;
	}
	
	@Override
	public void run() {
		if(downLength < block){//未下载完成
			try {
				int responseCode=0;
			    http = (HttpURLConnection) downUrl.openConnection();
				http.setConnectTimeout(30* 1000);
				http.setReadTimeout(30* 1000);
				http.setRequestMethod("GET");
				http.setRequestProperty("Accept-Encoding", "identity");
				http.setFollowRedirects(true);
				int startPos = block * (threadId - 1) + downLength;//开始位置
				int endPos = block * threadId -1;//结束位置
				http.setRequestProperty("Range", "bytes=" + startPos + "-"+ endPos);//设置获取实体数据的范围
				responseCode=http.getResponseCode();
				if(responseCode>=400){
					this.downLength = 1;
					return ;
				}
				InputStream inStream = http.getInputStream();
				byte[] buffer = new byte[1024*4];
				int offset = 0;
				RandomAccessFile threadfile = new RandomAccessFile(this.saveFile, "rwd");
				threadfile.seek(startPos);
				DownloadThreadInfo downloadSize=new DownloadThreadInfo();
				while ((offset = inStream.read(buffer, 0, 1024*4)) != -1) {
					threadfile.write(buffer, 0, offset);
					downLength += offset;
					downloadSize.downSize=downLength;
					downloadSize.thradId=this.threadId;
					downloader.append(offset);
					downloader.update(this.threadId, downLength,downloadSize);
				}
				threadfile.close();
				inStream.close();
				this.finish = true;
			} catch (Exception e) {
				//e.printStackTrace();
				this.downLength = -1;
			}
		}
	}
	
	/**
	 * 下载是否完成
	 * @return
	 */
	public boolean isFinish() {
		return finish;
	}
	
	/**
	 * 已经下载的内容大小
	 * @return 如果返回值为-1,代表下载失败
	 */
	public long getDownLength() {
		return downLength;
	}
	public int threadSize(){
		return downLength;
	}
}

