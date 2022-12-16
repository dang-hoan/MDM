package BLL.Monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.swing.SwingUtilities;

import BLL.MDM;
import xdman.Config;
import xdman.monitoring.ParsedHookData;
import xdman.monitoring.Request;
import xdman.monitoring.Response;
import xdman.util.Logger;

public class Session implements Runnable {
	private Socket sock;
	private InputStream inStream;
	private OutputStream outStream;
	private Request request;
	private Response response;
	
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
//			onVideo(request, response);
			System.out.println("video");
		}
		else if (action.equals("/cmd")) {
//			onCmd(request, response);
		} 
		else if (action.equals("/quit")) {
//			onQuit(request, response);
		} 
		else if (action.startsWith("/preview")) {
//			onPreview(request, response);
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
			
			String url = body.substring(body.indexOf('=')+1, body.indexOf('\r', body.indexOf('=')));
//			if(url.equals("https://th.bing.com/th/id/R.c4f4387256bfddf88a3184c0bc483edf?rik=R52ZpQXS%2byfU0w&riu=http%3a%2f%2fwww.pixelstalk.net%2fwp-content%2fuploads%2f2016%2f05%2fDownload-Free-HD-Wallpapers-Backgrounds-Desktop.jpg&ehk=HSd9cpNYuuWalQrTbbf5A5Wj6y0jy3fuAOfhrRaO%2fIk%3d&risl=&pid=ImgRaw&r=0")) System.out.println("say yes yes");
//			else System.out.println("say good bye");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MDM.mv.newDownloadView(url);
				}
			});
			
//			ParsedHookData data = ParsedHookData.parse(b);
//			if (data.getUrl() != null && data.getUrl().length() > 0) {
//				HttpMetadata metadata = new HttpMetadata();
//				metadata.setUrl(data.getUrl());
//				metadata.setHeaders(data.getRequestHeaders());
//				metadata.setSize(data.getContentLength());
//				String file = data.getFile();
//				MDM.mv.newDownloadView(data.getUrl());
//			}
		} finally {
			setResponseOk(res);
		}
	}
	
//	private void onVideo(Request request, Response res) throws UnsupportedEncodingException {
//		try {
//			Logger.log("video received");
//			Logger.log(new String(request.getBody()));
//			if (!Config.getInstance().isShowVideoNotification()) {
//				Logger.log("video received but disabled");
//				return;
//			}
//			byte[] b = request.getBody();
//			ParsedHookData data = ParsedHookData.parse(b);
//			String type = data.getContentType();
//			if (type == null) {
//				type = "";
//			}
//			if (type.contains("f4f") || type.contains("m4s") || type.contains("mp2t") || data.getUrl().contains("fcs")
//					|| data.getUrl().contains("abst") || data.getUrl().contains("f4x")
//					|| data.getUrl().contains(".fbcdn") || data.getUrl().contains("http://127.0.0.1:9614")) {
//				return;
//			}
//			if (!(processDashSegment(data) || processVideoManifest(data))) {
//				processNormalVideo(data);
//			}
//		} finally {
//			setResponseOk(res);
//		}
//	}
	
	private void setResponseOk(Response res) {
		res.setCode(200);
		res.setMessage("OK");
		HeaderCollection headers = new HeaderCollection();
		headers.setValue("content-type", "application/json");
		headers.setValue("Cache-Control", "max-age=0, no-cache, must-revalidate");
		res.setHeaders(headers);
	}


}
