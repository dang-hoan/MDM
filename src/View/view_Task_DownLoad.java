package View;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import BLL.Utils;
import BLL.Values;
import BLL.DownFile.DownloadManager;
import BLL.DownFile.DownloadTask;
import BLL.DownFile.speed_Download;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class view_Task_DownLoad extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private DownloadManager downloadManager = DownloadManager.getInstance();
	private DownloadTask task;
	
	private speed_Download speed_Download;
	private JProgressBar[] array_JProgressBar;
	
	private JPanel contentPane;
	private JLabel jlb_NameFile;
	private JLabel jlb_Speed;
	
	private Main_View _Main_View;
	/**
     * Launch the application.
     */



   /**
     * Create the frame.
     */
	public view_Task_DownLoad(int TaskID, Main_View _Main_View) 
	{		
		this.task = downloadManager.getTask(TaskID);
		if(task.getFileSize() == -1) {
			this.array_JProgressBar = new JProgressBar[1];
		}
		else {
			this.array_JProgressBar = new JProgressBar[task.getThreadCount()];
		}
		
		this.speed_Download = new speed_Download();
		initComponent();

		task.setSpeed_Download(this.speed_Download);
		task.setJProgressBar(this.array_JProgressBar);	
		this._Main_View=_Main_View;
		start_Download();
	}

	/**
	 * @wbp.parser.constructor
	 */
	public view_Task_DownLoad(String url, String folder, String FileName, int number_Thread, Main_View _Main_View) {
		
		this.speed_Download = new speed_Download();
		
		if(Utils.getFileLength(url) == -1)
			this.array_JProgressBar = new JProgressBar[1];
		else
			this.array_JProgressBar = new JProgressBar[number_Thread];
		
		this.task = downloadManager.addTask(url, folder, FileName, number_Thread, false, this.array_JProgressBar,this.speed_Download);
		
		this._Main_View = _Main_View;
		
		initComponent();		
		start_Download();	
	}

	public void start_Download() {
		try {
			downloadManager.startTask(task.getTaskID());
			get_Speed();
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getStackTrace(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void add_array_JProgressBar(int ThreadCount)
	{
		int tmp = (this.getWidth()-25)/ThreadCount;
		if(array_JProgressBar.length==1)
		{
			array_JProgressBar[0] = new JProgressBar();
			array_JProgressBar[0].setBounds(5, 100, tmp, 20);
			array_JProgressBar[0].setIndeterminate(true);
			array_JProgressBar[0].setStringPainted(true);
			array_JProgressBar[0].setString("DOWNLOADING...");
			getContentPane().add(this.array_JProgressBar[0]);	
		}
		else
		{
			for(int i= 0;i<array_JProgressBar.length;i++)
			{
			
				array_JProgressBar[i] = new JProgressBar();
				array_JProgressBar[i].setBounds(tmp*i+5, 100, tmp, 20);
				array_JProgressBar[i].setStringPainted(true);
				getContentPane().add(this.array_JProgressBar[i]);	
			}
		}

	}

	public void get_Speed() {
		Thread thread = new Thread() {
			@Override
			public void run()
			{
				while(true)
				{
				try {
					if(speed_Download.get_Check()==Values.DOWNLOADING)
					{
						int tmp = speed_Download.Get_Seze_1s();
						
						String s = ""; int MB = 1024*1024, KB = 1024;
						if (tmp < 0)
							s = "---";
						if (tmp > MB) {			// > 1MB
							s = String.format("%.1f MB", (float) tmp / MB);
						} else if (tmp > KB) {	// > 1KB
							s = String.format("%.1f KB", (float) tmp / KB);
						} else {
							s = String.format("%d B", (int) tmp);
						}
						
						String remainTime = "";
						if(task.getFileSize() > 0) {
							long second = (tmp != 0)? (task.getFileSize()-task.getDownloadedSize())/tmp : 0;
							long hour = 0, minute = 0;
							if(second > 3600) {		// > 1 giờ
								hour = second/3600; 
								minute = (second%3600)/60;
							} else if(second > 60) {// > 1 phút
								minute = second/60;
								second = second%60;
							}
							if(hour > 0) remainTime += hour + " giờ ";
							if(minute > 0) remainTime += minute + " phút ";
							if(second > 0) remainTime += second + " giây";
							if(remainTime != "") remainTime = " còn " + remainTime;
						}
						
						jlb_Speed.setText(s+"/s" + remainTime);
						
						speed_Download.set_Size_Download();
						Thread.sleep(1000);
					}
					if(speed_Download.get_Check() == Values.ASSEMBLING) {
						jlb_Speed.setText("Đang ghép file...");
					}
					if(speed_Download.get_Check() == Values.FINISHED) {
						jlb_Speed.setText("Hoàn thành");
						jlb_NameFile.setText(task.getSaveName());
						_Main_View.ReloadView();
						break;
					}
					if(speed_Download.get_Check() == Values.PAUSED) {
						jlb_Speed.setText("Đã dừng");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					speed_Download.set_Size_Download();
				}
				}

			}

		};
		thread.start();
	}
	public void close_Frame()
	{
		this.dispose();
	}
	
	public void initComponent() {
		setTitle("Task Download");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLocationRelativeTo(null);

		setContentPane(contentPane);

		JPanel panel = new JPanel();

		JPanel panel_1 = new JPanel();

		JLabel lblTc = new JLabel("Tốc độ :");

		jlb_Speed = new JLabel("New label");
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(gl_panel_1.createParallelGroup(Alignment.LEADING).addGap(0, 225, Short.MAX_VALUE)
				.addGroup(gl_panel_1.createSequentialGroup().addContainerGap().addComponent(lblTc)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(jlb_Speed, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE).addContainerGap()));
		gl_panel_1.setVerticalGroup(gl_panel_1.createParallelGroup(Alignment.LEADING).addGap(0, 38, Short.MAX_VALUE)
				.addGroup(Alignment.TRAILING,
						gl_panel_1.createSequentialGroup().addContainerGap(13, Short.MAX_VALUE).addGroup(gl_panel_1
								.createParallelGroup(Alignment.BASELINE).addComponent(lblTc).addComponent(jlb_Speed))
								.addContainerGap()));
		panel_1.setLayout(gl_panel_1);

		JPanel panel_2 = new JPanel();

		JLabel lblTinTrinhDownload = new JLabel("Tiến Trinh DownLoad");
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addGap(188)
					.addComponent(lblTinTrinhDownload, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(256, Short.MAX_VALUE))
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap(13, Short.MAX_VALUE)
					.addComponent(lblTinTrinhDownload)
					.addContainerGap())
		);
		panel_2.setLayout(gl_panel_2);

		JPanel panel_2_1 = new JPanel();

		JButton btn_Play = new JButton("");
		btn_Play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					downloadManager.startTask(task.getTaskID());
					get_Speed();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btn_Play.setIcon(new ImageIcon(view_Task_DownLoad.class.getResource("/View/icon/play.png")));

		JButton btn_Pause = new JButton("");
		btn_Pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					downloadManager.pauseTask(task.getTaskID());
					_Main_View.ReloadView();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btn_Pause.setIcon(new ImageIcon(view_Task_DownLoad.class.getResource("/View/icon/pause.png")));

		JButton btn_Huy = 
				new JButton("");
		btn_Huy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					downloadManager.cancelTask(task.getTaskID());
					_Main_View.ReloadView();
					close_Frame();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btn_Huy.setIcon(new ImageIcon(view_Task_DownLoad.class.getResource("/View/icon/x.png")));
		GroupLayout gl_panel_2_1 = new GroupLayout(panel_2_1);
		gl_panel_2_1.setHorizontalGroup(
			gl_panel_2_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2_1.createSequentialGroup()
					.addGap(173)
					.addComponent(btn_Play, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btn_Pause, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btn_Huy, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(179, Short.MAX_VALUE))
		);
		gl_panel_2_1.setVerticalGroup(
			gl_panel_2_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_2_1.createSequentialGroup()
					.addContainerGap(42, Short.MAX_VALUE)
					.addGroup(gl_panel_2_1.createParallelGroup(Alignment.LEADING, false)
						.addComponent(btn_Huy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btn_Play, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
						.addComponent(btn_Pause, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);
		panel_2_1.setLayout(gl_panel_2_1);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
					.addContainerGap())
				.addComponent(panel_2_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
					.addComponent(panel_2_1, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE))
		);

		JLabel lblNewLabel = new JLabel("Tên File :");

		jlb_NameFile = new JLabel("New label");
		jlb_NameFile.setText(task.getSaveName());
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup().addContainerGap().addComponent(lblNewLabel)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(jlb_NameFile, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE).addContainerGap()));
		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING,
				gl_panel.createSequentialGroup().addContainerGap(13, Short.MAX_VALUE).addGroup(gl_panel
						.createParallelGroup(Alignment.BASELINE).addComponent(lblNewLabel).addComponent(jlb_NameFile))
						.addContainerGap()));
		panel.setLayout(gl_panel);
		contentPane.setLayout(gl_contentPane);
		
		add_array_JProgressBar(task.getThreadCount());
	}

}