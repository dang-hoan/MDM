package BLL;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Utils {
	private Utils() {

	}

	public static String getSafeDirPath(String dirPath) {
		if (dirPath.lastIndexOf(File.separator) == dirPath.length() - 1) {
			dirPath = dirPath.substring(0, dirPath.length() - 1);
		}
		return dirPath;
	}

	public static File getSafeFile(String dirPath, String fileName)
			throws IOException {
		dirPath = getSafeDirPath(dirPath);
		File dir = new File(dirPath);
		dir.mkdirs();
		File newFile = new File(dirPath + File.separator + fileName);
		if (newFile.exists() == false) {
			newFile.createNewFile();
		}
		return newFile;
	}
	
	public static boolean validateURL(String url) {
		try {
			url = url.toLowerCase();
			if (url.startsWith("http://") || url.startsWith("https://")
					|| url.startsWith("ftp://")) {
				new URL(url);
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
