package BLL.Monitoring;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import View.Main_View;

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
//			NativeMessagingHostInstaller.installNativeMessagingHostForFireFox();
//			NativeMessagingHostInstaller.installNativeMessagingHostForChromium();
			while (true) {
				Socket sock = serverSock.accept();
				Session session = new Session(sock);
				session.start();
			}
		} catch (Exception e) {
			ImageIcon icon = new ImageIcon(Main_View.class.getResource("/View/icon/error.png"));
			icon = new ImageIcon(icon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH));

			JOptionPane.showMessageDialog(null, "Cổng 2002 để lắng nghe yêu cầu tiện ích đã bị chiếm!! (Có thể do bạn đã mở 1 app MDM trước đó, hoặc do chương trình khác chiếm)", "Notification", JOptionPane.INFORMATION_MESSAGE, icon);
			System.out.println("Cổng 2002 để lắng nghe yêu cầu tiện ích đã bị chiếm!! (Có thể do bạn đã mở 1 app MDM trước đó, hoặc do chương trình khác chiếm)");
		}
		try {
			serverSock.close();
		} catch (Exception e) {

		}
	}
}
