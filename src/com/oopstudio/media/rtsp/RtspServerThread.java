package com.oopstudio.media.rtsp;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import com.oopstudio.utils.Log;

public class RtspServerThread extends Thread {
	private String TAG = "[OPS]";
	
	private int DELAY = 1;
	
	private boolean isStreamingStart = false;
	private boolean threadLoop = true;
	
	private Timer threadTimer;
	private RtspStreamer streamer;
	private RtspSession rtspSession;
	
	private Socket client = null;
	
	public RtspServerThread(Socket client) {
		this.client = client;
		
		streamer = new RtspStreamer(client);
		rtspSession = new RtspSession(this.client, streamer);
	}
	
	public void timerStart() {
		threadTimer = new Timer();
		if(threadTimer == null) {
			Log.d("threadTimer is null..");
			return;
		}
		
		threadTimer.schedule(new TimerTask() {

			public void run() {
				// TODO Auto-generated method stub
				timerWork();
			}
			
		}, 0, DELAY);
		
	}
	
	public void timerStop() {
		if(threadTimer == null) {
			Log.d("threadTimer is null..");
			return;
		}
		
		threadTimer.cancel();
		threadTimer = null;
		
	}
	
	public void timerWork() {
		
		if ( isStreamingStart ) {
			Log.d("Start stream images ...");
			streamer.streamImage(rtspSession.getStreamID());
		}
		
	}

	public void run() {
		// TODO Auto-generated method stub
		
		while(threadLoop) {
			
			RtspSession.RTSP_CMD_TYPES type = rtspSession.rtspRequest();
			
			if ( type == RtspSession.RTSP_CMD_TYPES.RTSP_PLAY ) {
				isStreamingStart = true;
				timerStart();
			}else if ( type == RtspSession.RTSP_CMD_TYPES.RTSP_TEARDOWN ) {
				threadLoop = false;
				isStreamingStart = false;
				timerStop();
			}
			
		}
		
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
