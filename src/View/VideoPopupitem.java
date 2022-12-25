package View;

public class VideoPopupitem {
	private String url_Video;
	private String url_Audio;
	private String file_Name;
	
	public VideoPopupitem(String url_Video, String url_Audio, String file_Name) {
		this.url_Video = url_Video;
		this.url_Audio = url_Audio;
		this.file_Name = file_Name;
	}
	public String getUrl_Video() {
		return url_Video;
	}
	public void setUrl_Video(String url_Video) {
		this.url_Video = url_Video;
	}
	public String getUrl_Audio() {
		return url_Audio;
	}
	public void setUrl_Audio(String url_Audio) {
		this.url_Audio = url_Audio;
	}
	public String getFile_Name() {
		return file_Name;
	}
	public void setFile_Name(String file_Name) {
		this.file_Name = file_Name;
	}
	
	

}
