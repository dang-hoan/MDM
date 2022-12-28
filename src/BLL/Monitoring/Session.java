package BLL.Monitoring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingUtilities;

import BLL.MDM;
import BLL.Utils;
import View.VideoPopupitem;
import View.Video_Popup;

public class Session implements Runnable {
	private Socket sock;
	private InputStream inStream;
	private OutputStream outStream;
	private Request request;
	private Response response;
//	private URL[]

	public Session(Socket socket) {
		this.sock = socket;
		this.request = new Request();
		this.response = new Response();
		System.out.println("New session");
	}

	public void start() {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void run() {
		serviceRequest();
	}

	private void serviceRequest() {
		try {
			inStream = sock.getInputStream();
			outStream = sock.getOutputStream();
			while (true) {
				this.request.read(inStream);
				this.processRequest(this.request, this.response);
				System.out.println("Request processed, sending response\n");
//				this.response.write(System.out);
//				this.response.write(outStream);
				this.response.write(outStream);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		close(); //close stream and socket
	}

	private void close() {
		try {
			inStream.close();
		} catch (Exception e) {
		}

		try {
			outStream.close();
		} catch (Exception e) {
		}

		try {
			sock.close();
		} catch (Exception e) {
		}
	}

	private void processRequest(Request request, Response res) throws IOException {
		String action = request.getAction();
		if (action.equals("/download")) {
			onDownload(request, response);
		}
		else if (action.equals("/video")) {
			onVideo(request, response);
		}
		else if (action.startsWith("/item")) {
			onVideoRetrieve(request, response);
		}
		else if (action.equals("/cmd")) {
//			onCmd(request, response);
		}
		else if (action.startsWith("/clear")) {
			onVideoClear(request, response);
		}
		else if (action.equals("/quit")) {
//			onQuit(request, response);
		}
		else if (action.startsWith("/preview")) {
//			onPreview(request, response);
		}
		else if (action.toLowerCase().startsWith("/sync")) {
			System.out.println("sync");
		}
		else {
//			throw new IOException("invalid action " + action);
			System.out.println("invalid action " + action);
		}
	}

	private void onDownload(Request request, Response res) throws UnsupportedEncodingException {
		try {
			byte[] b = request.getBody();
			String body = new String(b, "UTF-8");
			System.out.println("\nbody:");
			System.out.println("=====================================");
			System.out.print(body);
			System.out.println("=====================================");

			ParsedHookData data = ParsedHookData.parse(b);
			if (data.getUrl() != null && data.getUrl().length() > 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						MDM.mv.newDownloadView(data.getUrl(), data.getContentLength());
						System.out.println(data.getContentLength());
					}
				});
			}

		} finally {
			setResponseOk(res);
		}
	}

	private void setResponseOk(Response res) {
		res.setCode(200);
		res.setMessage("OK");
		HeaderCollection headers = new HeaderCollection();
		headers.setValue("content-type", "application/json");
		headers.setValue("Cache-Control", "max-age=0, no-cache, must-revalidate");
		res.setHeaders(headers);
	}

	private void onVideo(Request request, Response res) throws UnsupportedEncodingException {
		try {
			System.out.println("video received");
//			System.out.println(new String(request.getBody()));
//			if (!Config.getInstance().isShowVideoNotification()) {
//				System.out.println("video received but disabled");
//				return;
//			}
			byte[] b = request.getBody();
			ParsedHookData data = ParsedHookData.parse(b);
			String type = data.getContentType();
			if (type == null) {
				type = "";
			}
			//link rác
			if (type.contains("f4f") || type.contains("m4s") || type.contains("mp2t") || data.getUrl().contains("fcs")
					|| data.getUrl().contains("abst") || data.getUrl().contains("f4x")
					|| data.getUrl().contains(".fbcdn") || data.getUrl().contains("http://127.0.0.1:9614")) {
				return;
			}

			if (!(processDashSegment(data) || processVideoManifest(data))) {
				processNormalVideo(data);
			}
		} finally {
			setResponseOk(res);
		}
	}

	private void onVideoRetrieve(Request request, Response res) throws UnsupportedEncodingException {
//		try {
			String content = new String(request.getBody(), "utf-8");
			System.out.println("Video retrieve: " + content);
//			String lines[] = content.split("\r\n");
//			for (String line : lines) {
//				String id = line.trim();
//				for (VideoPopupItem item : XDMApp.getInstance().getVideoItemsList()) {
//					if (id.equals(item.getMetadata().getId())) {
//						HttpMetadata md = item.getMetadata().derive();
//						Logger.log("dash metdata ? " + (md instanceof DashMetadata));
//						XDMApp.getInstance().addVideo(md, item.getFile());
//					}
//				}
//			}
//		} finally {
//			setResponseOk(res);
//		}
	}

