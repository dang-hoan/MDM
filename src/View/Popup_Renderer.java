package View;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;


public class Popup_Renderer extends JPanel implements ListCellRenderer<VideoPopupitem> {
	private static final long serialVersionUID = 1L;
	
	JLabel lb_File_Name;
	public Popup_Renderer() {
		// TODO Auto-generated constructor stub
		setLayout(new BorderLayout());
		lb_File_Name = new JLabel();
		add(lb_File_Name,BorderLayout.CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends VideoPopupitem> list, VideoPopupitem value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		
		lb_File_Name.setText(value.getFile_Name());	
		lb_File_Name.setOpaque(true);
			
		if (isSelected)
		{
			
			lb_File_Name.setBackground(list.getSelectionBackground());
		}
		else
		{
			lb_File_Name.setBackground(list.getBackground());
		
		}
		
		return this;
		// TODO Auto-generated method stub
	}

}
