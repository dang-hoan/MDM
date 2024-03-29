package View;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import BLL.Values;

public class TaskRenderer extends JPanel implements ListCellRenderer<CompactTask>
{
	private static final long serialVersionUID = 1L;
	JLabel lbitem;
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
		lbitem = new JLabel();
		pnicon = new JPanel(new FlowLayout());
		pnicon.add(lbitem);
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
		Icon img = load(value.getTypeitem(), value.getStatusitem(), 40, 40);
		lbitem.setIcon(img);
		lbname.setText(value.getName());
		lbstate.setText(value.getStatus());
		lbsize.setText(value.getSize());
		lbdate.setText(Values.dateFormat.format(value.getDate()));
		
		lbitem.setOpaque(true);
		pnicon.setOpaque(true);
		lbname.setOpaque(true);
		lbstate.setOpaque(true);
		lbsize.setOpaque(true);
		lbdate.setOpaque(true);
		
		if (isSelected)
		{
			lbitem.setBackground(list.getSelectionBackground());
			pnicon.setBackground(list.getSelectionBackground());
			lbname.setBackground(list.getSelectionBackground());
			lbstate.setBackground(list.getSelectionBackground());
			lbsize.setBackground(list.getSelectionBackground());
			lbdate.setBackground(list.getSelectionBackground());
				   setBackground(list.getSelectionBackground());
		}
		else
		{
			lbitem.setBackground(list.getBackground());
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
	public Icon load(URL linkImage, URL linkImage2, int k, int m)/*linkImage là tên icon, k kích thước chiều rộng mình muốn,
													m chiều dài và hàm này trả về giá trị là 1 icon.*/
	{  
		try 
		{
			BufferedImage imgBG = ImageIO.read(linkImage);//đọc ảnh dùng BufferedImage
	        BufferedImage imgFG = ImageIO.read(linkImage2);//đọc ảnh dùng BufferedImage
	        // For simplicity we will presume the images are of identical size
	       
	        final BufferedImage combinedImage = new BufferedImage( 
	                imgBG.getWidth(), 
	                imgBG.getHeight(), 
	                BufferedImage.TYPE_INT_ARGB );
	        Graphics2D g = (Graphics2D) combinedImage.getGraphics();
	        g.drawImage(imgBG,0,0,null);
	        g.drawImage(imgFG,0,260,null);
	        g.dispose();
	        int x = k;
	        int y = m;
	        int ix = combinedImage.getWidth();
	        int iy = combinedImage.getHeight();
	        int dx = 0, dy = 0;
	 
	        if (x / y > ix / iy) {
	            dy = y;
	            dx = dy * ix / iy;
	        } else {
	            dx = x;
	            dy = dx * iy / ix;
	        }
	        return new ImageIcon(combinedImage.getScaledInstance(dx, dy, BufferedImage.SCALE_SMOOTH));
	    } catch (IOException e) { e.printStackTrace(); }
	    return null;
	}
	
}