	private boolean processDashSegment(ParsedHookData data) {
		try {
			URL url = new URL(data.getUrl());
			String host = url.getHost();
			if (!(host.contains("youtube.com") || host.contains("googlevideo.com"))) {
				System.out.println("non yt host");
				return false;
			}
			String type = data.getContentType();
			if (type == null) {
				type = "";
			}
			if (!(type.contains("audio/") || type.contains("video/") || type.contains("application/octet"))) {
				System.out.println("non yt type");
				return false;
			}
			String low_path = data.getUrl().toLowerCase();
			if (low_path.indexOf("videoplayback") >= 0 && low_path.indexOf("itag") >= 0) {
				// found DASH audio/video stream
				if (url.getQuery() == null | url.getQuery().trim().length() < 1) {
					return false;
				}

				int index = data.getUrl().indexOf("?");

				String path = data.getUrl().substring(0, index);
				String query = data.getUrl().substring(index + 1);

				String arr[] = query.split("&");
				StringBuilder yt_url = new StringBuilder();
				yt_url.append(path + "?");  //mở rộng path url video (thêm thông tin vào url...)
				int itag = 0;
				long clen = 0;
				String id = "";
				String mime = "";

				for (int i = 0; i < arr.length; i++) {
					String str = arr[i];
					index = str.indexOf("=");
					if (index > 0) {
						String key = str.substring(0, index).trim();
						String val = str.substring(index + 1).trim();
						if (key.startsWith("range")) {
							continue;
						}
						if (key.equals("itag")) {
							itag = Integer.parseInt(val);
						}
						if (key.equals("clen")) {
							clen = Long.parseLong(val);
						}
						if (key.startsWith("mime")) {
							mime = URLDecoder.decode(val, "UTF-8");
						}
						if (str.startsWith("id")) {
							id = val;
						}
					}
					yt_url.append(str);
					if (i < arr.length - 1) {
						yt_url.append("&");
					}
				}
				if (itag != 0) {
					if (YtUtil.isNormalVideo(itag)) {
						System.out.println("Normal vid");
						return false;
					}
				}

				DASH_INFO info = new DASH_INFO();
				info.url = yt_url.toString();
				info.clen = clen;
				info.video = mime.startsWith("video");
				info.itag = itag;
				info.id = id;
				info.mime = mime;
				info.headers = data.getRequestHeaders();

				System.out.println("processing yt mime: " + mime + " id: " + id + " clen: " + clen + " itag: " + itag);

				if (YtUtil.addToQueue(info)) {
					DASH_INFO di = YtUtil.getDASHPair(info);

					if (di != null) {
						System.out.println("+++updating adding");
//						System.out.println("URL la: " + di.url + "\n" + info.url);//hình ảnh và âm thanh
						String videoURL = di.url;
						String audioURL = info.url;
						String fileName = data.getFile() + "." + getYtDashFormat(info.mime, di.mime);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								Video_Popup popup = Video_Popup.getInstance();
								popup.add_model(new VideoPopupitem(videoURL, audioURL, di.clen, info.clen, fileName));
								if(!popup.isVisible())
								{
									popup.setAlwaysOnTop(true);
									popup.setVisible(true);
								}
								System.out.println("a1b1c1");
							}
						});
						return true;
					}
				} else {
					System.out.println("+++updating");
					System.out.println("same");
					Video_Popup popup = Video_Popup.getInstance();
					popup.reload(id, clen, data.getFile());
					System.out.println("Tên name :"+data.getFile());
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean processVideoManifest(ParsedHookData data) {
		String url = data.getUrl();
		System.out.println("processVideoManifest..........................");
		// String file = data.getFile();
		String contentType = data.getContentType();
		if (contentType == null) {
			contentType = "";
		}
		String ext = Utils.getExtension(Utils.getFileName(data.getUrl()));
		File manifestfile = null;

		try {
			if (contentType.contains("mpegurl") || ".m3u8".equalsIgnoreCase(ext) || contentType.contains("m3u8")) {
				System.out.println("Downloading m3u8 manifest");
				manifestfile = downloadMenifest(data);
				return true;
			}
			if (contentType.contains("f4m") || ".f4m".equalsIgnoreCase(ext)) {
				System.out.println("Downloading f4m manifest");
				manifestfile = downloadMenifest(data);
				return true;
			}
			if (url.contains(".facebook.com") && url.toLowerCase().contains("pagelet")) {
				System.out.println("Downloading fb manifest");
				manifestfile = downloadMenifest(data);
				return true;
			}
			if ((url.contains("player.vimeo.com") && contentType.toLowerCase().contains("json")) || url.contains("instagram.com/p/")) {
				System.out.println("Downloading video manifest");
				manifestfile = downloadMenifest(data);
				return true;
			}
		} catch (Exception e) {
		} finally {
			if (manifestfile != null) {
				manifestfile.delete();
			}
		}

		return false;
	}

	private void processNormalVideo(ParsedHookData data) {
		System.out.println("process normal.........................");
		String file = data.getFile();
		String type = data.getContentType();
		if (type == null) {
			type = "";
		}
		if (file == null || file.trim().length() < 1) {
			file = Utils.getFileName(data.getUrl());
		}
		String ext = "";
		if (type.contains("video/mp4")) {
			ext = "mp4";
		} else if (type.contains("video/x-flv")) {
			ext = "flv";
		} else if (type.contains("video/webm")) {
			ext = "mkv";
		} else if (type.contains("matroska") || type.contains("mkv")) {
			ext = "mkv";
		} else if (type.equals("audio/mpeg") || type.contains("audio/mp3")) {
			ext = "mp3";
		} else if (type.contains("audio/aac")) {
			ext = "aac";
		} else if (type.contains("audio/mp4")) {
			ext = "m4a";
		} else {
			return;
		}
		file += "." + ext;

		//loại bỏ những đoạn âm thanh thông báo (là tiếng bip như success.mp3, failure.mp3 ...) do server gởi về
		long minSizeVideo = 1048576;
		if (data.getContentLength() < minSizeVideo) {
			System.out.println("video less than min size");
			return;
		}

		System.out.println("process normal: " + data.getUrl());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MDM.mv.newDownloadView(data.getUrl(), data.getContentLength());
			}
		});

