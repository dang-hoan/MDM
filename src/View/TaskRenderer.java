package View;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class TaskRenderer extends JPanel implements ListCellRenderer<CompactTask>
{
	private static final long serialVersionUID = 1L;
	JLabel lbicon;
	JLabel lbname;
	JLabel lbstate;
	JLabel lbsize;
	JLabel lbdate;
	JPanel pnicon, pnText, pnInfor;
	
	public TaskRenderer() 
	{
		setLayout(new BorderLayout(5,5));	
		lbname = new JLabel();
		lbstate = new JLabel();
		lbsize = new JLabel();
		lbdate = new JLabel();
		lbicon = new JLabel();
		pnicon = new JPanel(new FlowLayout());
		pnicon.add(lbicon);
		pnicon.setBorder(new EmptyBorder(5,5,0,0));
		add(pnicon, BorderLayout.WEST);
		pnInfor = new JPanel(new GridLayout());
		pnInfor.add(lbsize); pnInfor.add(lbstate); pnInfor.add(lbdate);
		pnText = new JPanel(new GridLayout(0,1));
		pnText.add(lbname); pnText.add(pnInfor);
		add(pnText, BorderLayout.CENTER);
	}
	@Override
	public Component getListCellRendererComponent(JList<? extends CompactTask> list,
			CompactTask value, int index, boolean isSelected, boolean cellHasFocus) 
	{
		Icon img = load(value.getIcon(), 40, 40);
		lbicon.setIcon(img);
		lbname.setText(value.getName());
		lbstate.setText(value.getStatus());
		lbsize.setText(value.getSize());
		lbdate.setText(value.getDatetime());
		
		lbicon.setOpaque(true);
		pnicon.setOpaque(true);
		lbname.setOpaque(true);
		lbstate.setOpaque(true);
		lbsize.setOpaque(true);
		lbdate.setOpaque(true);
		
		if (isSelected)
		{
			lbicon.setBackground(list.getSelectionBackground());
			pnicon.setBackground(list.getSelectionBackground());
			lbname.setBackground(list.getSelectionBackground());
			lbstate.setBackground(list.getSelectionBackground());
			lbsize.setBackground(list.getSelectionBackground());
			lbdate.setBackground(list.getSelectionBackground());
				   setBackground(list.getSelectionBackground());
		}
		else
		{
			lbicon.setBackground(list.getBackground());
			pnicon.setBackground(list.getBackground());
			lbname.setBackground(list.getBackground());
			lbstate.setBackground(list.getBackground());
			lbsize.setBackground(list.getBackground());
			lbdate.setBackground(list.getBackground());
				   setBackground(list.getBackground());
		}
		
		return this;
	}
	// can chinh image
	public Icon load(URL linkImage, int k, int m)/*linkImage là tên icon, k kích thước chiều rộng mình muốn,
													m chiều dài và hàm này trả về giá trị là 1 icon.*/
	{  
		try 
		{
	        BufferedImage image = ImageIO.read(linkImage);//đọc ảnh dùng BufferedImage
	        
	        int x = k;
	        int y = m;
	        int ix = image.getWidth();
	        int iy = image.getHeight();
	        int dx = 0, dy = 0;
	 
	        if (x / y > ix / iy) {
	            dy = y;
	            dx = dy * ix / iy;
	        } else {
	            dx = x;
	            dy = dx * iy / ix;
	        }
	        return new ImageIcon(image.getScaledInstance(dx, dy, BufferedImage.SCALE_SMOOTH));
	    } catch (IOException e) { e.printStackTrace(); }
	    return null;
	}
}
