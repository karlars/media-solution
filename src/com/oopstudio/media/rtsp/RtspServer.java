package com.oopstudio.media.rtsp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.oopstudio.utils.Log;

public class RtspServer extends Thread {
	
	public static int SERVER_PORT = 8554;
	public static int SERVER_TIMEOUT = 3000;
	
	public RtspServer () {
		
		// TODO Auto-generated method stub
		ServerSocket listenSocket = null;
		Socket connectionSocket = null;
		
		try {
			listenSocket = new ServerSocket(RtspConst.RTSP_BASIC_PORT);
//			listenSocket.setSoTimeout(SERVER_TIMEOUT);
		} catch ( IOException e ) {
			Log.d("Can't bind port: " + SERVER_PORT);
			e.printStackTrace();
			System.exit(1);
		}
		
		Log.d("MJPEG Server Socket Initialization successful");
		Log.d("MJPEG Server started successfully...");
		
		while(true) {
			
			try {
				if(listenSocket.isClosed()) listenSocket.close();
				
				connectionSocket = listenSocket.accept();
				Log.d("New client connected...");
				
				Log.d("Server connection from{/" + connectionSocket.getInetAddress() + ":" + connectionSocket.getPort () + "} " +
						"to{/" + connectionSocket.getLocalAddress() + ":" + connectionSocket.getLocalPort() + "}");
				
				RtspServerThread serverThread = new RtspServerThread(connectionSocket);
				serverThread.start();
				
			} catch ( IOException e) {
				Log.d("Can't start server thread!");
				e.printStackTrace();
				
				try {
					if ( connectionSocket != null ) connectionSocket.close();
					Log.d("MJPEG Server closed ...");
				} catch (IOException i) {
					Log.d("Client closed fail..");
					e.printStackTrace();
				}
				
			}
			
		}
	}
	
}
