package View;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import BLL.Values;
import BLL.DownFile.DownloadManager;
import BLL.DownFile.DownloadTask;
import BLL.DownFile.YTVideo;
import BLL.VideoConversion.FFmpeg;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.border.MatteBorder;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class Main_View extends JFrame {

	private JPanel contentPane;
	private JPopupMenu jPopupMenu;
	private DownloadTask task;
	private JTabbedPane tabbedPane; 
	private JList<CompactTask> listView = new JList<>();
	private JList<CompactTask> listViewAll;
	private JList<CompactTask> listViewCompl;
	private JList<CompactTask> listViewIncom;
	private JList<CompactTask> listViewQueue;
	private JTextField txtSearch;
	private JComboBox<String> cbbSort;
	
	private static Main_View _Main_View;

	public static Main_View getInstance() {
		if (_Main_View == null) {
			_Main_View = new Main_View();
		}
		return _Main_View;
	}
	private Main_View() {
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
					new TrayClass().show();
					DownloadManager.getInstance().resumeTasks();
					ReloadView();
					sort_By_Date();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		setTitle("PBL4_MAX_SPEED_DOWNLOAD");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setSize(700, 400);
		setLocationRelativeTo(null);
		//icon
		try {
			this.setIconImage(ImageIO.read(getClass().getResourceAsStream("/View/icon/app.png")));
			
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewDownload = new JMenuItem("New Download");
		mntmNewDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					newDownloadView("", -2);					
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} 
			}
		});
		mntmNewDownload.setMnemonic(KeyEvent.VK_N);
		mnNewMenu.add(mntmNewDownload);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnNewMenu.add(mntmExit);
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					System.exit(0);					
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} 
			}
		});

		contentPane = new JPanel();

		setContentPane(contentPane);
		
		JPanel under_panel = new JPanel();
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(under_panel, GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
					.addContainerGap())
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(under_panel, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
		);
	
		JScrollPane scrollPaneListView_0 = new JScrollPane();
		tabbedPane.addTab("All", null, scrollPaneListView_0, null);

		listViewAll = new JList<CompactTask>();
		scrollPaneListView_0.setViewportView(listViewAll);

		JScrollPane scrollPaneListView_1 = new JScrollPane();
		tabbedPane.addTab("Complete", null, scrollPaneListView_1, null);

		listViewCompl = new JList<CompactTask>();
		scrollPaneListView_1.setViewportView(listViewCompl);

		JScrollPane scrollPaneListView_2 = new JScrollPane();
		tabbedPane.addTab("Incomplete", null, scrollPaneListView_2, null);

		listViewIncom = new JList<CompactTask>();
		scrollPaneListView_2.setViewportView(listViewIncom);

		JScrollPane scrollPaneListView_3 = new JScrollPane();
		tabbedPane.addTab("Queue", null, scrollPaneListView_3, null);

		listViewQueue = new JList<CompactTask>();
		scrollPaneListView_3.setViewportView(listViewQueue);
		
		under_panel.setLayout(null);
		
		listView = listViewAll; //listView ban đầu = listViewAll

		JPanel PanelGrid = new JPanel();
		PanelGrid.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(192, 192, 192)));

		scrollPaneListView_0.setColumnHeaderView(PanelGrid);

		PanelGrid.setLayout(new GridLayout(0, 3, 0, 0));

		JPanel panel1 = new JPanel();
		PanelGrid.add(panel1);

		JPanel panel2_Sort = new JPanel();
		PanelGrid.add(panel2_Sort);
		panel2_Sort.setLayout(new GridLayout(0, 2, 0, 0));

		JLabel lbl1 = new JLabel("Sort by:    ");
		lbl1.setHorizontalAlignment(SwingConstants.RIGHT);
		panel2_Sort.add(lbl1);

		cbbSort = new JComboBox<>();
		cbbSort.addItem("");
		cbbSort.addItem("Name");
		cbbSort.addItem("Date modified");
		cbbSort.addItem("Size");
		cbbSort.addItem("Type item");
		cbbSort.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (cbbSort.getSelectedItem().equals("Name")) sort_By_Name();
                else if (cbbSort.getSelectedItem().equals("Date modified")) sort_By_Date();
                else if (cbbSort.getSelectedItem().equals("Size")) sort_By_Size();
                else if (cbbSort.getSelectedItem().equals("Type item")) sort_By_Type();
            }
        });
		panel2_Sort.add(cbbSort);

		JPanel panel3_Search = new JPanel();
		PanelGrid.add(panel3_Search);
		panel3_Search.setLayout(new BorderLayout(0, 0));

		txtSearch = new JTextField();
		panel3_Search.add(txtSearch, BorderLayout.CENTER);
		txtSearch.setColumns(10);
		txtSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ReloadView();
			}
		});

		JButton btSearch = new JButton("");
		btSearch.setIcon(new ImageIcon(Main_View.class.getResource("/View/icon/search.png")));
		btSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ReloadView();
			}
		});
		panel3_Search.add(btSearch, BorderLayout.EAST);

		//sự kiện thay đổi tab
		tabbedPane.addChangeListener(new ChangeListener() 
		{	@Override
			public void stateChanged(ChangeEvent e) 
			{
				switch (tabbedPane.getSelectedIndex())
				{
					case 0: 
						listView = listViewAll; 
						scrollPaneListView_0.setColumnHeaderView(PanelGrid);
						break;
					case 1: 
						listView = listViewCompl; 
						scrollPaneListView_1.setColumnHeaderView(PanelGrid);
						break;
					case 2: 
						listView = listViewIncom; 
						scrollPaneListView_2.setColumnHeaderView(PanelGrid);
						break;
					case 3: 
						listView = listViewQueue; 
						scrollPaneListView_3.setColumnHeaderView(PanelGrid);
						break;
				}
				ReloadView();				
			}
		});
		
		JButton bNewDownload = new JButton("");
		bNewDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					newDownloadView("", -2);		
					
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
					DownloadManager.getInstance().cancelTask(listView.getSelectedValue().getId());
					
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
					DownloadManager.getInstance().pauseTask(listView.getSelectedValue().getId());
				
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
				int id = listView.getSelectedValue().getId();
				if(DownloadManager.getInstance().isTaskExist(id)) {
					view_Task_DownLoad view = new view_Task_DownLoad(id, getthis());
					view.setVisible(true);
				}
				else if(DownloadManager.getInstance().isVideoExist(id)){
					view_Task_DownLoad_Video view = new view_Task_DownLoad_Video(id, getthis());
					view.setVisible(true);
				}

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
		jPopupMenu = new JPopupMenu();
		create_Jpopupmenu();
		listViewAll.addMouseListener(mouseListenerListView);
		listViewCompl.addMouseListener(mouseListenerListView);
		listViewIncom.addMouseListener(mouseListenerListView);
		listViewQueue.addMouseListener(mouseListenerListView);
	}

	public synchronized void ReloadView()
	{
		DefaultListModel<CompactTask> modelIncom = new DefaultListModel<>();
		DefaultListModel<CompactTask> modelCompl = new DefaultListModel<>();
		DefaultListModel<CompactTask> modelQueue = new DefaultListModel<>();
		DefaultListModel<CompactTask> modelAll = new DefaultListModel<>();
		for(int i = 0; i < Values.Task_ID_COUNTER; i++)
		{
			try {
				task = DownloadManager.getInstance().getTask(i);	
				CompactTask cpTask;
				if (task != null && task.getDownloadStatus() != Values.DELETED)
				{
					String name = task.getSaveName().toLowerCase();
					if (txtSearch.getText().equals("") || name.contains(txtSearch.getText().toLowerCase()))
					{
						int id = task.getTaskID();
						String folder = task.getSaveDirectory();
						String str_name = task.getSaveName();
						String str_status = Values.State(task.getDownloadStatus());
						String str_size = new String();
						double totalSize = task.getFileSize();
						if (totalSize == -1) totalSize = task.getDownloaded();
						long s = (long)totalSize;
						double downloadedSize = task.getDownloaded();

						String[] donvi = { "B", "KB", "MB", "GB", "TB" };
						int total = 0, download = 0;
						while (totalSize / 1024 > 1 && total < donvi.length) {
							totalSize /= 1024;
							total++;
						}
						while (downloadedSize / 1024 > 1 && download < donvi.length) {
							downloadedSize /= 1024;
							download++;
						}				
					
						switch (task.getDownloadStatus()) {
						case Values.READY:
							str_size = "";
							break;
						case Values.DOWNLOADING:
						case Values.ASSEMBLING:
							str_size = "";
							break;
						case Values.PAUSED: 
							str_size = String.format("%.2f%s / %.2f%s", downloadedSize, donvi[download], totalSize,
									donvi[total]);
							break;
						case Values.FINISHED:
							str_size = String.format("%.2f %s", totalSize, donvi[total]);
							break;
						case Values.CANCELED:
							str_size = "";
							break;
						}

						long date = task.getCreateDate();
						String type = task.getType();
						cpTask = new CompactTask(id, folder, str_name, str_status, str_size,totalSize,date,type,s);
						modelAll.addElement(cpTask);
						if (task.getDownloadStatus() == Values.FINISHED)
							modelCompl.addElement(cpTask);
						else if (task.getDownloadStatus() == Values.DOWNLOADING 
								|| task.getDownloadStatus() == Values.ASSEMBLING
								|| task.getDownloadStatus() == Values.PAUSED
								|| task.getDownloadStatus() == Values.CANCELED)
							modelIncom.addElement(cpTask);
						else if (task.getDownloadStatus() == Values.READY)
							modelQueue.addElement(cpTask);
					}	
				}
				else {
					YTVideo v = DownloadManager.getInstance().getVideo(i);
					if(v != null && v.getDownloadStatus() != Values.DELETED) {
						String name = v.getFileName().toLowerCase();
						if (txtSearch.getText().equals("") || name.contains(txtSearch.getText().toLowerCase()))
						{
							int id = v.getT()[0].getTaskID();
							String folder = v.getT()[0].getSaveDirectory();
							String str_name = v.getFileName();
							String str_status = Values.State(v.getDownloadStatus());
							String str_size = new String();
							double totalSize = v.getFileSize();
							if (totalSize == -2) totalSize = v.getFileSize();
							long s = (long)totalSize;
							double downloadedSize = v.getDownloaded();

							String[] donvi = { "B", "KB", "MB", "GB", "TB" };
							int total = 0, download = 0;
							while (totalSize / 1024 > 1 && total < donvi.length) {
								totalSize /= 1024;
								total++;
							}
							while (downloadedSize / 1024 > 1 && download < donvi.length) {
								downloadedSize /= 1024;
								download++;
							}				
						
							switch (v.getDownloadStatus()) {
							case Values.READY:
								str_size = "";
								break;
							case Values.DOWNLOADING:
							case Values.ASSEMBLING:
								str_size = "";
								break;
							case Values.PAUSED: 
								str_size = String.format("%.2f%s / %.2f%s", downloadedSize, donvi[download], totalSize,
										donvi[total]);
								break;
							case Values.FINISHED:
								str_size = String.format("%.2f %s", totalSize, donvi[total]);
								break;
							case Values.CANCELED:
								str_size = "";
								break;				
							case Values.MERGING:
								str_size = "";
								break;
							case FFmpeg.FF_CONVERSION_FAILED:
							case FFmpeg.FF_LAUNCH_ERROR:
							case FFmpeg.FF_NOT_FOUND:
								str_size = "";
								break;
							case FFmpeg.FF_SUCCESS:
								str_size = String.format("%.2f %s", totalSize, donvi[total]);
								break;
							}

							long date = v.getT()[0].getCreateDate();
							int index = v.getFileName().lastIndexOf(".");
							String type = v.getFileName().substring(index);
							cpTask = new CompactTask(id, folder, str_name, str_status, str_size,totalSize,date,type,s);
							modelAll.addElement(cpTask);
							if (v.getDownloadStatus() == FFmpeg.FF_SUCCESS)
								modelCompl.addElement(cpTask);
							else if (v.getDownloadStatus() == Values.DOWNLOADING 
									|| v.getDownloadStatus() == Values.ASSEMBLING
									|| v.getDownloadStatus() == Values.PAUSED
									|| v.getDownloadStatus() == Values.CANCELED
									|| v.getDownloadStatus() == Values.MERGING
									|| v.getDownloadStatus() == Values.FINISHED
									|| v.getDownloadStatus() == FFmpeg.FF_CONVERSION_FAILED
									|| v.getDownloadStatus() == FFmpeg.FF_LAUNCH_ERROR
									|| v.getDownloadStatus() == FFmpeg.FF_NOT_FOUND
									)
								modelIncom.addElement(cpTask);
							else if (v.getDownloadStatus() == Values.READY)
								modelQueue.addElement(cpTask);
						}
					}
				}
			}catch (Exception e) { 
				e.printStackTrace(); 
			}
		}

		

		switch (tabbedPane.getSelectedIndex())
		{
		case 0:
			listView.setModel(modelAll);
			listView.setCellRenderer(new TaskRenderer());
			break;
		case 1:
			listViewCompl.setModel(modelCompl);
			listViewCompl.setCellRenderer(new TaskRenderer());
			break;
		case 2:
			listViewIncom.setModel(modelIncom);
			listViewIncom.setCellRenderer(new TaskRenderer());
			break;
		case 3:
			listViewQueue.setModel(modelQueue);
			listViewQueue.setCellRenderer(new TaskRenderer());
			break;
		}
		if (cbbSort.getSelectedItem().equals("Name")) sort_By_Name();
		else if (cbbSort.getSelectedItem().equals("Date Motified")) sort_By_Date();
		else if (cbbSort.getSelectedItem().equals("Size")) sort_By_Size();
		else if (cbbSort.getSelectedItem().equals("Type Item")) sort_By_Type();
	}
	
	public void newDownloadView(String url, long length)
	{
		try {
			NewDownload_View ndv = new NewDownload_View(this, url, length);
			ndv.setAlwaysOnTop(true);
			ndv.setVisible(true);
//			new NewDownload_View(this, url).setVisible(true);
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Main_View getthis()
	{
		return this;
	}
	public void open_File(String save_Directory, String save_File) {
		System.out.println(save_Directory + File.separator + save_File);
		File file = new File(save_Directory + File.separator + save_File);
		try {
			if (file.exists()) {
				Process pro = Runtime.getRuntime()
						.exec("rundll32 url.dll,FileProtocolHandler " + save_Directory + File.separator + save_File);
				pro.waitFor();
			} else {
				System.out.println("file does not exist");
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void create_Jpopupmenu() {
		JMenu sort = new JMenu("Sort by");
		JMenuItem sort_By_Name = new JMenuItem("Name");
		sort_By_Name.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sort_By_Name();
				cbbSort.setSelectedItem("Name");
			}
		});
		JMenuItem sort_By_Date = new JMenuItem("Date modified");
		sort_By_Date.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sort_By_Date();
				cbbSort.setSelectedItem("Date modified");
			}
		});
		JMenuItem sort_By_Size = new JMenuItem("Size");
		sort_By_Size.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {				
				sort_By_Size();
				cbbSort.setSelectedItem("Size");
			}
		});
		JMenuItem sort_By_Type = new JMenuItem("Type item");
		sort_By_Type.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sort_By_Type();
				cbbSort.setSelectedItem("Type item");
			}
		});
		sort.add(sort_By_Name);
		sort.add(sort_By_Date);
		sort.add(sort_By_Size);
		sort.add(sort_By_Type);
		jPopupMenu.add(sort);
		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CompactTask tmp = listView.getSelectedValue();
				if(tmp.getStatus().equals("FINISHED")) open_File(tmp.getFolder(), tmp.getName());
				else JOptionPane.showMessageDialog(getthis(), "File download not completed!", "Notification", JOptionPane.INFORMATION_MESSAGE);				
			}
		});
		JMenuItem openfolder = new JMenuItem("Open Folder");
		openfolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CompactTask tmp = listView.getSelectedValue();
				open_Folder(tmp.getFolder());
			}
		});
		JMenuItem pause = new JMenuItem("Pause");
		pause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DownloadManager.getInstance().pauseTask(listView.getSelectedValue().getId());
				
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});
		JMenuItem resume = new JMenuItem("Resume");
		resume.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int id = listView.getSelectedValue().getId();
				if(DownloadManager.getInstance().isTaskExist(id)) {
					view_Task_DownLoad view = new view_Task_DownLoad(id, getthis());
					view.setVisible(true);
				}
				else if(DownloadManager.getInstance().isVideoExist(id)){
					view_Task_DownLoad_Video view = new view_Task_DownLoad_Video(id, getthis());
					view.setVisible(true);
				}

			}
		});
		JMenuItem delete = new JMenuItem("Delete Download");
		delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CompactTask tmp = listView.getSelectedValue();

				if(tmp.getStatus().equals("DOWNLOADING") || tmp.getStatus().equals("ASSEMBLING") || tmp.getStatus().equals("MERGING")) {
					JOptionPane.showMessageDialog(getthis(), "You can't remove file when the file is downloading, assembling or merging", "Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				int result = JOptionPane.showConfirmDialog(getthis(), "Bạn có muốn xóa trên ổ đĩa không ", "Xác nhận",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					try {
						if (tmp.getStatus().equals("FINISHED")) {
							File file = new File(tmp.getFolder() + File.separator + tmp.getName());
							if (file.delete()) {
								System.out.println(file.getName() + " is deleted!");
							} else {
								System.out.println("Delete operation is failed.");
							}
							System.out.println(tmp.getId());
							DownloadManager.getInstance().setStatus(tmp.getId(), Values.DELETED);
							ReloadView();
						} else {
							DownloadManager.getInstance().delete(tmp.getId());
							ReloadView();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else if (result == JOptionPane.NO_OPTION) {
					System.out.println(tmp.getId());
					DownloadManager.getInstance().setStatus(tmp.getId(), Values.DELETED);
					ReloadView();
				}
			}
		});
		JMenuItem changeThreadCount = new JMenuItem("Change thread count");
		changeThreadCount.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CompactTask tmp = listView.getSelectedValue();
				switch(tmp.getStatus()) {
				case "READY":
				case "FINISHED":
				case "CANCELED":
				case "FF_CONVERSION_FAILED":
					List<String> item = new ArrayList<>();
					int min;
					if(DownloadManager.getInstance().isVideoExist(tmp.getId())) {
						min = 2;
					}
					else {
						min = Values.MIN_THREAD_COUNT;					
					}
					for(int i = min; i <= Values.MAX_THREAD_COUNT; i++)
						item.add(Integer.toString(i));	
					
					JComboBox<?> threadNumber = new JComboBox<>(item.toArray());					
					threadNumber.setSelectedIndex(DownloadManager.getInstance().getThreadCount(tmp.getId())-min);
					int result = JOptionPane.showConfirmDialog(getthis(), threadNumber, "Change thread count", JOptionPane.OK_CANCEL_OPTION);
					if(result == JOptionPane.OK_OPTION) {
						boolean res = DownloadManager.getInstance().setThreadCount(tmp.getId(), threadNumber.getSelectedIndex() + min);
						if(res) {
							JOptionPane.showMessageDialog(getthis(), "Change thread count success!", "Notification", JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(getthis(), "Change thread count fail!", "Notification", JOptionPane.INFORMATION_MESSAGE);
						}
					}
					break;
					default:{
						JOptionPane.showMessageDialog(getthis(), "You can only change the number of threads when the file is ready, finished, canceled or conversion failed", "Notification", JOptionPane.INFORMATION_MESSAGE);
						break;
					}
				}
					ReloadView();
			}
		});
		JMenuItem checkFile = new JMenuItem("Verify file");
		checkFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CompactTask tmp = listView.getSelectedValue();
				if(tmp.getStatus().equals("FINISHED")) {
					new VerifyFile(tmp.getId()).setVisible(true);;
				}
				else
					JOptionPane.showMessageDialog(getthis(), "You can only verify file when it has been finished", "Notification", JOptionPane.INFORMATION_MESSAGE);

			}
		});
		JMenuItem properties = new JMenuItem("Properties");
		properties.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CompactTask tmp = listView.getSelectedValue();
				String Message = "File Name : " + tmp.getName();
				Message += "\nLocation : " + tmp.getFolder();
				String s = tmp.getSize();
				Message += "\nSize : ";
				if(s  == "") Message += "0B";
				else {
					int index = s.indexOf("/");
					if(index != -1) Message += s.substring(0, index);
					else Message += s;
				}
				DownloadTask t = DownloadManager.getInstance().getTask(tmp.getId());
				if(t != null) {
					Message += "\nThread Count: " + t.getTaskThreadCount();
					Message += "\nUrl: " + t.getUrl();
				}
				else {
					YTVideo v = DownloadManager.getInstance().getVideo(tmp.getId());
					Message += "\nThread Count: " + (v.getT()[0].getTaskThreadCount() + v.getT()[1].getTaskThreadCount());
					Message += "\nUrl1: " + v.getT()[0].getUrl();
					Message += "\n\n\nUrl2: " + v.getT()[1].getUrl();
				}
				JTextArea area = new JTextArea(20, 50);
				area.setText(Message);
				area.setWrapStyleWord(true);
				area.setLineWrap(true);
				area.setCaretPosition(0);
				area.setEditable(false);
				JOptionPane.showMessageDialog(getthis(), new JScrollPane(area), "Properties", JOptionPane.INFORMATION_MESSAGE);

			}
		});
		jPopupMenu.add(open);
		jPopupMenu.add(openfolder);

		jPopupMenu.add(pause);
		jPopupMenu.add(resume);
		jPopupMenu.add(delete);
		jPopupMenu.add(changeThreadCount);
		jPopupMenu.add(checkFile);
		jPopupMenu.add(properties);
	}

	MouseListener mouseListenerListView = new MouseListener(){

		@Override
		public void mouseReleased(MouseEvent e) {			
				if (e.isPopupTrigger()) {
					int tmp = listView.locationToIndex(e.getPoint());
					listView.setSelectedIndex(tmp);
					show_Popup_Jlist(e);
				}
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (e.getClickCount() == 2) {
						CompactTask tmp = listView.getSelectedValue();
						if(tmp.getStatus().equals("FINISHED")) open_File(tmp.getFolder(), tmp.getName());
						else JOptionPane.showMessageDialog(getthis(), "File download not completed!", "Notification", JOptionPane.INFORMATION_MESSAGE);
					}

				}

		}

		@Override
		public void mousePressed(MouseEvent e) { }

		@Override
		public void mouseExited(MouseEvent e) { }

		@Override
		public void mouseEntered(MouseEvent e) { }

		@Override
		public void mouseClicked(MouseEvent e) { }
	};

	public void show_Popup_Jlist(MouseEvent e) {

		jPopupMenu.show(listView, e.getX(), e.getY());
	}

	public void open_Folder(String save_Directory) {
		File directory = new File(save_Directory);
		try {
			Desktop.getDesktop().open(directory);
		} 
		catch (IOException e) { e.printStackTrace(); }
	}
	public void sort_By_Size()
	{
		ListModel<CompactTask> model = listView.getModel();
		int n = model.getSize();
		CompactTask[] data = new CompactTask[n];
		for(int i = 0 ; i < n ; i++)
		{
			data[i] = (CompactTask) model.getElementAt(i);
		}
		bubbleSort_for_Size(data, n);
		listView.setListData(data);
		
	}
	  void bubbleSort_for_Size(CompactTask arr[], int n) {
	        int i, j;
	        CompactTask temp;
	        boolean swapped;
	        for (i = 0; i < n - 1; i++) {
	            swapped = false;
	            for (j = 0; j < n - i - 1; j++) {
	                if (arr[j].getTotal_size() > arr[j+1].getTotal_size()) {
	                    temp = arr[j];
	                    arr[j] = arr[j + 1];
	                    arr[j + 1] = temp;
	                    swapped = true;
	                }
	            }
	            if (swapped == false)
	                break;
	        }
	    }
	  public void sort_By_Name()
	  {
		  ListModel<CompactTask> model = listView.getModel();
			int n = model.getSize();
			CompactTask[] data = new CompactTask[n];
			for(int i = 0 ; i < n ; i++)
			{
				data[i] = (CompactTask) model.getElementAt(i);
			}
			Arrays.sort(data,(a,b)->a.getName().compareTo(b.getName()));
			listView.setListData(data);
	  }
	  public void sort_By_Date()
	  {
		  ListModel<CompactTask> model = listView.getModel();
			int n = model.getSize();
			CompactTask[] data = new CompactTask[n];
			for(int i = 0 ; i < n ; i++)
			{
				data[i] = (CompactTask) model.getElementAt(i);
			}
			bubbleSort_for_Day(data, n);
			listView.setListData(data);
	  }
	  void bubbleSort_for_Day(CompactTask arr[], int n) {
	        int i, j;
	        CompactTask temp;
	        boolean swapped;
	        for (i = 0; i < n - 1; i++) {
	            swapped = false;
	            for (j = 0; j < n - i - 1; j++) {
	                if (arr[j].getDate() > arr[j+1].getDate()) {
	                    // swap arr[j] và arr[j+1]
	                    temp = arr[j];
	                    arr[j] = arr[j + 1];
	                    arr[j + 1] = temp;
	                    swapped = true;
	                }
	            }
	            if (swapped == false)
	                break;
	        }
	    }
	  public void sort_By_Type()
	  {
		  ListModel<CompactTask> model = listView.getModel();
			int n = model.getSize();
			CompactTask[] data = new CompactTask[n];
			for(int i = 0 ; i < n ; i++)
			{
				data[i] = (CompactTask) model.getElementAt(i);
			}
			Arrays.sort(data,(a,b)->a.getType_File().compareTo(b.getType_File()));
			listView.setListData(data);
	  }

	  
	public void delete_Folder(String path)
	{
		File file_Source = new File(path);
		if(!file_Source.exists())
		{
			System.out.println("non exist");
		}
		else if (file_Source.isDirectory())
		{
			File[] list_File = file_Source.listFiles();
			for(File file : list_File)
			{
				if(file.isFile())
				{
					file.delete();
				}
				else
				{
					delete_Folder(file.getAbsolutePath());
					file.delete();

				}
			}
		}
	}

}
