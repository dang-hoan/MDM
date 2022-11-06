package View;

import javax.swing.JFrame;
import BLL.DownFile.DownloadManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.ImageIcon;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class NewDownload_View extends JFrame {
	private JButton bCancel;
	private JButton bDownload;
	private JLabel labURL;
	private JLabel labFileName;
	private JLabel labNotice;
	private JTextArea txtURL;
	private JTextArea txtFileName;
	private JButton btnNewButton;
	private JLabel labSaveAt;
	private JLabel labNumber;
	private JComboBox cbNumber;
	private String folder = new File(System.getProperty("user.home"), "Downloads").getAbsolutePath();
	DownloadManager downloadManager = DownloadManager.getInstance();

	public NewDownload_View() throws HeadlessException, UnsupportedFlavorException, IOException {
		setTitle("NEW_DOWNLOAD");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(485, 267);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		
		btnNewButton = new JButton("");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				String dir = folder;     //Lấy đường dẫn thư mục cũ
//				if (String.isNullOrEmptyOrBlank(dir)) {
//					dir = Config.getInstance().getLastFolder();
//					if (StringUtils.isNullOrEmptyOrBlank(dir)) {
//						dir = Config.getInstance().getDownloadFolder();
//					}
//				}
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				//File dir = new File(folder); //Đặt thư mục cũ
//				if (file != null) {
//					jfc.setCurrentDirectory(file);
//				}
				
				if (jfc.showOpenDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
					folder = jfc.getSelectedFile().getAbsolutePath();
					labSaveAt.setText("Save at: " + folder);
				}
			}
		});
		btnNewButton.setIcon(new ImageIcon("icon\\new-window.png"));
		btnNewButton.setBounds(373, 81, 27, 22);
		getContentPane().add(btnNewButton);
		
		labNotice = new JLabel();
		labNotice.setForeground(Color.RED);
		labNotice.setBounds(44, 12, 402, 14);
		labNotice.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(labNotice);
		
		txtURL = new JTextArea();
		txtURL.setBounds(118, 37, 281, 22);
		
		txtURL.setText((String) Toolkit.getDefaultToolkit().getSystemClipboard()
				.getData(DataFlavor.stringFlavor));
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
						downloadManager.addTask(txtURL.getText(), folder, txtFileName.getText(), Integer.parseInt(String.valueOf(cbNumber.getSelectedItem())));
						downloadManager.start();					
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
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
		
		cbNumber = new JComboBox();
		cbNumber.setMaximumRowCount(50);
		cbNumber.setBounds(139, 125, 43, 22);
		List<String> item = new ArrayList<String>();
		for(int i = 1; i <= cbNumber.getMaximumRowCount(); i++)
			item.add(Integer.toString(i));
		cbNumber.setModel(new DefaultComboBoxModel(item.toArray()));
		cbNumber.setSelectedIndex(7);
		getContentPane().add(cbNumber);
		
		bCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
	public boolean check() {
		if(txtURL.getText().equals("") || txtFileName.getText().equals("")) {
			labNotice.setText("You must enter all the fields!");
			return false;
		}
		labNotice.setText("");
		return true;
	}
}
