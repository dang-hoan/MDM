package View;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import BLL.Values;
import BLL.DownFile.DownloadManager;

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
	/**
	 * Launch the application.
	 * @throws IOException 
	 */
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

	/**
	 * Create the frame.
	 */

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
					new NewDownload_View().setVisible(true);
					
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (UnsupportedFlavorException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				};
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
		
		JPanel panel = new JPanel();
		
		JPanel panel_1 = new JPanel();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(panel, GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE))
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
		);
		panel.setLayout(null);
		
		JButton bNewDownload = new JButton("");
		bNewDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					new NewDownload_View().setVisible(true);
					
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (UnsupportedFlavorException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				};
			}
		});
		bNewDownload.setBounds(0, 0, 62, 65);
		panel.add(bNewDownload);
		bNewDownload.setIcon(new ImageIcon("icon\\plus.png"));
		
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
		bCancelDownload.setIcon(new ImageIcon("icon\\x.png"));
		bCancelDownload.setBounds(72, 0, 62, 65);
		panel.add(bCancelDownload);
		
		JButton bPauseDownload = new JButton("");
		bPauseDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DownloadManager.getInstance().pauseTask(Values.Task_ID_COUNTER-1);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		bPauseDownload.setIcon(new ImageIcon("icon\\pause.png"));
		bPauseDownload.setBounds(144, 0, 62, 65);
		panel.add(bPauseDownload);
		
		JButton bStartDownload = new JButton("");
		bStartDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DownloadManager.getInstance().startTask(Values.Task_ID_COUNTER-1);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		bStartDownload.setIcon(new ImageIcon("icon\\play.png"));
		bStartDownload.setBounds(216, 0, 62, 65);
		panel.add(bStartDownload);
		
		JButton bSettings = new JButton("");
		bSettings.setIcon(new ImageIcon("icon\\gear.png"));
		bSettings.setBounds(288, 0, 62, 65);
		panel.add(bSettings);
		contentPane.setLayout(gl_contentPane);
	}
}
