package com.oopstudio.utils;

import java.util.Date;

public class Log {
	
	public static void d(String TAG, String msg) {
		final Thread current = Thread.currentThread();
		final long tid = current.getId();
		final StackTraceElement[] stack = current.getStackTrace();

		final int i = 2;
		final String prefix = "[" + tid + "] [" + stack[i].getClassName() + "." + stack[i].getMethodName() + " " + stack[i].getLineNumber() + "] ";

		StringBuilder log = new StringBuilder();
		log.append(new Date());
		log.append(" ");
		log.append(prefix);
		log.append(msg);

		System.out.println(log.toString());
		if ( TAG != null ) FileLog.writeLog(TAG, log.toString());
		else FileLog.writeLog(null, log.toString());
	}
	
	public static void d(String msg) {
		d(null, msg);
	}
	
}