//		XDMApp.getInstance().addMedia(metadata, file, sz);
	}

	private File downloadMenifest(ParsedHookData data) {
//		JavaHttpClient client = null;
//		OutputStream out = null;
		try {
			System.out.println("downloading manifest: " + data.getUrl());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MDM.mv.newDownloadView(data.getUrl(), data.getContentLength());
				}
			});
//			client = new JavaHttpClient(data.getUrl());
			Iterator<HttpHeader> headers = data.getRequestHeaders().getAll();
			boolean hasAccept = false;
			List<String> cookieList = new ArrayList<>();
			while (headers.hasNext()) {
				HttpHeader header = headers.next();
				//System.err.println(header.getName() + " " + header.getValue());
				if (header.getName().toLowerCase(Locale.ENGLISH).equals("cookie")) {
					cookieList.add(header.getValue());
					continue;
				}
				if (header.getName().toLowerCase().equals("accept")) {
					hasAccept = true;
				}
//				client.addHeader(header.getName(), header.getValue());
			}
			if (!hasAccept) {
//				client.addHeader("Accept", "*/*");
			}
			if (cookieList.size() > 0) {
//				client.addHeader("Cookie", String.join(";", cookieList));
			}
//			client.setFollowRedirect(true);
//			client.connect();
//			int resp = client.getStatusCode();
//			System.out.println("manifest download response: " + resp);
//			if (resp == 206 || resp == 200) {
//				InputStream in = client.getInputStream();
//				File tmpFile = new File(Config.getInstance().getTemporaryFolder(), UUID.randomUUID().toString());
//				long len = client.getContentLength();
//				out = new FileOutputStream(tmpFile);
//				XDMUtils.copyStream(in, out, len);
//				System.out.println("manifest download successfull");
//
//				return tmpFile;
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
//				out.close();
			} catch (Exception e) {
			}
			try {
//				client.dispose();
			} catch (Exception e) {
			}
		}
		return null;
	}

	private void onVideoClear(Request request, Response response) {
		try {
			YtUtil.videoQueue.clear();
			YtUtil.audioQueue.clear();
//			XDMApp.getInstance().getVideoItemsList().clear();
//			BrowserMonitor.getInstance().updateSettingsAndStatus();
		} finally {
			setResponseOk(response);
		}
	}

	private String getYtDashFormat(String videoContentType, String audioContentType) {
		if (videoContentType == null) {
			videoContentType = "";
		}
		if (audioContentType == null) {
			audioContentType = "";
		}
		if (videoContentType.contains("mp4") && audioContentType.contains("mp4")) {
			return "mp4";
		} else {
			return "mkv";
		}
	}

}
