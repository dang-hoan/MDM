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
import BLL.DownFile.YTVideo;
import BLL.DownFile.speed_Download;
import BLL.VideoConversion.FFmpeg;

public class view_Task_DownLoad_Video extends JFrame {

	private static final long serialVersionUID = 1L;

	private DownloadManager downloadManager = DownloadManager.getInstance();

	private speed_Download speed_Download;
	private speed_Download speed_Download2;
	private JProgressBar[] array_JProgressBar;
	private JProgressBar[] array_JProgressBar2;

	private JLabel labNotice;

	private JPanel contentPane;
	private JLabel jlb_NameFile;
	private JLabel jlb_Speed;

	private YTVideo v;
	private int ThreadCount;
	private Main_View _Main_View;
	/**
     * Launch the application.
     */



   /**
     * Create the frame.
     */
	public view_Task_DownLoad_Video(int TaskID, Main_View _Main_View)
	{
		YTVideo v = downloadManager.getVideo(TaskID);
		this.v = v;
		ThreadCount = v.getT()[0].getThreadCount() + v.getT()[1].getThreadCount();

		if(v.getT()[0].getFileSize() == -1) {
			this.array_JProgressBar = new JProgressBar[1];
			this.array_JProgressBar2 = new JProgressBar[1];
			ThreadCount = 1;
		}
		else {
			this.array_JProgressBar = new JProgressBar[v.getT()[0].getThreadCount()];
			this.array_JProgressBar2 = new JProgressBar[v.getT()[1].getThreadCount()];
		}

		this.speed_Download = new speed_Download();
		this.speed_Download2 = new speed_Download();

		initComponent();

		v.getT()[0].setSpeed_Download(this.speed_Download);
		v.getT()[0].setJProgressBar(this.array_JProgressBar);
		v.getT()[1].setSpeed_Download(this.speed_Download2);
		v.getT()[1].setJProgressBar(this.array_JProgressBar2);
		this._Main_View=_Main_View;
		this.setVisible(true);
		start_Download();
	}

	/**
	 * @wbp.parser.constructor
	 */
	public view_Task_DownLoad_Video(VideoPopupitem videoItem, String folder, int number_Thread, Main_View _Main_View) {

		this.speed_Download = new speed_Download();
		this.speed_Download2 = new speed_Download();
		ThreadCount = number_Thread;

		int size1 = number_Thread/2;
		int size2 = number_Thread-size1;

		if(videoItem.getLen1() == -1) {
			this.array_JProgressBar = new JProgressBar[1];
			this.array_JProgressBar2 = new JProgressBar[1];
			ThreadCount = 1;
		}
		else {
			this.array_JProgressBar = new JProgressBar[size1];
			this.array_JProgressBar2 = new JProgressBar[size2];
		}

		
		YTVideo v = downloadManager.addVideo(videoItem, folder, size1, size2, false, array_JProgressBar, speed_Download, array_JProgressBar2, speed_Download2);
		this.v = v;

		this._Main_View = _Main_View;

		initComponent();
		this.setVisible(true);
		start_Download();
	}

