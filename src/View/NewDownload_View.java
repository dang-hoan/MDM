package View;

import javax.swing.JFrame;

import BLL.Utils;
import BLL.Values;
import BLL.DownFile.DownloadManager;

import javax.swing.JLabel;

import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.ImageIcon;
import java.awt.Color;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class NewDownload_View extends JFrame {
	private JButton bCancel;
	private JButton bDownload;
	private JLabel labURL;
	private JLabel labFileName;
	private JLabel labNotice;
	private JTextArea txtURL;
	private JTextArea txtFileName;
	private JButton btnChoseFile;
	private JLabel labSaveAt;
	private JLabel labNumber;
	private JComboBox<?> cbNumber;
	private String folder = new File(System.getProperty("user.home"), "Downloads").getAbsolutePath();
	DownloadManager downloadManager = DownloadManager.getInstance();
	private Main_View _Main_View;

	public NewDownload_View(Main_View _Main_View ) throws HeadlessException, UnsupportedFlavorException, IOException {
		setTitle("NEW_DOWNLOAD");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(485, 267);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		
		this._Main_View = _Main_View;
		
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
				
				if (jfc.showDialog(NewDownload_View.this, "Choose folder") == JFileChooser.APPROVE_OPTION) {
					folder = jfc.getSelectedFile().getAbsolutePath();
					labSaveAt.setText("Save at: " + folder);
				}
			}
		});
		btnChoseFile.setIcon(new ImageIcon(NewDownload_View.class.getResource("/View/icon/new-window.png")));
		btnChoseFile.setBounds(373, 81, 27, 22);
		getContentPane().add(btnChoseFile);
		
		labNotice = new JLabel();
		labNotice.setForeground(Color.RED);
		labNotice.setBounds(44, 12, 402, 14);
		labNotice.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(labNotice);
		
		txtURL = new JTextArea();
		txtURL.setBounds(118, 37, 281, 22);

		String s;
		try {
			s = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
			.getData(DataFlavor.stringFlavor);
		}catch(Exception e){
			s = "";
		}
		txtURL.setText(s);
		getContentPane().add(txtURL);
		
		labURL = new JLabel("URL:");
		labURL.setBounds(44, 42, 64, 14);
		labURL.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(labURL);
		
		labFileName = new JLabel("File name:");
		labFileName.setBounds(44, 86, 64, 14);
		labFileName.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(labFileName);
		
		bDownload = new JButton("DOWNLOAD NOW");
		bDownload.setBounds(90, 182, 147, 23);
		bDownload.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(bDownload);
		bDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(check()) {
					try {
						view_Task_DownLoad viewTaskDownload = new view_Task_DownLoad(txtURL.getText(), folder, txtFileName.getText(), Integer.parseInt(String.valueOf(cbNumber.getSelectedItem())),_Main_View);
//						Thread.sleep(5000);
						
						viewTaskDownload.setVisible(true);
						NewDownload_View.this.dispose();
						_Main_View.ReloadView();
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
			}
		});
		
		bCancel = new JButton("CANCEL");
		bCancel.setBounds(288, 182, 89, 23);
		bCancel.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(bCancel);
		
		labSaveAt = new JLabel("Save at: " + folder);
		labSaveAt.setFont(new Font("Times New Roman", Font.ITALIC, 12));
		labSaveAt.setBounds(194, 106, 206, 14);
		getContentPane().add(labSaveAt);
		
		txtFileName = new JTextArea();
		txtFileName.setBounds(118, 81, 255, 22);
		getContentPane().add(txtFileName);
		
		labNumber = new JLabel("Thread number:");
		labNumber.setFont(new Font("Times New Roman", Font.BOLD, 12));
		labNumber.setBounds(44, 129, 89, 14);
		getContentPane().add(labNumber);

		List<String> item = new ArrayList<String>();
		for(int i = Values.MIN_THREAD_COUNT; i <= Values.MAX_THREAD_COUNT; i++)
			item.add(Integer.toString(i));
		
		cbNumber = new JComboBox<>(item.toArray());
		cbNumber.setBounds(139, 125, 51, 22);
		cbNumber.setSelectedItem(Integer.toString(Values.DEFAULT_THREAD_COUNT));
		getContentPane().add(cbNumber);
		
		bCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
	public boolean check() {
		String urlStr = txtURL.getText();
		String fileName = txtFileName.getText();
		if(urlStr.equals("") || fileName.equals("")) {
			labNotice.setText("You must enter all the fields!");
			return false;
		}
		if (!Utils.validateURL(urlStr)) {
			urlStr = "http://" + urlStr;
			if (!Utils.validateURL(urlStr)) {
				labNotice.setText("Url are no valid!");
				return false;
			} else {
				txtURL.setText(urlStr);
			}
		}
		labNotice.setText("");
		return true;
	}
}
