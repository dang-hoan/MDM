package View;

import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Video_Popup extends JFrame {

	private JPanel contentPane;
	list_Video_Popup_Download list_Popup_Download;
	

	private static Video_Popup _Video_Popup;

	public static Video_Popup getInstance() {
		if (_Video_Popup == null) {
			_Video_Popup = new Video_Popup();
		}
		return _Video_Popup;
	}

	public Video_Popup() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 220, 50);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);

		JButton btnNewButton = new JButton("VIDEO DOWNLOAD");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setvisible();
			}
		});

		JButton btnNewButton_1 = new JButton("X");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close_Frame();
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE)
						.addGap(10).addComponent(btnNewButton_1).addContainerGap(16, Short.MAX_VALUE)));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(btnNewButton_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnNewButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap(50, Short.MAX_VALUE)));
		contentPane.setLayout(gl_contentPane);
		GraphicsConfiguration config = this.getGraphicsConfiguration();
		Rectangle bounds = config.getBounds();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);

		int x = bounds.x + bounds.width - insets.right - this.getWidth();
		int y = bounds.y + insets.top;
		this.setLocation(x, y);
		this.setUndecorated(true);
		list_Popup_Download = list_Video_Popup_Download.getInstance();
	}

	public void close_Frame() {
		this.dispose();
		list_Popup_Download.dispose();
	}
	public void add_model(VideoPopupitem iPopupitem) {
		list_Popup_Download.add_Model(iPopupitem);
		
	}
	public void setvisible() {
		list_Popup_Download.setAlwaysOnTop(true);
		list_Popup_Download.setVisible(true);
	}
}