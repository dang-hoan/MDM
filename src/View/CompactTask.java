package View;

import java.net.URL;

public class CompactTask 
{
	private int id;
	private String name;	
	private URL icon;
	private String status;
	private String size;
	private String datetime;
	
	public CompactTask(int id, String name, URL icon, String status, String size, String datetime) 
	{
		this.name = name;
		this.icon = icon;
		this.status = status;
		this.size = size;
		this.datetime = datetime;
		this.id = id;
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

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	@Override
	public String toString() {
		return "Đồ án lập trình mạng:" + id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
