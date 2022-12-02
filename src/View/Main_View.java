package View;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import BLL.Values;
import BLL.DownFile.DownloadManager;
import BLL.DownFile.DownloadTask;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class Main_View extends JFrame {

	private JPanel contentPane; 
	JScrollPane scrollPaneListView = new JScrollPane();
	JList<CompactTask> listView = new JList<>();
	
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main_View frame = new Main_View();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Main_View() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					DownloadManager.getInstance().shutdown();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			@Override
			public void windowOpened(WindowEvent e) {
				try {
					DownloadManager.getInstance().resumeTasks();
					ReloadView();					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		setTitle("PBL4_MAX_SPEED_DOWNLOAD");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 400);
		setLocationRelativeTo(null);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("New Download");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					newDownloadView();					
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} 
			}
		});
		mntmNewMenuItem_1.setMnemonic(KeyEvent.VK_N);
		mnNewMenu.add(mntmNewMenuItem_1);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Delete Download");
		mnNewMenu.add(mntmNewMenuItem);
		
		JMenu mnNewMenu_1 = new JMenu("Document");
		menuBar.add(mnNewMenu_1);
		
		JMenuItem mntmNewMenuItem_2 = new JMenuItem("All");
		mnNewMenu_1.add(mntmNewMenuItem_2);
		
		JMenuItem mntmNewMenuItem_3 = new JMenuItem("Video");
		mnNewMenu_1.add(mntmNewMenuItem_3);
		
		JMenuItem mntmNewMenuItem_4 = new JMenuItem("Music");
		mnNewMenu_1.add(mntmNewMenuItem_4);
		
		JMenuItem mntmNewMenuItem_5 = new JMenuItem("Picture");
		mnNewMenu_1.add(mntmNewMenuItem_5);
		
		JMenu mnNewMenu_2 = new JMenu("Download");
		menuBar.add(mnNewMenu_2);
		
		JMenuItem mntmNewMenuItem_6 = new JMenuItem("Downloading");
		mnNewMenu_2.add(mntmNewMenuItem_6);
		
		JMenuItem mntmNewMenuItem_7 = new JMenuItem("Downloaded");
		mnNewMenu_2.add(mntmNewMenuItem_7);
		
		JMenu mnNewMenu_3 = new JMenu("Tools");
		menuBar.add(mnNewMenu_3);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		
		JPanel under_panel = new JPanel();
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(under_panel, GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
					.addContainerGap())
				.addComponent(scrollPaneListView, GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(scrollPaneListView, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(under_panel, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
		);
	
		scrollPaneListView.setViewportView(listView);
		
		under_panel.setLayout(null);
		
		JButton bNewDownload = new JButton("");
		bNewDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					newDownloadView();		
					
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				}
			}
		});
		bNewDownload.setBounds(0, 0, 62, 65);
		under_panel.add(bNewDownload);
		bNewDownload.setIcon(new ImageIcon(Main_View.class.getResource("/View/icon/plus.png")));
		
		JButton bCancelDownload = new JButton("");
		bCancelDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DownloadManager.getInstance().cancelTask(Values.Task_ID_COUNTER-1);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		bCancelDownload.setIcon(new ImageIcon(Main_View.class.getResource("/View/icon/x.png")));
		bCancelDownload.setBounds(72, 0, 62, 65);
		under_panel.add(bCancelDownload);
		
		JButton bPauseDownload = new JButton("");
		bPauseDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DownloadManager.getInstance().pauseTask(listView.getSelectedIndex());
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		bPauseDownload.setIcon(new ImageIcon(Main_View.class.getResource("/View/icon/pause.png")));
		bPauseDownload.setBounds(144, 0, 62, 65);
		under_panel.add(bPauseDownload);
		
		JButton bStartDownload = new JButton("");
		bStartDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view_Task_DownLoad viewTaskDownload = new view_Task_DownLoad(listView.getSelectedIndex());
				viewTaskDownload.setVisible(true);	
				
//				Lấy TaskID của file mà người dùng chọn r tạo mới view_Task_Download để tải
//				view_Task_DownLoad viewTaskDownload = new view_Task_DownLoad(TaskID);
//				viewTaskDownload.setVisible(true);

			}
		});
		bStartDownload.setIcon(new ImageIcon(Main_View.class.getResource("/View/icon/play.png")));
		bStartDownload.setBounds(216, 0, 62, 65);
		under_panel.add(bStartDownload);
		
		JButton bSettings = new JButton("");
		bSettings.setIcon(new ImageIcon(Main_View.class.getResource("/View/icon/gear.png")));
		bSettings.setBounds(288, 0, 62, 65);
		under_panel.add(bSettings);
		contentPane.setLayout(gl_contentPane);
	}
	public void ReloadView()
	{
		DefaultListModel<CompactTask> model = new DefaultListModel<>();
		for(int i = 0; i < Values.Task_ID_COUNTER; i++)
		{
			try {
				DownloadTask task = DownloadManager.getInstance().getTask(i);
				String str_name = task.getSaveName();
				URL urlicon = null;
				String str_status = Values.State(task.getDownloadStatus());
				String str_size = new String();
				double totalSize = task.getFileSize();
				if(totalSize == -1) totalSize = task.getCurrentSize();
				double downloadedSize = task.getCurrentSize();
				
				String[] donvi = {"B", "KB", "MB", "GB", "TB"};
				int total = 0, download = 0;
				while(totalSize/1024 > 1 && total < donvi.length) {
					totalSize /= 1024; total++;
				}
				while(downloadedSize/1024 > 1 && download < donvi.length) {
					downloadedSize /= 1024; download++;
				}
//				
//				String donvi = "B";
//				if (totalSize/1024 > 1)
//				{
//					totalSize /= 1024;
//					downloadedSize /= 1024;
//					donvi = "KB";
//				}
//				if (totalSize/1024 > 1)
//				{
//					totalSize /= 1024;
//					downloadedSize /= 1024;
//					donvi = "MB";
//				}
//				if (totalSize/1024 > 1)
//				{
//					totalSize %= 1024;
//					downloadedSize /= 1024;
//					donvi = "GB";
//				}
				
				switch (task.getDownloadStatus()) {
				case 1:
					str_size = ""; urlicon = Main_View.class.getResource("/View/icon/ready.png"); break;
				case 2: 
					str_size = ""; urlicon = Main_View.class.getResource("/View/icon/dloading.png"); break;
				case 3: //pause
					str_size = String.format("%.2f%s / %.2f%s",downloadedSize, donvi[download], totalSize, donvi[total]);
					urlicon =Main_View.class.getResource("/View/icon/dloading.png"); break;
				case 4:
					str_size = String.format("%.2f %s", totalSize, donvi[total]);
					urlicon = Main_View.class.getResource("/View/icon/completed.png"); break;
				case 5: 
					str_size = ""; urlicon = Main_View.class.getResource("/View/icon/canceled.png"); break;
				}
				
				String str_date = Values.dateFormat.format(task.getCreateDate());
				model.addElement(new CompactTask(str_name, urlicon, str_status, str_size, str_date));
			} 
			catch (Exception e) { e.printStackTrace(); }
		}
		listView.setModel(model);
		listView.setCellRenderer(new TaskRenderer());
	}
	public void newDownloadView()
	{
		try {
			new NewDownload_View(this).setVisible(true);
			
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
