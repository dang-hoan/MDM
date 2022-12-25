package View;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import BLL.Values;
import BLL.DownFile.DownloadManager;
import BLL.DownFile.DownloadTask;
import BLL.DownFile.speed_Download;

public class view_Task_DownLoad extends JFrame {

	private static final long serialVersionUID = 1L;

	private DownloadManager downloadManager = DownloadManager.getInstance();
	private DownloadTask task;

	private speed_Download speed_Download;
	private JProgressBar[] array_JProgressBar;

	private JLabel labNotice;

	private JPanel contentPane;
	private JLabel jlb_NameFile;
	private JLabel jlb_Speed;

	private Main_View _Main_View;

	private JButton btn_Play;
	private JButton btn_Pause;
	private JButton btn_Huy;
	private JButton btn_OK;
	private JButton btn_Verify;
	/**
     * Launch the application.
     */



   /**
     * Create the frame.
     */
	public view_Task_DownLoad(int TaskID, Main_View _Main_View)
	{
		System.out.println("t id:"+TaskID);
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
		this.setVisible(true);
		start_Download();
	}

	/**
	 * @wbp.parser.constructor
	 */
	public view_Task_DownLoad(String url, String folder, String FileName, int number_Thread, Main_View _Main_View,long size) {

		this.speed_Download = new speed_Download();

		if( size == -1)
			this.array_JProgressBar = new JProgressBar[1];
		else
			this.array_JProgressBar = new JProgressBar[number_Thread];

		this.task = downloadManager.addTask(url, folder, FileName, number_Thread, size, false, this.array_JProgressBar,this.speed_Download, false);

		this._Main_View = _Main_View;

		initComponent();
		this.setVisible(true);
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
		if(task.getFileSize() == -1)
		{
			array_JProgressBar[0] = new JProgressBar();
			array_JProgressBar[0].setBounds(5, 100, tmp, 20);
			array_JProgressBar[0].setIndeterminate(true);
			array_JProgressBar[0].setStringPainted(true);
			array_JProgressBar[0].setString("Đang tải xuống...");
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
						long tmp = speed_Download.Get_Seze_1s();

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
							remainTime = calculateTime(second);
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
						String time = calculateTime(task.getDownloadTime()/1000);
						if(time.equals("")) time = "gần 1 giây";
						jlb_Speed.setText("Hoàn thành, " + "tổng thời gian tải: " + time);
						jlb_NameFile.setText(task.getSaveName());
						labNotice.setText("");
						disable_Button();
						if(array_JProgressBar.length==1)
						{
							array_JProgressBar[0].setIndeterminate(false);
							array_JProgressBar[0].setString("Hoàn Thành");
							}
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

	public String calculateTime(long second) {
		String remainTime = "";
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
		return remainTime;
	}

	public void close_Frame()
	{
		this.dispose();
	}

	public void initComponent() {
		setTitle("Task Download");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(2)
					.addComponent(lblTc)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(jlb_Speed, GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap(13, Short.MAX_VALUE)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTc)
						.addComponent(jlb_Speed))
					.addContainerGap())
		);
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

		btn_Play = new JButton("");
		btn_Play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				downloadManager.startTask(task.getTaskID());
				if(array_JProgressBar.length==1)
				{
					array_JProgressBar[0].setIndeterminate(true);
					array_JProgressBar[0].setString("Đang tải xuống");
				}
			}
		});
		btn_Play.setIcon(new ImageIcon(view_Task_DownLoad.class.getResource("/View/icon/play.png")));

		btn_Pause = new JButton("");
		btn_Pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					downloadManager.pauseTask(task.getTaskID());
					if(array_JProgressBar.length==1)
					{
						array_JProgressBar[0].setIndeterminate(false);
						array_JProgressBar[0].setString("Tạm dừng");
					}
					_Main_View.ReloadView();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btn_Pause.setIcon(new ImageIcon(view_Task_DownLoad.class.getResource("/View/icon/pause.png")));

		btn_Huy = new JButton("");
		btn_Huy.addActionListener(new ActionListener() {
			@Override
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

		btn_Verify = new JButton("Verify File");
		btn_Verify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(task.getDownloadStatus() != Values.FINISHED) {
					labNotice.setText("The file hasn't been downloaded yet!");
					return;
				}
				else {
					labNotice.setText("");
					new VerifyFile(task.getTaskID()).setVisible(true);
				}
			}
		});
		btn_Verify.setFont(new Font("Tahoma", Font.PLAIN, 14));

		btn_OK = new JButton("OK");
		btn_OK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btn_OK.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GroupLayout gl_panel_2_1 = new GroupLayout(panel_2_1);
		gl_panel_2_1.setHorizontalGroup(
			gl_panel_2_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2_1.createSequentialGroup()
					.addGap(99)
					.addComponent(btn_OK, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btn_Play, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btn_Pause, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btn_Huy, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btn_Verify, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(75, Short.MAX_VALUE))
		);
		gl_panel_2_1.setVerticalGroup(
			gl_panel_2_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_2_1.createSequentialGroup()
					.addContainerGap(19, Short.MAX_VALUE)
					.addGroup(gl_panel_2_1.createParallelGroup(Alignment.LEADING)
						.addComponent(btn_OK, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_2_1.createParallelGroup(Alignment.TRAILING)
							.addComponent(btn_Verify, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_panel_2_1.createParallelGroup(Alignment.LEADING, false)
								.addComponent(btn_Huy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btn_Play, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
								.addComponent(btn_Pause, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
					.addContainerGap())
		);
		panel_2_1.setLayout(gl_panel_2_1);

		labNotice = new JLabel("");
		labNotice.setForeground(Color.RED);
		labNotice.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addComponent(panel_2_1, GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(labNotice, GroupLayout.PREFERRED_SIZE, 244, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(322, Short.MAX_VALUE))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
					.addGap(27)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
					.addComponent(labNotice)
					.addPreferredGap(ComponentPlacement.RELATED)
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

		try {
			this.setIconImage(ImageIO.read(getClass().getResourceAsStream("/View/icon/app.png")));

		} catch (IOException e2) {
			e2.printStackTrace();
		}
		btn_OK.setEnabled(false);

		add_array_JProgressBar(task.getThreadCount());
	}
	public void disable_Button() {
		this.btn_Huy.setEnabled(false);
		this.btn_Pause.setEnabled(false);
		this.btn_Play.setEnabled(false);
		this.btn_OK.setEnabled(true);
	}
}