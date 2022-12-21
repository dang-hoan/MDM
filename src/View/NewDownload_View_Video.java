package View;

import javax.swing.JFrame;

import BLL.Utils;
import BLL.Values;
import BLL.DownFile.DownloadManager;

import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class NewDownload_View_Video extends JFrame {
	private JButton bCancel;
	private JButton bDownload;
	private JLabel labFileName;
	private JLabel labNotice;
	private JTextArea txtFileName;
	private JButton btnChoseFile;
	private JLabel labSaveAt;
	private JLabel labNumber;
	private JComboBox<?> cbNumber;
	private String folder = new File(System.getProperty("user.home"), "Downloads").getAbsolutePath();
	DownloadManager downloadManager = DownloadManager.getInstance();

	public NewDownload_View_Video(Main_View _Main_View, String urlVideo, String urlAudio, String FileName) {
		setTitle("NEW_DOWNLOAD");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(485, 224);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		
		btnChoseFile = new JButton("");
		btnChoseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				File dir = new File(folder); //Đặt thư mục mặc định
				if (dir != null) {
					jfc.setCurrentDirectory(dir);
				}
				
				if (jfc.showDialog(NewDownload_View_Video.this, "Choose folder") == JFileChooser.APPROVE_OPTION) {
					folder = jfc.getSelectedFile().getAbsolutePath();
					labSaveAt.setText("Save at: " + folder);
				}
			}
		});
		btnChoseFile.setIcon(new ImageIcon(NewDownload_View_Video.class.getResource("/View/icon/new-window.png")));
		btnChoseFile.setBounds(383, 37, 27, 22);
		getContentPane().add(btnChoseFile);
		
		labNotice = new JLabel();
		labNotice.setForeground(Color.RED);
		labNotice.setBounds(44, 12, 402, 14);
		labNotice.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(labNotice);
		
		txtFileName = new JTextArea();
		txtFileName.setBounds(128, 37, 255, 22);
		txtFileName.setText(FileName.equals("")?Utils.getFileName(urlVideo):FileName);
		getContentPane().add(txtFileName);
		
		labFileName = new JLabel("File name:");
		labFileName.setBounds(54, 42, 64, 14);
		labFileName.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(labFileName);
		
		bDownload = new JButton("DOWNLOAD NOW");
		bDownload.setBounds(100, 138, 147, 23);
		bDownload.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(bDownload);
		bDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(check()) {
					try {
						new view_Task_DownLoad_Video(new String[] {urlVideo, urlAudio}, folder, txtFileName.getText(), Integer.parseInt(String.valueOf(cbNumber.getSelectedItem())),_Main_View);
						NewDownload_View_Video.this.dispose();
						_Main_View.ReloadView();
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
			}
		});
		
		bCancel = new JButton("CANCEL");
		bCancel.setBounds(298, 138, 89, 23);
		bCancel.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(bCancel);
		
		labSaveAt = new JLabel("Save at: " + folder);
		labSaveAt.setFont(new Font("Times New Roman", Font.ITALIC, 12));
		labSaveAt.setBounds(204, 62, 206, 14);
		getContentPane().add(labSaveAt);
		
		labNumber = new JLabel("Thread number:");
		labNumber.setFont(new Font("Times New Roman", Font.BOLD, 12));
		labNumber.setBounds(54, 85, 89, 14);
		getContentPane().add(labNumber);

		List<String> item = new ArrayList<String>();
		for(int i = 2; i <= Values.MAX_THREAD_COUNT; i++)
			item.add(Integer.toString(i));
		
		cbNumber = new JComboBox<>(item.toArray());
		cbNumber.setBounds(149, 81, 51, 22);
		cbNumber.setSelectedItem(Integer.toString(Values.DEFAULT_THREAD_COUNT));
		getContentPane().add(cbNumber);
		
		bCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		try {
			this.setIconImage(ImageIO.read(getClass().getResourceAsStream("/View/icon/app.png")));
			
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
	}
	public boolean check() {
		String fileName = txtFileName.getText();
		if(fileName.equals("")) {
			labNotice.setText("You must enter all the fields!");
			return false;
		}
		labNotice.setText("");
		return true;
	}
}
