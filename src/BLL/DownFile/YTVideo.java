package BLL.DownFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class YTVideo {
	private DownloadTask[] t;
	private String FileName;
	private int completedFile;
	public YTVideo(DownloadTask[] t, String FileName, int completedFile) {
		this.t = t;
		this.FileName = FileName;
		this.completedFile = completedFile;
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
	public int getCompletedFile() {
		return completedFile;
	}
	public void setCompletedFile(int completedFile) {
		this.completedFile = completedFile;
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

}
