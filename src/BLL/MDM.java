package BLL;

import java.io.IOException;

import BLL.Monitoring.BrowserMonitor;
import View.Main_View;

public class MDM {
	public static Main_View mv;
	
	public static void main(String[] args) throws IOException {
		System.setProperty("http.KeepAlive.remainingData", "0");
		System.setProperty("http.KeepAlive.queuedConnections", "0");
		System.setProperty("sun.net.http.errorstream.enableBuffering", "false");
		System.setProperty("awt.useSystemAAFontSettings", "lcd");
		System.setProperty("swing.aatext", "true");
		System.setProperty("sun.java2d.d3d", "false");
		System.setProperty("sun.java2d.opengl", "false");
		System.setProperty("sun.java2d.xrender", "false");
		
		System.setProperty("sun.java2d.uiScale.enabled", "true");
		System.setProperty("sun.java2d.uiScale", String.format("%.2f", Values.ZOOM_LEVEL_VALUES[6]));
		mv = new Main_View();
		mv.setVisible(true);	
		BrowserMonitor.getInstance().start();
	}
	
}