	public void start_Download() {
		try {
			downloadManager.startTask(v.getT()[0].getTaskID());
			get_Speed();
			_Main_View.ReloadView();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getStackTrace(), "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void add_array_JProgressBar(int ThreadCount)
	{
		int tmp = (this.getWidth()-25)/ThreadCount;
		if(v.getT()[0].getFileSize() == -1)
		{
			array_JProgressBar[0] = new JProgressBar();
			array_JProgressBar2[0] = new JProgressBar();
			array_JProgressBar[0].setBounds(5, 100, tmp, 20);
			array_JProgressBar[0].setIndeterminate(true);
			array_JProgressBar2[0].setIndeterminate(true);
			array_JProgressBar[0].setStringPainted(true);
			array_JProgressBar[0].setString("DOWNLOADING...");
			getContentPane().add(this.array_JProgressBar[0]);
		}
		else
		{
			boolean str = (ThreadCount > 24)? false : true; //số luồng nhiều thì không vẽ phần trăm để đỡ rối mắt
			for(int i= 0;i<array_JProgressBar.length;i++)
			{
				array_JProgressBar[i] = new JProgressBar();
				array_JProgressBar[i].setBounds(tmp*i+5, 100, tmp, 20);
				array_JProgressBar[i].setStringPainted(str);
				getContentPane().add(this.array_JProgressBar[i]);
			}
			int j = 0;
			for(int i= array_JProgressBar.length;i<ThreadCount;i++)
			{
				array_JProgressBar2[j] = new JProgressBar();
				array_JProgressBar2[j].setBounds(tmp*i+5, 100, tmp, 20);
				array_JProgressBar2[j].setStringPainted(str);
				getContentPane().add(this.array_JProgressBar2[j++]);
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
					if(v.getDownloadStatus() == Values.DOWNLOADING)
					{
						long tmp = speed_Download.Get_Seze_1s() + speed_Download2.Get_Seze_1s();

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
						if(v.getT()[0].getFileSize() > 0) {
							long second = (tmp != 0)? (v.getFileSize()-v.getDownloadedSize())/tmp : 0;
							remainTime = calculateTime(second);
							if(remainTime != "") remainTime = " còn " + remainTime;
						}

						jlb_Speed.setText(s+"/s" + remainTime);

						speed_Download.set_Size_Download();
						speed_Download2.set_Size_Download();
//						_Main_View.ReloadView();
						Thread.sleep(1000);
					}
					if(v.getDownloadStatus() == Values.ASSEMBLING) {
						jlb_Speed.setText("Đang ghép file...");
						_Main_View.ReloadView();
					}
					if(v.getDownloadStatus() == Values.PAUSED) {
						jlb_Speed.setText("Đã dừng");
						if(v.getT()[0].getFileSize()==-1)
						{
							array_JProgressBar[0].setIndeterminate(false);
							array_JProgressBar[0].setString("Đã dừng");
						}
						break;
					}
					if(v.getDownloadStatus() == Values.CANCELED) {
						break;
					}
					if(v.getDownloadStatus() == Values.MERGING) {
//						System.out.println("merge...............");
						jlb_Speed.setText("Đang ghép âm thanh vào video...");
						_Main_View.ReloadView();
					}
						
					switch(v.getDownloadStatus()) {
						case FFmpeg.FF_NOT_FOUND:{
							jlb_Speed.setText("Không tìm thấy ffmpeg, hãy cài đặt ffmpeg và thử lại!");
							if(v.getT()[0].getFileSize()==-1)
							{
								array_JProgressBar[0].setIndeterminate(false);
								array_JProgressBar[0].setString("Lỗi khi ghép file");
							}
							JOptionPane.showMessageDialog(view_Task_DownLoad_Video.this, "File \"" + v.getFileName() + "\" download fail!", "Notification", JOptionPane.INFORMATION_MESSAGE);
							_Main_View.ReloadView();
							return;
						}
						case FFmpeg.FF_LAUNCH_ERROR:{
							jlb_Speed.setText("Lỗi khi chạy ffmpeg!");
							if(v.getT()[0].getFileSize()==-1)
							{
								array_JProgressBar[0].setIndeterminate(false);
								array_JProgressBar[0].setString("Lỗi khi ghép file");
							}
							JOptionPane.showMessageDialog(view_Task_DownLoad_Video.this, "File \"" + v.getFileName() + "\" download fail!", "Notification", JOptionPane.INFORMATION_MESSAGE);
							_Main_View.ReloadView();
							return;
						}
						case FFmpeg.FF_SUCCESS:{
							String time = calculateTime(v.getDownloadTime()/1000);
							if(time.equals("")) time = "gần 1 giây";
							jlb_Speed.setText("Hoàn thành, " + "tổng thời gian tải: " + time);
							if(v.getT()[0].getFileSize()==-1)
							{
								array_JProgressBar[0].setIndeterminate(false);
								array_JProgressBar[0].setString("Hoàn Thành");
							}
							jlb_NameFile.setText(v.getFileName());
							labNotice.setText("");
							JOptionPane.showMessageDialog(view_Task_DownLoad_Video.this, "File \"" + v.getFileName() + "\" download completed!", "Notification", JOptionPane.INFORMATION_MESSAGE);
							_Main_View.ReloadView();
							return;
						}
						case FFmpeg.FF_CONVERSION_FAILED:{
							jlb_Speed.setText("Ghép âm thanh vào video không thành công!");
							if(v.getT()[0].getFileSize()==-1)
							{
								array_JProgressBar[0].setIndeterminate(false);
								array_JProgressBar[0].setString("Lỗi khi ghép file");
							}
							JOptionPane.showMessageDialog(view_Task_DownLoad_Video.this, "File \"" + v.getFileName() + "\" download fail!", "Notification", JOptionPane.INFORMATION_MESSAGE);
							_Main_View.ReloadView();
							return;
						}
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
			second = second - hour*3600 - minute*60;
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

		JButton btn_Play = new JButton("");
		btn_Play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start_Download();
			}
		});
		btn_Play.setIcon(new ImageIcon(view_Task_DownLoad_Video.class.getResource("/View/icon/play.png")));

		JButton btn_Pause = new JButton("");
		btn_Pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					downloadManager.pauseTask(v.getT()[0].getTaskID());
					_Main_View.ReloadView();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btn_Pause.setIcon(new ImageIcon(view_Task_DownLoad_Video.class.getResource("/View/icon/pause.png")));

		JButton btn_Huy =
				new JButton("");
		btn_Huy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					downloadManager.cancelTask(v.getT()[0].getTaskID());
					_Main_View.ReloadView();
					close_Frame();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btn_Huy.setIcon(new ImageIcon(view_Task_DownLoad_Video.class.getResource("/View/icon/x.png")));

		JButton btnVerify = new JButton("Verify File");
		btnVerify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(v.getT()[0].getDownloadStatus() != Values.FINISHED) {
					labNotice.setText("The file hasn't been downloaded yet!");
					return;
				}
				else {
					labNotice.setText("");
					new VerifyFile(v.getT()[0].getTaskID()).setVisible(true);
				}
			}
		});
		btnVerify.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GroupLayout gl_panel_2_1 = new GroupLayout(panel_2_1);
		gl_panel_2_1.setHorizontalGroup(
			gl_panel_2_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2_1.createSequentialGroup()
					.addGap(67)
					.addComponent(btnVerify, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btn_Play, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btn_Pause, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btn_Huy, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(181, Short.MAX_VALUE))
		);
		gl_panel_2_1.setVerticalGroup(
			gl_panel_2_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_2_1.createSequentialGroup()
					.addContainerGap(19, Short.MAX_VALUE)
					.addGroup(gl_panel_2_1.createParallelGroup(Alignment.LEADING)
						.addComponent(btnVerify, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_2_1.createParallelGroup(Alignment.LEADING, false)
							.addComponent(btn_Huy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btn_Play, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
							.addComponent(btn_Pause, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
		jlb_NameFile.setText(v.getFileName());
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

		add_array_JProgressBar(ThreadCount);
	}
}