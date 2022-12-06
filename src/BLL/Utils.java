package BLL;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

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
	
	public static long getFileLength(String fileUrl) {
		try {
			URI uri = new URI(fileUrl);
			URL url = uri.toURL();
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("Accept-Encoding", "identity");
			return connection.getContentLengthLong();
			
		}catch(IOException | URISyntaxException e) {
			return -1;
		}
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
	
	public static String getFileName(String uri) {
		try {
			if (uri == null)
				return "FILE";
			if (uri.equals("/") || uri.length() < 1) {
				return "FILE";
			}
			int x = uri.lastIndexOf("/");
			String path = uri;
			if (x > -1) {
				path = uri.substring(x);
			}
			int f = path.indexOf("file=");
			if(f != -1) {
				path = path.substring(f+5);
			}
			int qindex = path.indexOf("?");
			if (qindex > -1) {
				path = path.substring(0, qindex);
			}
			path = decodeFileName(path);
			if (path.length() < 1)
				return "FILE";
			if (path.equals("/"))
				return "FILE";
			File test = new File(path);
			test.createNewFile();
			test.delete();
			return path;
		} catch (Exception e) {
			return "FILE";
		}
	}

	public static String decodeFileName(String encoded) {
		String str = "";
		try {
			str = URLDecoder.decode(encoded.replace("+", "%2B"), "UTF-8");
		} catch (Exception e) {

		}
		StringBuilder builder = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (c == '/' || c == '\\' || c == '"' || c == '?' || c == '%'
					|| c == '*' || c == '<' || c == '>'
					|| c == ':' || c == '|')
				continue;
			builder.append(c);
		}
		return builder.toString();
	}
}
