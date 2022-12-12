package View;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import BLL.DownFile.DownloadManager;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

public class VerifyFile extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtChecksum;
	private JComboBox<?> cbbType;
	private JLabel labNotice;

	public VerifyFile(int IDTask) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 178);
		setLocationRelativeTo(null);
		setTitle("Verify file");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Enter a checksum:");
		lblNewLabel.setBounds(29, 25, 111, 24);
		contentPane.add(lblNewLabel);
		
		txtChecksum = new JTextField();
		txtChecksum.setBounds(29, 49, 372, 20);
		contentPane.add(txtChecksum);
		txtChecksum.setColumns(10);
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
            	if(!txtChecksum.getText().equals("")) labNotice.setText("");
            }
		};
		txtChecksum.getDocument().addDocumentListener(dl);
		
		JButton btnVerify = new JButton("Verify");
		btnVerify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(txtChecksum.getText().equals("")) {
					labNotice.setText("You haven't entered checksum field!");
					return;
				}
				
				Boolean result = DownloadManager.getInstance().checkFile(IDTask, cbbType.getSelectedItem().toString(), txtChecksum.getText());		
				
				if(result) {									    
				    ImageIcon icon = new ImageIcon(Main_View.class.getResource("/View/icon/check.png"));
					icon = new ImageIcon(icon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH));
					
					JOptionPane.showMessageDialog(VerifyFile.this, "This file is intact and not attacked by hackers!", "Notification", JOptionPane.INFORMATION_MESSAGE, icon);
				}
				else {
					ImageIcon icon = new ImageIcon(Main_View.class.getResource("/View/icon/error.png"));
					icon = new ImageIcon(icon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH));
					
					JOptionPane.showMessageDialog(VerifyFile.this, "This file is error when downloading!", "Notification", JOptionPane.INFORMATION_MESSAGE, icon);
				}
			}
		});
		btnVerify.setBounds(106, 107, 89, 23);
		contentPane.add(btnVerify);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btnCancel.setBounds(228, 107, 89, 23);
		contentPane.add(btnCancel);
		
		Object[] type = {"All", "MD5", "SHA-1", "SHA-256"};
		cbbType = new JComboBox<>(type);
		cbbType.setBounds(132, 74, 79, 22);
		cbbType.setSelectedIndex(0);
		contentPane.add(cbbType);
		
		JLabel lblTypeChecksum = new JLabel("Type checksum:");
		lblTypeChecksum.setBounds(29, 72, 100, 24);
		contentPane.add(lblTypeChecksum);
		
		labNotice = new JLabel("");
		labNotice.setForeground(Color.RED);
		labNotice.setBounds(168, 25, 258, 24);
		contentPane.add(labNotice);
		
		try {
			this.setIconImage(ImageIO.read(getClass().getResourceAsStream("/View/icon/app.png")));
			
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
}
