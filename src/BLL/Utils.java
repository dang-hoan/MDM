package BLL;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import BLL.Monitoring.HeaderCollection;
import BLL.Monitoring.HttpContext;

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
		if (!newFile.exists()) {
			newFile.createNewFile();
		}
		return newFile;
	}

	public static long getFileLength(String fileUrl) {
		try {
			URI uri = new URI(fileUrl);
			if(uri.isAbsolute()) {
				URL url = uri.toURL();
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("Accept-Encoding", "identity");
				return connection.getContentLengthLong();
			}
			else return -1;

		}catch(IOException | URISyntaxException e) {
			return -1;
		}
	}
	
	public static long getLengthSocket(String txtUrl) {
		try {
			URL fileUrl = new URL(txtUrl);
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(fileUrl.getHost(), (fileUrl.getPort() < 0)?fileUrl.getDefaultPort():fileUrl.getPort()));
//			Socket socket = createSocket(fileUrl.getHost(), (fileUrl.getPort() < 0)?fileUrl.getDefaultPort():fileUrl.getPort());
			OutputStream sockOut = socket.getOutputStream();
			BufferedReader sockIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String s = fileUrl.getPath();
			if (s == null || s.trim().length() < 1) {
				s = "/";
			}
			String s2 = fileUrl.getQuery();
			if (!(s2 == null || s2.trim().length() < 1)) {
				s += "?" + s2;
			}
			String reqLine = "GET /videoplayback?expire=1672066604&ei=zGGpY4q0EsTi7gPwh4XQAQ&ip=45.133.193.85&id=o-AKlfo07TOjqW3YV37qAdWoCw84aUmiaGpDEPP91utPyO&itag=18&source=youtube&requiressl=yes&spc=zIddbBjpqfuAOX9tGHslrpPhW-i11nc&vprv=1&mime=video%2Fmp4&ns=e2ZcgNQAyH7csxNisqA9lY8K&cnr=14&ratebypass=yes&dur=3656.097&lmt=1635835741043135&fexp=24001373,24007246&c=WEB&txp=6218224&n=O749K1KxWoi7Jg&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cmime%2Cns%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRQIgO7COy33dsWX2HNqkbh86YzJdYZurl4tq90NM5xTXExUCIQCavtbmugetXYSBQP_MLYXjghgord-C8rFI3seVvwZS7Q%3D%3D&rm=sn-45gpjx-3x4e7e,sn-npodz7z&req_id=230d1c87382f36e2&cm2rm=sn-5hnel67s&redirect_counter=3&cms_redirect=yes&cmsv=e&ipbypass=yes&mh=Yd&mip=183.80.212.88&mm=34&mn=sn-npoe7nsr&ms=ltu&mt=1672060613&mv=m&mvi=4&pl=24&lsparams=ipbypass,mh,mip,mm,mn,ms,mv,mvi,pl&lsig=AG3C_xAwRAIgX1Ez9jdcAcLH5yF3HPimEaM6Xvjm2HuCQkDgA0yFLQsCIH7THzzNgYtPP9QQIkuSbfjQWZYDQnYZybOkvp04XPnr HTTP/1.1\r\n"
					+ "Cookie: \r\n"
					+ "Range: bytes=0-\r\n"
					+ "host: rr4---sn-npoe7nsr.googlevideo.com";
//			String reqLine = "GET " + s + " HTTP/1.1";
//			reqLine = reqLine + "\r\n";
////			reqLine += "Range: bytes=0-\r\n";
//			reqLine += "host: " + fileUrl.getHost() + "\r\n";
//			StringBuffer reqBuf = new StringBuffer();
//			reqBuf.append(reqLine);
//			
//			System.out.println(reqLine);
//			sockOut.write(reqBuf.toString().getBytes());
			sockOut.write(reqLine.getBytes());
			sockOut.flush();
			
			String statusLine = re(socket.getInputStream());
			System.out.println("Status:" + statusLine);
			
			HeaderCollection responseHeaders = new HeaderCollection();
			responseHeaders.loadFromStream(socket.getInputStream());
			long length = Utils.getContentLength(responseHeaders);
			System.out.println(length);
//			int h = 1;
//			while(true) {
//				System.out.println(h++);
////				String ln = sockIn.readLine();
////				if (ln == null)
////					break;
//				String ln = Utils.readLine(socket.getInputStream());
//				System.out.println(ln);
//				if (ln.length() < 1)
//					break;
//				if(ln.contains("Content-Length")) {
//					int index = ln.indexOf(":");
//					String value = ln.substring(index + 1).trim();
//					if(index > 0) {
//						return Long.parseLong(value);
//					}
//					System.out.println("hello");
//					break;
//				}
//				if(ln.contains("content-range")) {
//					int index = ln.indexOf(":");
//					String value = ln.substring(index + 1).trim();
//					if(index > 0) {
//						String str = value.split(" ")[1];
//						str = str.split("/")[0];
//						String arr[] = str.split("-");
//						return Long.parseLong(arr[1]) - Long.parseLong(arr[0]) + 1;
//					}		
//					System.out.println("bye");
//					break;
//				}
//				
//			}
			return -1;
			
		} catch (IOException e) {
			return -1;
		}
	}
	
	private static Socket createSocket(String host, int port) {
		try {
			Socket socket = new Socket();
			socket.setTcpNoDelay(true);
			// socket.setReceiveBufferSize(tcpBufSize);
			socket.setSoLinger(false, 0);
			socket.connect(new InetSocketAddress(host, port));
			socket = wrapSSL(socket, host, port);
			return socket;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	
	public static SSLSocket wrapSSL(Socket socket, String host, int port) {
		try {
			SSLSocket sock2 = (SSLSocket) (HttpContext.getInstance().getSSLContext().getSocketFactory())
					.createSocket(socket, host, port, true);
			sock2.startHandshake();
			return sock2;
		} catch (IOException e) {
//			throw new NetworkException("Https connection failed: " + host + ":" + port);
		}
		return null;
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
	
	public static boolean isURLExist(String txtUrl) {
		try {
			URL url = new URL(txtUrl);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
//			huc.setRequestMethod("HEAD");
			int responseCode = huc.getResponseCode();
			 
			if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) return false;
			return true;
		}catch(IOException e) {
			return false;
		}
	}

	public static String getFileName(String uri) {
		try {
			if ((uri == null) || uri.equals("/") || uri.length() < 1) {
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

	public static String readLine(InputStream in) throws IOException {
		String result = "";
		while (true) {
			int x = in.read();
			if (x == -1)
				throw new IOException(
						"Unexpected EOF while reading header line");
			if (x == '\n')
				return result;
			if (x != '\r')
				result += (char) x;
		}
	}
	
	public static String re(InputStream in) throws IOException {
		StringBuffer buf = new StringBuffer();
		while (true) {
			int x = in.read();
			if (x == -1)
				throw new IOException(
						"Unexpected EOF while reading header line");
			if (x == '\n')
				return buf.toString();
			if (x != '\r')
				buf.append((char) x);
		}
	}

//	public static ParsedHookData parse(byte[] b) throws UnsupportedEncodingException {
//		ParsedHookData data = new ParsedHookData();
//		Map<String, String> cookies = new HashMap<>();
//		data.requestHeaders = new HeaderCollection();
//		data.responseHeaders = new HeaderCollection();
//		String strBuf = new String(b, "utf-8");
//		String[] arr = strBuf.split("\r\n");
//		for (int i = 0; i < arr.length; i++) {
//			String str = arr[i];
//			if (!str.contains("=")) {
//				continue;
//			}
//			String ln = str;
//			int index = ln.indexOf("=");
//			String key = ln.substring(0, index).trim().toLowerCase();
//			String val = ln.substring(index + 1).trim();
//			if (key.equals("url")) {
//				data.setUrl(val);
//			} else if (key.equals("file")) {
//				val = XDMUtils.getFileName(val);
//				data.setFile(val);
//			} else if (key.equals("req")) {
//				index = val.indexOf(":");
//				if (index > 0) {
//					String headerName = val.substring(0, index).trim().toLowerCase();
//					String headerValue = val.substring(index + 1).trim();
//					if (headerName.equals("range") && (!headerValue.startsWith("bytes=0-"))) {
//						data.setPartialResponse(true);
//					}
//					if (!isBlockedHeader(headerName)) {
//						data.requestHeaders.addHeader(headerName, headerValue);
//					}
//					if (headerName.equals("cookie")) {
//						parseCookies(headerValue, cookies);
//					}
//					System.out.println(ln);
//				}
//			} else if (key.equals("res")) {
//				index = val.indexOf(":");
//				if (index > 0) {
//					String headerName = val.substring(0, index).trim().toLowerCase();
//					String headerValue = val.substring(index + 1).trim();
//					data.responseHeaders.addHeader(headerName, headerValue);
//				}
//			} else if (key.equals("cookie")) {
//				index = val.indexOf(":");
//				if (index > 0) {
//					String cookieName = val.substring(0, index).trim();
//					String cookieValue = val.substring(index + 1).trim();
//					cookies.put(cookieName, cookieValue);
//					// System.out.println("********Adding cookie " + val);
//
//				}
//			}
//		}
//		if (data.responseHeaders.containsHeader("content-length")
//				|| data.responseHeaders.containsHeader("content-range")) {
//			data.contentLength = NetUtils.getContentLength(data.responseHeaders);
//		}
//		if (data.responseHeaders.containsHeader("content-type")) {
//			data.contentType = NetUtils.getCleanContentType(data.responseHeaders.getValue("content-type"));
//		}
//		if (!data.requestHeaders.containsHeader("user-agent")) {
//			if (data.responseHeaders.containsHeader("realua")) {
//				data.requestHeaders.addHeader("user-agent", data.responseHeaders.getValue("realua"));
//			}
//		}
//
//		for (String cookieKeys : cookies.keySet()) {
//			data.requestHeaders.addHeader("Cookie", cookieKeys + "=" + cookies.get(cookieKeys));
//		}
//
//		try {
//			data.setExt(XDMUtils.getExtension(XDMUtils.getFileName(data.getUrl())));
//		} catch (Exception e) {
//		}
//		return data;
//	}

	public static long getContentLength(HeaderCollection headers) {
		try {
			String clen = headers.getValue("content-length");
			if (clen != null) {
				return Long.parseLong(clen);
			} else {
				clen = headers.getValue("content-range");
				if (clen != null) {
					String str = clen.split(" ")[1];
					str = str.split("/")[0];
					String arr[] = str.split("-");
					return Long.parseLong(arr[1]) - Long.parseLong(arr[0]) + 1;
				} else {
					return -1;
				}
			}
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getCleanContentType(String contentType) {
		if (contentType == null || contentType.length() < 1)
			return contentType;
		int index = contentType.indexOf(";");
		if (index > 0) {
			contentType = contentType.substring(0, index).trim().toLowerCase();
		}
		return contentType;
	}

	public static String getExtension(String file) {
		int index = file.lastIndexOf(".");
		if (index > 0) {
			String ext = file.substring(index);
			return ext;
		} else {
			return null;
		}
	}

	public static final int detectOS() {
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if (os.contains("mac") || os.contains("darwin")
				|| os.contains("os x")) {
			return Values.MAC;
		} else if (os.contains("linux")) {
			return Values.LINUX;
		} else if (os.contains("windows")) {
			return Values.WINDOWS;
		} else {
			return -1;
		}
	}

	public static File getJarFile() {
		try {
			System.out.println("path jar: " + MDM.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI().getPath());
			return new File(MDM.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getNativePath() {
		return "src" + File.separator + "Extension" + File.separator + "native-messaging";
	}

	public static String getFFmpegPath() {
		return "lib" + File.separator;
	}
}
