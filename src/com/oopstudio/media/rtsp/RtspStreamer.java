package com.oopstudio.media.rtsp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import com.oopstudio.utils.Log;


public class RtspStreamer {
	
	private DatagramSocket rtpSocket;
	private DatagramSocket rtcpSocket;
	private Socket client;
	
	private int sendIdx;
	private int rtpClientPort;
	private int rtcpClientPort;
	private int rtpServerPort;
	private int rtcpServerPort;
	private int sequenceNumber;
	
	private long timeStamp;
	private boolean tcpTransport;
	
	private byte [] buffer = new byte[65507];

	public RtspStreamer(Socket client) {
		this.client = client;
		
		rtpServerPort = 0;
		rtcpServerPort = 0;
		rtpClientPort = 0;
		rtcpClientPort = 0;
		
		sequenceNumber = 0;
		timeStamp = 0;		
		sendIdx = 0;
		tcpTransport = false;
	}
	
	public int getRtpServerPort() {
		return rtpServerPort;
	}
	
	public int getRtcpServerPort() {
		return rtcpServerPort;
	}
	
	public void initTransport(int rtpPort, int rtcpPort, boolean TCP) {
		
		rtpClientPort = rtpPort;
		rtcpClientPort = rtcpPort;
		tcpTransport = TCP;
		
		if ( !tcpTransport ) {

			for ( int p = 6970; p < 0xFFFE; p += 2) {
				
				try {
					
					rtpSocket = new DatagramSocket( p );
					rtcpSocket = new DatagramSocket( p + 1 );
					
					if ( rtpSocket != null && rtcpSocket != null) {
						rtpServerPort = p;
						rtcpServerPort = p + 1;
						break;
					}else {
						rtpSocket.close();
						rtcpSocket.close();
					}
					
				}catch ( SocketException e ) {
					e.printStackTrace();
				}
				
			}
			
		}		
		
	}
	
	public void streamImage(int streamID) {
		
//		System.out.println("[0] sendIdx : " + sendIdx);
		
//		byte [][] samples1 = { JPEGSamples.JpegScanDataCh1A, JPEGSamples.JpegScanDataCh1B };
//		char [][] samples2 = { JPEGSamples.JpegScanDataCh2A, JPEGSamples.JpegScanDataCh2B };
		
		byte [] jpegScanData = null;
		
		switch ( sendIdx ) {
			
		case 0:
			jpegScanData = RtspConst.JpegScanDataCh1A;
			break;
			
		case 1: 
			jpegScanData = RtspConst.JpegScanDataCh1B;
			//jpegScanData = samples2[0];
			break;
		
		}
		
		sendIdx++;
		if ( sendIdx > 1 ) sendIdx = 0;
		SendRtpPacket(jpegScanData, jpegScanData.length, streamID);
		
	}
	
	
	public void SendRtpPacket(byte [] jpegScanData, int JpegLen, int Chn)
	{
		int KRtpHeaderSize = 12;           // size of the RTP header
		int KJpegHeaderSize = 8;           // size of the special JPEG payload header

	    byte        RtpBuf[] = new byte[2048];
	    int         RtpPacketSize = JpegLen + KRtpHeaderSize + KJpegHeaderSize;
	    
	    // Prepare the first 4 byte of the packet. This is the Rtp over Rtsp header in case of TCP based transport
	    RtpBuf[0]  = '$';        // magic number
	    RtpBuf[1]  = 0;          // number of multiplexed subchannel on RTPS connection - here the RTP channel
	    RtpBuf[2]  = (byte) ((RtpPacketSize & 0x0000FF00) >> 8);
	    RtpBuf[3]  = (byte) (RtpPacketSize & 0x000000FF);
	    // Prepare the 12 byte RTP header
	    RtpBuf[4]  = (byte) 0x80;                               // RTP version
	    RtpBuf[5]  = (byte) 0x9a;                               // JPEG payload (26) and marker bit
	    RtpBuf[7]  = (byte) (sequenceNumber & 0x0FF);           // each packet is counted with a sequence counter
	    RtpBuf[6]  = (byte) (sequenceNumber >> 8);
	    RtpBuf[8]  = (byte) ((timeStamp & 0xFF000000) >> 24);   // each image gets a timestamp
	    RtpBuf[9]  = (byte) ((timeStamp & 0x00FF0000) >> 16);
	    RtpBuf[10] = (byte) ((timeStamp & 0x0000FF00) >> 8);
	    RtpBuf[11] = (byte) (timeStamp & 0x000000FF);
	    RtpBuf[12] = 0x13;                               // 4 byte SSRC (sychronization source identifier)
	    RtpBuf[13] = (byte) 0xf9;                               // we just an arbitrary number here to keep it simple
	    RtpBuf[14] = 0x7e;
	    RtpBuf[15] = 0x67;
	    // Prepare the 8 byte payload JPEG header
	    RtpBuf[16] = 0x00;                               // type specific
	    RtpBuf[17] = 0x00;                               // 3 byte fragmentation offset for fragmented images
	    RtpBuf[18] = 0x00;
	    RtpBuf[19] = 0x00;
	    RtpBuf[20] = 0x01;                               // type
	    RtpBuf[21] = 0x5e;                               // quality scale factor
	    if (Chn == 0)
	    {
	        RtpBuf[22] = 0x06;                           // width  / 8 -> 48 pixel
	        RtpBuf[23] = 0x04;                           // height / 8 -> 32 pixel
	    }
	    else
	    {
	        RtpBuf[22] = 0x08;                           // width  / 8 -> 64 pixel
	        RtpBuf[23] = 0x06;                           // height / 8 -> 48 pixel
	    };
	    // append the JPEG scan data to the RTP buffer
	    System.arraycopy(jpegScanData, 0, RtpBuf, 24, JpegLen);
	    
	    sequenceNumber++;                              // prepare the packet counter for the next packet
	    timeStamp += 3600;                             // fixed timestamp increment for a frame rate of 25fps

	    if (tcpTransport) // RTP over RTSP - we send the buffer + 4 byte additional header
	    {
	    	try {
				client.getOutputStream().write(RtpBuf, 0, RtpBuf.length + 4);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    else                // UDP - we send just the buffer by skipping the 4 byte RTP over RTSP header
	    {
	    	byte [] temp = new byte [RtpBuf.length-4];
	    	System.arraycopy(RtpBuf, 4, temp, 0, temp.length);
	    	DatagramPacket dp = new DatagramPacket(temp, temp.length, client.getInetAddress(), rtpClientPort);
	    	try {
				rtpSocket.send(dp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	}
	
}
