package com.oopstudio.app;

import com.oopstudio.media.rtsp.RtspServer;
import com.oopstudio.utils.Log;

public class AppMain {
	public static final String SERVER_VERSION = "v0.1";
	public static final String SERVER_COMPILE_DATE = "2014.07.02";
	
	public static void main(String args[]) {
		
		// TODO Auto-generated method stub
		Log.d("Starting server daemon");
		Log.d("======================================================================");
		Log.d(" MJPEG Rtsp Server TCP Daemon starting...");
		Log.d(" Version : " + SERVER_VERSION);
		Log.d(" Compiled at " + SERVER_COMPILE_DATE);
		Log.d(" Author : Seungku Yu(yuseungku@gmail.com)");
		Log.d("======================================================================");
		Log.d("MJPEG Server init...");
		
		RtspServer rtspserver = new RtspServer();
		rtspserver.start();
		
	}
	
}
