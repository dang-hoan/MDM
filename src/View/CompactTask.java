package View;

import java.net.URL;

import BLL.Values;

public class CompactTask {
	private int id;
	private String folder;
	private String name;
	private URL typeitem;
	private URL statusitem;
	private String status;
	private String size;
	private double size_file;
	private long total_size;
	private long date;
	private String type_File;
	private int munber_id;

	

	public CompactTask(int id, String folder, String name, String status, String size, double size_file, long date,
			String type_File, long total_Size) {
		this.folder = folder;
		this.name = name;
		
		if (type_File.equals("")) 
			typeitem = Main_View.class.getResource("/View/icon/file_icon/ItemUnk.png");
		else if (Values.video.contains(type_File))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/ItemVd.png");
		else if (Values.music.contains(type_File))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/ItemMsc.png");
		else if (Values.picture.contains(type_File))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/ItemPic.png");
		else if (Values.program.contains(type_File))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/itemprgr.png");
		else if (Values.compressed.contains(type_File))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/itemzip.png");
		else typeitem = Main_View.class.getResource("/View/icon/file_icon/itemDoc.png");
		
		switch(status) {
			case "READY":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateNon.png"));
				break;
			}
			case "DOWNLOADING":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateDown.png"));
				break;
			}
			case "PAUSED":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StatePause.png"));
				break;
			}
			case "ASSEMBLING":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateAssembling.png"));
				break;
			}
			case "FINISHED":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateDone.png"));
				break;
			}
			case "CANCELED":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateX.png"));
				break;
			}
			case "MERGING":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateMerge.png"));
				break;
			}
			case "FF_NOT_FOUND":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateX.png"));
				break;
			}
			case "FF_LAUNCH_ERROR":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateX.png"));
				break;
			}
			case "FF_CONVERSION_FAILED":{
				setStatusitem(Main_View.class.getResource("/View/icon/file_icon/StateX.png"));
				break;
			}
		}		
		
		this.status = status;
		this.size = size;
		this.id = id;
		this.size_file = size_file;
		this.date = date;
		this.type_File = type_File;
		this.total_size = total_Size;
	}

	public long getTotal_size() {
		return total_size;
	}

	public void setTotal_size(long total_size) {
		this.total_size = total_size;
	}

	public double getSize_file() {
		return size_file;
	}

	public void setSize_file(double size_file) {
		this.size_file = size_file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URL getTypeitem() {
		return typeitem;
	}

	public void setTypeitem(URL typeitem) {
		this.typeitem = typeitem;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getDate() {
		return date;
	}

	public String getType_File() {
		return type_File;
	}

	public void setType_File(String type_File) {
		this.type_File = type_File;
	}
	public int getMunber_id() {
		return munber_id;
	}

	public void setMunber_id(int munber_id) {
		this.munber_id = munber_id;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public URL getStatusitem() {
		return statusitem;
	}

	public void setStatusitem(URL statusitem) {
		this.statusitem = statusitem;
	}
}
