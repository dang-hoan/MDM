package View;

import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import BLL.Values;
import BLL.DownFile.DownloadManager;
import BLL.DownFile.DownloadTask;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JList;

public class list_Video_Popup_Download extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	private JList<VideoPopupitem> list;
	private DefaultListModel<VideoPopupitem> model;

	private static list_Video_Popup_Download _Video_Popup_Download;

	public static list_Video_Popup_Download getInstance() {
		if (_Video_Popup_Download == null) {
			_Video_Popup_Download = new list_Video_Popup_Download();
		}
		return _Video_Popup_Download;
	}
	public list_Video_Popup_Download() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 220, 220);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
		);
		
		list = new JList<VideoPopupitem>();
		scrollPane.setViewportView(list);
		contentPane.setLayout(gl_contentPane);
		GraphicsConfiguration config = this.getGraphicsConfiguration();
		Rectangle bounds = config.getBounds();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);

		int x = bounds.x + bounds.width - insets.right - this.getWidth();
		int y = bounds.y + insets.top;
		this.setLocation(x, y+40);
		this.setUndecorated(true);
		model = new DefaultListModel<VideoPopupitem>();
		list.setModel(model);
		list.setCellRenderer(new Popup_Renderer());
		add_Event();
	}
	public void add_Model(VideoPopupitem item) {
		System.out.println("abc");
		model.addElement(item);
		list.setModel(model);
		System.out.println("abc");
	}
	public void add_Event()
	{
		list.addMouseListener( new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (e.getClickCount() == 2) {
						VideoPopupitem popupitem = list.getSelectedValue();
						NewDownload_View_Video window = new NewDownload_View_Video(Main_View.getInstance(), popupitem.getUrl_Video(), popupitem.getUrl_Audio(), popupitem.getFile_Name());
						window.setAlwaysOnTop(true);
						window.setVisible(true);
					}

				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
