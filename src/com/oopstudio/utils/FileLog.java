package com.oopstudio.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

public class FileLog {

	public static final String LOG_FILE_PATH = "./"; //"/usr/local/rtspserver/logs/";
	public static final long MIN_FREE_SPACE = 50 * 1024 * 1024 * 1024; // 50GB
	
	static final private FileLog pthis = new FileLog();
	private BufferedWriter bw = null;
	private String filename = "";

	synchronized public static void writeLog(String tag, String sLog) {
		pthis.checkLogFile(tag);
		pthis.write(sLog);
	}

	synchronized public static void closeLog() {
		pthis.close();
	}

	private void checkLogFile(String tag) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");

		// Make directory
		StringBuilder path = new StringBuilder();
		path.append(LOG_FILE_PATH);
		if(tag != null) path.append(tag + "/");

		File dir = new File(path.toString());
		if(!dir.exists()) {
			dir.mkdir();
		}

		// Make file name
		StringBuilder filename = new StringBuilder();
		filename.append(df.format(new Date()));
		//if(tag != null) filename.append("_"+tag);
		filename.append(".log");

		if (!filename.equals(this.filename)) {
			close();
			open(path.toString(), filename.toString());
		}
	}

	private void open(String path, String filename) {
		try {
			//String path = ServerConfig.LOG_FILE_PATH;
			freespaceCheck(path);
			bw = new BufferedWriter(new FileWriter(path + filename, true));
			this.filename = filename;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {
		if (bw != null) {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bw = null;
		}
	}

	private void write(String sLog) {
		if (bw != null) {
			try {
				bw.write(sLog);
				bw.newLine();
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void freespaceCheck(String dirpath) {
		final File dir = new File(dirpath);
		long freeSpace = dir.getFreeSpace();

		if (freeSpace < MIN_FREE_SPACE) {
			long needSpace = MIN_FREE_SPACE - freeSpace;

			File[] files = dir.listFiles();
			Vector<File> list = new Vector<File>(files.length);
			for (int i = 0; i < files.length; i++) {
				list.add(files[i]);
			}

			Comparator<File> comparator = new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return (o1.lastModified() < o2.lastModified()) ? -1 : (o1.lastModified() > o2.lastModified()) ? 1 : 0;
				}
			};

			// 오래된 파일 -> 최근 파일 순으로 정렬
			Collections.sort(list, comparator);

			long sizeOfFiles = 0;
			for (File file : list) {
				if (sizeOfFiles >= needSpace) {
					break;
				}
				if (file.getName().toLowerCase().endsWith(".log")) {
					sizeOfFiles += file.length();
					Log.d("delete file: " + file.getPath());
					try {
						file.delete();
					} catch (Exception e) {
						Log.d("error occur by delete file.  " + file.getPath());
					}
				}
			}
		}
	}
}