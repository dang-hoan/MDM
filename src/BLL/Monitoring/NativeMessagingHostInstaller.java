package BLL.Monitoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JOptionPane;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import BLL.Utils;
import BLL.Values;
import BLL.DownFile.DownloadManager;

public class NativeMessagingHostInstaller {
	//truyền id của tiện ích vào
	private static final String CHROME_EXTENSION_IDS = String.join(",",
			"\"chrome-extension://ogcijeogilmnohjiomiiclbfblcifkim/\"",
			"\"chrome-extension://dholefpnmilogpgldenhknjhhbdgfcap/\"",
			"\"chrome-extension://lnplfilkbalalfibdbcnjneeganfdagk/\"");

	private static final String FIREFOX_EXTENSION_IDS = String.join(",", "\"browser-mon@xdman.sourceforge.net\"");

	private static final String CHROME_LINUX_LOCATION = ".config/google-chrome/NativeMessagingHosts",
			FIREFOX_LINUX_LOCATION = ".mozilla/native-messaging-hosts",
			CHROME_MAC_LOCATION = "Library/Application Support/Google/Chrome/NativeMessagingHosts",
			FIREFOX_MAC_LOCATION = "Library/Application Support/Mozilla/NativeMessagingHosts",
			CHROMIUM_LINUX_LOCATION = ".config/chromium/NativeMessagingHosts",
			CHROMIUM_MAC_LOCATION = "Library/Application Support/Chromium/NativeMessagingHosts";

	public static final synchronized void installNativeMessagingHostForChrome() {
		installNativeMessagingHostForChrome(Utils.detectOS(), false);
	}

	public static final void installNativeMessagingHostForChromium() {
		installNativeMessagingHostForChrome(Utils.detectOS(), true);
	}

	public static final void installNativeMessagingHostForFireFox() {
		installNativeMessagingHostForFireFox(Utils.detectOS());
	}

	private static final void installNativeMessagingHostForChrome(int os, boolean chromium) {
		if (os == Values.WINDOWS) {
			if (!Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER,
					"Software\\Google\\Chrome\\NativeMessagingHosts\\com.mdm")) {
				if (!Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER,
						"Software\\Google\\Chrome\\NativeMessagingHosts", "com.mdm")) {
					JOptionPane.showMessageDialog(null, "Error: Unable to register native messaging host");
					return;
				}
			}
			File manifestFile = new File(DownloadManager.getInstance().getDataDir(), "mdm.native_host.json");
			File nativeHostFile = new File(Utils.getJarFile().getParentFile() + File.separator + Utils.getNativePath(), "native.exe");
			createNativeManifest(manifestFile, nativeHostFile, BrowserType.Chrome);
			try {
				Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER,
						"Software\\Google\\Chrome\\NativeMessagingHosts\\com.mdm", null,
						manifestFile.getAbsolutePath());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error: Unable to register native messaging host");
				return;
			}
		} else {
			File manifestFolder = new File(System.getProperty("user.home"),
					os == Values.MAC ? (chromium ? CHROMIUM_MAC_LOCATION : CHROME_MAC_LOCATION)
							: (chromium ? CHROMIUM_LINUX_LOCATION : CHROME_LINUX_LOCATION));
			if (!manifestFolder.exists()) {
				manifestFolder.mkdirs();
			}
			File manifestFile = new File(manifestFolder, "mdm.native_host.json");
			File nativeHostFile = new File(Utils.getJarFile().getParentFile() + File.separator + Utils.getNativePath(), "native");
			createNativeManifest(manifestFile, nativeHostFile, BrowserType.Chrome);
		}

	}

	public static final void installNativeMessagingHostForFireFox(int os) {
		if (os == Values.WINDOWS) {
			if (!Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER,
					"Software\\Mozilla\\NativeMessagingHosts\\com.mdm")) {
				if (!Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "Software\\Mozilla\\NativeMessagingHosts",
						"com.mdm")) {
					JOptionPane.showMessageDialog(null, "Error: Unable to register native messaging host");
					return;
				}
			}

			File manifestFile = new File(DownloadManager.getInstance().getDataDir(), "mdm.native_host.json");
			File nativeHostFile = new File(Utils.getJarFile().getParentFile() + File.separator + Utils.getNativePath(), "native.exe");
			createNativeManifest(manifestFile, nativeHostFile, BrowserType.Firefox);
			try {
				Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER,
						"Software\\Mozilla\\NativeMessagingHosts\\com.mdm", null,
						manifestFile.getAbsolutePath());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error: Unable to register native messaging host");
				return;
			}
		} else {
			File manifestFolder = new File(System.getProperty("user.home"),
					os == Values.MAC ? FIREFOX_MAC_LOCATION : FIREFOX_LINUX_LOCATION);
			if (!manifestFolder.exists()) {
				manifestFolder.mkdirs();
			}
			File manifestFile = new File(manifestFolder, "mdm.native_host.json");
			File nativeHostFile = new File(Utils.getJarFile().getParentFile() + File.separator + Utils.getNativePath(), "native");
			createNativeManifest(manifestFile, nativeHostFile, BrowserType.Firefox);
		}
	}

	private static final void createNativeManifest(File manifestFile, File nativeHostFile, BrowserType browserType) {
		try (OutputStream out = new FileOutputStream(manifestFile)) {
			String name, manifestKey, extension;
			if (browserType == BrowserType.Chrome || browserType == BrowserType.Chromium) {
				manifestKey = "\"allowed_origins\"";
				extension = CHROME_EXTENSION_IDS;
				name = "\"com.mdm\"";
			} else {
				manifestKey = "\"allowed_extensions\"";
				extension = FIREFOX_EXTENSION_IDS;
				name = "\"com.mdm\"";
			}

			String json = String.format(
					"{\n" + "  \"name\": %s,\n"
							+ "  \"description\": \"Native messaging host for Max Download Manager\",\n"
							+ "  \"path\": \"%s\",\n" + "  \"type\": \"stdio\",\n" + "  %s: [ %s ]\n" + "}",
					name, nativeHostFile.getAbsolutePath().replace("\\", "\\\\"), manifestKey, extension);

			out.write(json.getBytes("utf-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public enum BrowserType {
		Chrome, Chromium, Firefox
	}
}
