package View;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

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
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class Main_View extends JFrame {

	private JPanel contentPane;
	JScrollPane scrollPaneListView = new JScrollPane();
	JList<CompactTask> listView = new JList<>();
	JPopupMenu jPopupMenu;
	DownloadTask task;
	
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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("New Download");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					newDownloadView("", -2);					
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
	}

	public synchronized void ReloadView()
	{
		DefaultListModel<CompactTask> model = new DefaultListModel<>();
		for(int i = 0; i < Values.Task_ID_COUNTER; i++)
		{
			try {
				task = DownloadManager.getInstance().getTask(i);	
				if (task != null && task.getDownloadStatus() != Values.DELETED)
				{
					int id = task.getTaskID();
					String folder = task.getSaveDirectory();
					String str_name = task.getSaveName();
					URL urlicon = null;
					String str_status = Values.State(task.getDownloadStatus());
					String str_size = new String();
					double totalSize = task.getFileSize();
					if (totalSize == -1) totalSize = task.getCurrentSize();
					long s = (long)totalSize;
					double downloadedSize = task.getCurrentSize();

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
					urlicon = Main_View.class.getResource("/View/icon/ready.png");
					break;
				case Values.DOWNLOADING:
				case Values.ASSEMBLING:
					str_size = "";
					urlicon = Main_View.class.getResource("/View/icon/dloading.png");
					break;
				case Values.PAUSED: 
					str_size = String.format("%.2f%s / %.2f%s", downloadedSize, donvi[download], totalSize,
							donvi[total]);
					urlicon = Main_View.class.getResource("/View/icon/dloading.png");
					break;
				case Values.FINISHED:
					str_size = String.format("%.2f %s", totalSize, donvi[total]);
					urlicon = Main_View.class.getResource("/View/icon/completed.png");
					break;
				case Values.CANCELED:
					str_size = "";
					urlicon = Main_View.class.getResource("/View/icon/canceled.png");
					break;
				}

				long date = task.getCreateDate();
				String type = str_name.substring(str_name.lastIndexOf('.') + 1).trim();
				model.addElement(new CompactTask(id, folder, str_name, urlicon, str_status, str_size,totalSize,date,type,s));
				// i vừa là index cũng vừa là id của task
				// vì ở trên ta đã getTask(i) rồi, nên chắc chắn i == task.getId
				// nên ko cần tạo biến id = task.getId, bởi vì i chính là ID
				}
				else {
					YTVideo v = DownloadManager.getInstance().getVideo(i);
					if(v != null && v.getDownloadStatus() != Values.DELETED) {
						int id = v.getT()[0].getTaskID();
						String folder = v.getT()[0].getSaveDirectory();
						String str_name = v.getFileName();
						URL urlicon = null;
						String str_status = Values.State(v.getDownloadStatus());
						String str_size = new String();
						double totalSize = v.getFileSize();
						if (totalSize == -2) totalSize = v.getCurrentSize();
						long s = (long)totalSize;
						double downloadedSize = v.getCurrentSize();

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
						urlicon = Main_View.class.getResource("/View/icon/ready.png");
						break;
					case Values.DOWNLOADING:
					case Values.ASSEMBLING:
						str_size = "";
						urlicon = Main_View.class.getResource("/View/icon/dloading.png");
						break;
					case Values.PAUSED: 
						str_size = String.format("%.2f%s / %.2f%s", downloadedSize, donvi[download], totalSize,
								donvi[total]);
						urlicon = Main_View.class.getResource("/View/icon/dloading.png");
						break;
					case Values.FINISHED:
						str_size = String.format("%.2f %s", totalSize, donvi[total]);
						urlicon = Main_View.class.getResource("/View/icon/completed.png");
						break;
					case Values.CANCELED:
						str_size = "";
						urlicon = Main_View.class.getResource("/View/icon/canceled.png");
						break;
					case Values.MERGING:
						str_size = "";
						urlicon = Main_View.class.getResource("/View/icon/merge.png");
						break;
					case FFmpeg.FF_CONVERSION_FAILED:
					case FFmpeg.FF_LAUNCH_ERROR:
					case FFmpeg.FF_NOT_FOUND:
						str_size = "";
						urlicon = Main_View.class.getResource("/View/icon/canceled.png");
						break;
					case FFmpeg.FF_SUCCESS:
						str_size = String.format("%.2f %s", totalSize, donvi[total]);
						urlicon = Main_View.class.getResource("/View/icon/completed.png");
						break;
					}

					long date = v.getT()[0].getCreateDate();
					String type = str_name.substring(str_name.lastIndexOf('.') + 1).trim();
					model.addElement(new CompactTask(id, folder, str_name, urlicon, str_status, str_size,totalSize,date,type,s));
					}
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(model != null) {
			listView.setModel(model);
			listView.setCellRenderer(new TaskRenderer());
		}
		add_Even_Mouse_JList();
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
			}
		});
		JMenuItem sort_By_Date = new JMenuItem("Date modified");
		sort_By_Date.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sort_By_Date();
			}
		});
		JMenuItem sort_By_Size = new JMenuItem("Size");
		sort_By_Size.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {				
				sort_By_Size();
			}
		});
		JMenuItem sort_By_Type = new JMenuItem("Type item");
		sort_By_Type.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sort_By_Type();
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
		jPopupMenu.add(properties);
	}

	public void add_Even_Mouse_JList() {
		listView.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

				if (e.isPopupTrigger()) {
					int tmp = listView.locationToIndex(e.getPoint());
					listView.setSelectedIndex(tmp);
					show_Popup_Jlist(e);
				}
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (e.getClickCount() == 2) {
						DownloadTask task = DownloadManager.getInstance().getTask(listView.getSelectedValue().getId());
						if (task.getDownloadStatus() == Values.FINISHED) {
							open_File(task.getSaveDirectory(), task.getSaveName());
						}
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
		});
	}

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
	                    // swap arr[j] và arr[j+1]
	                    temp = arr[j];
	                    arr[j] = arr[j + 1];
	                    arr[j + 1] = temp;
	                    swapped = true;
	                }
	            }

	            // Nếu không có phần tử nào để hoán đổi
	            // bên trong vòng lặp thì Break
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

	            // Nếu không có phần tử nào để hoán đổi
	            // bên trong vòng lặp thì Break
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
