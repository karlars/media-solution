package com.oopstudio.media.rtsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import com.oopstudio.utils.Log;

public class RtspSession {
	private String TAG = "[OPS]";
	
	private int rtspSessionID;
	private int streamID;
	private int clientRTPPort;
	private int clientRTCPPort;
	
	private boolean tcpTransport;
	
	private Socket rtspClient;
	private OutputStream output;
	private BufferedReader input;
	private RtspStreamer streamer;
	
	private RTSP_CMD_TYPES rtspCmdType;
	private String urlPreSuffix;
	private String urlSuffix;
	private String urlHostPort;
	private String cSeq;
	
	private int contentLength;
	
	public enum RTSP_CMD_TYPES
    {
        RTSP_OPTIONS,
        RTSP_DESCRIBE,
        RTSP_SETUP,
        RTSP_PLAY,
        RTSP_TEARDOWN,
        RTSP_PAUSE,
        RTSP_UNKNOWN
    };
    
    public RtspSession(Socket rtspClient, RtspStreamer streamer) {
    	this.rtspClient = rtspClient;
    	this.streamer = streamer;
    	
    	try {
			this.input = new BufferedReader(new InputStreamReader(rtspClient.getInputStream()));
			this.output = rtspClient.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	init();
    	
    	rtspSessionID |= new Random().nextInt();
    	rtspSessionID |= 0x80000000;
    	if(rtspSessionID < 0) rtspSessionID *= -1;
    	streamID = -1;
    	clientRTPPort = 0;
    	clientRTCPPort = 0;
    	tcpTransport = false;
    }
    
    public void init() {
    	rtspCmdType = RTSP_CMD_TYPES.RTSP_UNKNOWN;
    	urlPreSuffix = null;
    	urlSuffix = null;
    	cSeq = null;
    	urlHostPort = null;
    	contentLength = 0 ;
    }
    
    public int getStreamID() {
    	return streamID;
    }
    
    public RTSP_CMD_TYPES rtspRequest() {
    	
    	String conntent = "RTSP/1.0 500 Internal Server Error\r\n\r\n";
    	
    	Log.d("===========================================>");
    	RtspRequest rtspRequest = null;
    	try {
			rtspRequest = RtspRequest.parseRequest(input);
			Log.d("RTSP Request : " + rtspRequest.headers.toString());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//RtspResponse response = new RtspResponse(rtspRequest);
    	if(rtspRequest == null) {
    		rtspCmdType = RTSP_CMD_TYPES.RTSP_UNKNOWN;
    		return rtspCmdType;
    	}
    	
    	int seqid = -1;

		try {
			seqid = Integer.parseInt(rtspRequest.headers.get("cseq").replace(" ",""));
		} catch (Exception e) {
		}
    	
    	if (rtspRequest.method.toUpperCase().equals("DESCRIBE")) {
    		Log.d("DESCRIBE");
    		rtspCmdType = RTSP_CMD_TYPES.RTSP_DESCRIBE;
    		
    		streamID = 1;
    		
    		String sdpbuf = "v=0\r\n"
    		        + "o=- 1 1 rtsp://127.0.0.1:8554/mjpeg/1\r\n"           
    		        + "s=SDP Seminar\r\n"
    		        + "t=0 0\r\n"                                            // start / stop - 0 -> unbounded and permanent session
    		        + "m=video 0 RTP/AVP 26\r\n"                             // currently we just handle UDP sessions
    		        + "c=IN IP4 0.0.0.0\r\n";
    		
    		conntent = "RTSP/1.0 200 OK\r\n"
					+ (seqid >= 0 ? ("Cseq: " + seqid + "\r\n") : "")
					+ "Content-Type: application/sdp\r\n"
					+ "Content-Base: rtsp://127.0.0.1:8554/mjpeg/1\r\n"
					+ "Content-Length: " + sdpbuf.length() + "\r\n\r\n"
					+ sdpbuf;

		}else if (rtspRequest.method.toUpperCase().equals("OPTIONS")) {
			Log.d("OPTIONS");
			rtspCmdType = RTSP_CMD_TYPES.RTSP_OPTIONS;
			
			conntent = "RTSP/1.0 200 OK\r\n"
					+ (seqid >= 0 ? ("Cseq: " + seqid + "\r\n") : "")
					+ "Public: DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE\r\n\r\n";
		}else if (rtspRequest.method.toUpperCase().equals("SETUP")) {
			Log.d("SETUP");
			rtspCmdType = RTSP_CMD_TYPES.RTSP_SETUP;
			
			if ( tcpTransport ) {
				conntent = "RTSP/1.0 200 OK\r\n"
						+ (seqid >= 0 ? ("Cseq: " + seqid + "\r\n") : "")
						+ "Transport: RTP/AVP/TCP;unicast;interleaved=0-1\r\n"
						+ "Session: " + rtspSessionID + "\r\n\r\n";
			}
			else {
				String transport = rtspRequest.headers.get("transport");
				String [] client_port = transport.split(";")[2].split("=")[1].split("-");
				
				streamer.initTransport(Integer.parseInt(client_port[0]), Integer.parseInt(client_port[1]), tcpTransport);
				
				conntent = "RTSP/1.0 200 OK\r\n"
						+ (seqid >= 0 ? ("Cseq: " + seqid + "\r\n") : "")
						+ "Transport: RTP/AVP;unicast;destination=127.0.0.1;source=127.0.0.1;client_port=" + client_port[0] + "-" + client_port[1] + ";server_port=" + streamer.getRtpServerPort() + "-" + streamer.getRtcpServerPort() + "\r\n"
						+ "Session: " + rtspSessionID + "\r\n\r\n";
				
			}
			
		}else if (rtspRequest.method.toUpperCase().equals("PLAY")) {
			Log.d("PLAY");
			rtspCmdType = RTSP_CMD_TYPES.RTSP_PLAY;
			
			conntent = "RTSP/1.0 200 OK\r\n"
					+ (seqid >= 0 ? ("Cseq: " + seqid + "\r\n") : "")
					+ "Range: npt=0.000-\r\n"
					+ "Session: " + rtspSessionID
					+ "RTP-Info: url=rtsp://127.0.0.1:8554/mjpeg/1/track1\r\n\r\n";
			
		}else if (rtspRequest.method.toUpperCase().equals("PAUSE")) {
			Log.d("PAUSE");
			rtspCmdType = RTSP_CMD_TYPES.RTSP_PAUSE;
		}else if (rtspRequest.method.toUpperCase().equals("TEARDOWN")) {
			Log.d("TEARDOWN");
			rtspCmdType = RTSP_CMD_TYPES.RTSP_TEARDOWN;
		}else {
			Log.d("RTSP_UNKNOWN");
			rtspCmdType = RTSP_CMD_TYPES.RTSP_UNKNOWN;
		}
    	
    	Log.d("Response : " + conntent);
    	Log.d("===========================================>\n");
    	
    	try {
			output.write(conntent.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return rtspCmdType;
    }
    
}
