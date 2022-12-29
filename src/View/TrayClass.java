package View;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import BLL.MDM;

public class TrayClass {
	static TrayIcon trayIcon;

	public TrayClass() {
		super();
	}

	public void show() {
		if(!SystemTray.isSupported()) System.exit(0);
		trayIcon = new TrayIcon(createIcon("/View/icon/app.png", "Icon"));
		trayIcon.setToolTip("MDM v1.0 App");
		final SystemTray tray = SystemTray.getSystemTray();

		final PopupMenu menu = new PopupMenu();
		MenuItem about = new MenuItem("About");
		MenuItem exit = new MenuItem("Exit");
		menu.add(about);
		menu.addSeparator();
		menu.add(exit);

		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "MDM v1.0 App \nTác giả: \n - Đăng Hoan\n - Bảo Quốc\n - Trần Tín");
			}
		});

		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});


		trayIcon.setPopupMenu(menu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 || e.getClickCount() == 2) {
					MDM.mv.setVisible(true);
				}
			}
		});
		try{
			tray.add(trayIcon);
		}catch(Exception e) {

		}
	}

	protected Image createIcon(String path, String description) {
		URL imageURL = TrayClass.class.getResource(path);
		try {
			return ImageIO.read(imageURL);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
