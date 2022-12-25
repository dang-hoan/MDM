package View;

import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import BLL.Utils;
import BLL.Values;
import BLL.DownFile.DownloadManager;
import BLL.DownFile.DownloadTask;

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
	private long size = -2;
	DownloadManager downloadManager = DownloadManager.getInstance();

	public NewDownload_View(Main_View _Main_View, String url, long length) throws HeadlessException, UnsupportedFlavorException, IOException {
		setTitle("NEW_DOWNLOAD");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(485, 267);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);

		btnChoseFile = new JButton("");
		btnChoseFile.addActionListener(new ActionListener() {
			@Override
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

		txtFileName = new JTextArea();
		txtFileName.setBounds(118, 81, 255, 22);
		getContentPane().add(txtFileName);

		txtURL = new JTextArea();
		txtURL.setBounds(118, 37, 281, 22);
		DocumentListener dl = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFieldState();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFieldState();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFieldState();
			}
            protected void updateFieldState() {
            	SwingUtilities.invokeLater(new Runnable() {
        			@Override
        			public void run() {
                    	String txt = txtURL.getText();
        			    txtFileName.setText(Utils.getFileName(txt));
        			    
                    	if(check()) {
        	            	if(!txt.equals("") && !txtFileName.getText().equals("")) labNotice.setText("");

        	            	if(length == -2) size=Utils.getFileLength(txt); //chưa kết nối tới server lần nào
        	            	else size = length;
                    	}
        			}
        		});
            }
		};
		txtURL.getDocument().addDocumentListener(dl);

		try {
			if(url == "")
				url = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

		}catch(Exception e){
			url = "";
		}
		txtURL.setText(url);
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
		bDownload.setBounds(194, 182, 147, 23);
		bDownload.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(bDownload);
		bDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(check()) {
					try {
						if(isURLExist()) {
							new view_Task_DownLoad(txtURL.getText(), folder, txtFileName.getText(), Integer.parseInt(String.valueOf(cbNumber.getSelectedItem())),_Main_View,size);
							NewDownload_View.this.dispose();
							_Main_View.ReloadView();
						}

					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}

			}
		});

		bCancel = new JButton("CANCEL");
		bCancel.setBounds(372, 182, 89, 23);
		bCancel.setFont(new Font("Times New Roman", Font.BOLD, 12));
		getContentPane().add(bCancel);

		labSaveAt = new JLabel("Save at: " + folder);
		labSaveAt.setFont(new Font("Times New Roman", Font.ITALIC, 12));
		labSaveAt.setBounds(194, 106, 206, 14);
		getContentPane().add(labSaveAt);

		labNumber = new JLabel("Thread number:");
		labNumber.setFont(new Font("Times New Roman", Font.BOLD, 12));
		labNumber.setBounds(44, 129, 89, 14);
		getContentPane().add(labNumber);

		List<String> item = new ArrayList<>();
		for(int i = Values.MIN_THREAD_COUNT; i <= Values.MAX_THREAD_COUNT; i++)
			item.add(Integer.toString(i));

		cbNumber = new JComboBox<>(item.toArray());
		cbNumber.setBounds(139, 125, 51, 22);
		cbNumber.setSelectedItem(Integer.toString(Values.DEFAULT_THREAD_COUNT));
		getContentPane().add(cbNumber);
		
		JButton btnDownloadLater = new JButton("DOWNLOAD LATER");
		btnDownloadLater.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DownloadTask task = new DownloadTask(Values.Task_ID_COUNTER++, txtURL.getText(), folder, txtFileName.getText(), Integer.parseInt(String.valueOf(cbNumber.getSelectedItem())), size, null, null, false);
				DownloadManager.getInstance().addTask(task);
				dispose();
				_Main_View.ReloadView();
			}
		});
		btnDownloadLater.setFont(new Font("Times New Roman", Font.BOLD, 12));
		btnDownloadLater.setBounds(10, 182, 157, 23);
		getContentPane().add(btnDownloadLater);

		bCancel.addActionListener(new ActionListener() {
			@Override
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
	public boolean isURLExist() {
		if(!Utils.isURLExist(txtURL.getText())) {
			labNotice.setText("URL is not exist!");
			return false;
		}
		return true;
	}
}
