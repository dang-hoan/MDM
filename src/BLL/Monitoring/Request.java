package BLL.Monitoring;

import java.io.IOException;
import java.io.InputStream;

import BLL.Utils;

public class Request {
	private String action;
	private HeaderCollection headers;
	private byte[] body;
	private int method;

	public void read(InputStream in) throws IOException {
		String reqLine = Utils.readLine(in);
		System.out.println("first line: " + reqLine); // dòng đầu tiên có dạng POST /download HTTP/1.1
		
		if (reqLine == null || reqLine.length() < 1) {
			throw new IOException("Invalid request line: " + reqLine);
		}

		String[] arr = reqLine.split(" "); 
		if (arr.length != 3) {
			throw new IOException("Invalid request: " + reqLine);
		}
		this.action = arr[1];
		this.method = arr[0].toLowerCase().equals("post") ? 1 : 2;		
//		System.out.println("action: " + action + ", method: " + method);
		
		this.headers = new HeaderCollection();		
		headers.loadFromStream(in);
		String header = headers.getValue("Content-Length");
		
		if (header != null) {		
			long len = Long.parseLong(header);
			body = new byte[(int) len];
			int off = 0;
			while (len > 0) {
				int x = in.read(body, off, body.length - off);
				if (x == -1) {
					throw new IOException("Unexpected EOF");
				}
				len -= x;
				off += x;
			}
		}
	}

	public final String getAction() {
		return action;
	}

	public final void setUrl(String action) {
		this.action = action;
	}

	public final HeaderCollection getHeaders() {
		return headers;
	}

	public final void setHeaders(HeaderCollection headers) {
		this.headers = headers;
	}

	public final byte[] getBody() {
		return body;
	}

	public final void setBody(byte[] body) {
		this.body = body;
	}

	public final int getMethod() {
		return method;
	}

	public final void setMethod(int method) {
		this.method = method;
	}
}
