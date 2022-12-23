package BLL.Monitoring;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BrowserMonitor implements Runnable{
	private static BrowserMonitor _this;

	public static BrowserMonitor getInstance() {
		if (_this == null) {
			_this = new BrowserMonitor();
		}
		return _this;
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		ServerSocket serverSock = null;
		try {
			serverSock = new ServerSocket();
			serverSock.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 2002));
			System.out.println("server listenning...");
			System.out.println("host: " + serverSock.getInetAddress() + ", port: " + serverSock.getLocalPort());
			NativeMessagingHostInstaller.installNativeMessagingHostForChrome();
			NativeMessagingHostInstaller.installNativeMessagingHostForFireFox();
			NativeMessagingHostInstaller.installNativeMessagingHostForChromium();
			while (true) {
				Socket sock = serverSock.accept();
				Session session = new Session(sock);
				session.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			serverSock.close();
		} catch (Exception e) {

		}
	}
}
