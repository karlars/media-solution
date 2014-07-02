package com.oopstudio.media.rtsp;

import java.io.IOException;
import java.io.OutputStream;

public class RtspResponse {
	// Status code definitions
	public static final String STATUS_OK = "200 OK";
	public static final String STATUS_BAD_REQUEST = "400 Bad Request";
	public static final String STATUS_NOT_FOUND = "404 Not Found";
	public static final String STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";

	public String status = STATUS_INTERNAL_SERVER_ERROR;
	public String content = "";
	public String attributes = "";
	private final RtspRequest request;

	public RtspResponse(RtspRequest request) {
		this.request = request;
	}

	public RtspResponse() {
		// Be carefull if you modify the send() method because request might be
		// null !
		request = null;
	}

	public void send(OutputStream output) throws IOException {
		int seqid = -1;

		try {
			seqid = Integer.parseInt(request.headers.get("cseq").replace(" ",
					""));
		} catch (Exception e) {
		}

		String response = "RTSP/1.0 " + status + "\r\n"
				+ "Server : DevO RTSP Server\r\n"
				+ (seqid >= 0 ? ("Cseq: " + seqid + "\r\n") : "")
				+ "Content-Length: " + content.length() + "\r\n" + attributes
				+ "\r\n" + content + "\r\n\r\n";

		output.write(response.getBytes());
	}
}
