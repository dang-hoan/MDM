package View;

import java.net.URL;
import java.util.Arrays;

import BLL.Values;

public class CompactTask {
	private int id;
	private String folder;
	private String name;
	private URL typeitem;
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
		else if (Arrays.stream(Values.video).anyMatch(type_File::equals))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/ItemVd.png");
		else if (Arrays.stream(Values.music).anyMatch(type_File::equals))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/ItemMsc.png");
		else if (Arrays.stream(Values.picture).anyMatch(type_File::equals))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/ItemPic.png");
		else if (Arrays.stream(Values.program).anyMatch(type_File::equals))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/itemprgr.png");
		else if (Arrays.stream(Values.compressed).anyMatch(type_File::equals))
			typeitem = Main_View.class.getResource("/View/icon/file_icon/itemzip.png");
		else typeitem = Main_View.class.getResource("/View/icon/file_icon/itemDoc.png");
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
}
