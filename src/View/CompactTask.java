package View;
public class CompactTask 
{
	private int id;
	private String name;	
	private String icon;
	private String status;
	private String size;
	private String datetime;
	
	public CompactTask(String name, String icon, String status, String size, String datetime) 
	{
		this.name = name;
		this.icon = icon;
		this.status = status;
		this.size = size;
		this.datetime = datetime;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
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

}
