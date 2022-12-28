package BLL.DownFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import BLL.Values;
import BLL.VideoConversion.FFmpeg;

public class YTVideo {
	private DownloadTask[] t;
	private String FileName;
	private long downloadTime = 0;
	private int DownloadStatus = Values.READY; 
	
	public YTVideo(DownloadTask[] t, String FileName) {
		this.t = t;
		this.FileName = FileName;
	}
	public YTVideo(DownloadTask[] t, String FileName, long downloadTime, int DownloadStatus) {
		this.t = t;
		this.FileName = FileName;
		this.downloadTime = downloadTime;
		this.DownloadStatus = DownloadStatus;
	}
	public DownloadTask[] getT() {
		return t;
	}
	public void setT(DownloadTask[] t) {
		this.t = t;
	}
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	public int getDownloadStatus() {
		return DownloadStatus;
	}
	public void setDownloadStatus(int downloadStatus) {
		DownloadStatus = downloadStatus;
	}
	public void set_Status(int status) {
		DownloadStatus = status;
		t[0].set_Status(status);
		t[1].set_Status(status);
	}
	public long getDownloadTime() {
		 return (downloadTime == 0)? t[0].getDownloadTime() + t[1].getDownloadTime() : downloadTime;
	}
	public long getDownloadedSize() {
		return t[0].getDownloadedSize() + t[1].getDownloadedSize();
	}
	public long getFileSize() {
		if(DownloadStatus == FFmpeg.FF_SUCCESS) {
			File file = new File(t[0].getSaveDirectory() + File.separator +  FileName);
			if(file.exists()) return file.length();
		}
		return t[0].getFileSize() + t[1].getFileSize();
	}
	public long getCurrentSize() {
		return t[0].getCurrentSize() + t[1].getCurrentSize();
	}
	public long getDownloaded() {
		return t[0].getDownloaded() + t[1].getDownloaded();
	}

	public boolean checkFile(String type, String checksum) {
		try {
			List<MessageDigest> md = new ArrayList<>();
			switch(type) {
				case "All":{
					md.add(MessageDigest.getInstance("MD5"));
					md.add(MessageDigest.getInstance("SHA1"));
					md.add(MessageDigest.getInstance("SHA256"));
					if(getChecksum(md)) {
						for (MessageDigest element : md)
							if(checksum.equals(calcChecksum(element))) return true;
					}
					break;
				}
				case "MD5":{
					md.add(MessageDigest.getInstance("MD5"));
					if(getChecksum(md)) {
						if(checksum.equals(calcChecksum(md.get(0)))) return true;
					}
					break;
				}
				case "SHA-1":{
					md.add(MessageDigest.getInstance("SHA-1"));
					if(getChecksum(md)) {
						if(checksum.equals(calcChecksum(md.get(0)))) return true;
					}
					break;
				}
				case "SHA-256":{
					md.add(MessageDigest.getInstance("SHA-256"));
					if(getChecksum(md)) {
						if(checksum.equals(calcChecksum(md.get(0)))) return true;
					}
					break;
				}
			}
		}catch(NoSuchAlgorithmException e) {
			return false;
		}
		return false;
	}
	private boolean getChecksum(List<MessageDigest> md) {
		try {
			File saveF = new File(t[0].getSaveDirectory() + File.separator + FileName);
			if(!saveF.exists()) return false;

			BufferedInputStream is = new BufferedInputStream(new FileInputStream(saveF));
			int s; byte[] buf = new byte[1024*1024];
			while((s = is.read(buf, 0, buf.length)) != -1) {
				for (MessageDigest element : md)
					element.update(buf, 0, s);
			}
			is.close();
			return true;
		}catch(IOException e) {
			return false;
		}
	}

	public String calcChecksum(MessageDigest md) {
		String result = "";
	    byte[] b = md.digest();

	    for (byte element : b) {
	        result += Integer.toString( ( element & 0xff ) + 0x100, 16).substring( 1 );
	    }

	    return result;
	}
	
	public void merge() {
		long previousTimeLine = System.currentTimeMillis();
		DownloadStatus = Values.MERGING;
		File desF = new File(t[0].getSaveDirectory() + File.separator + FileName);
		if(desF.exists() && desF.length() != 0) {
			int index = FileName.lastIndexOf(".");
			String saveName = (index != -1)? FileName.substring(0, index):FileName;
			String FileType = (index != -1)? FileName.substring(index):".mkv";
			int i = 1;
			while(desF.exists()) {
				FileName = saveName + "(" + Integer.toString(i) + ")" + FileType;
				desF = new File(t[0].getSaveDirectory() + File.separator + FileName);
				i += 1;
			}
		}

		int result = DownloadManager.getInstance().mergeFile(t[0].getFilePath(), t[1].getFilePath(), t[0].getSaveDirectory() + File.separator + FileName);
		setDownloadStatus(result);

		t[0].cleanUp();
		t[1].cleanUp();
		downloadTime = t[0].getDownloadTime() + t[1].getDownloadTime() + System.currentTimeMillis() - previousTimeLine;
	}

}
