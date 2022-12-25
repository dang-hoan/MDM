package View;

import java.net.URL;

public class CompactTask {
	private int id;
	private String name;
	private URL icon;
	private String status;
	private String size;
	private double size_file;
	private long total_size;
	private long date;
	private String type_File;
	private int munber_id;

	

	public CompactTask(int id, String name, URL icon, String status, String size, double size_file, long date,
			String type_File, long total_Size) {
		this.name = name;
		this.icon = icon;
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

	public URL getIcon() {
		return icon;
	}

	public void setIcon(URL icon) {
		this.icon = icon;
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
}
