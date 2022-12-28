package View;

public class VideoPopupitem {
	private String url_Video;
	private String url_Audio;
	private String file_Name;
	private long len1;
	private long len2;
	
	public VideoPopupitem(String url_Video, String url_Audio, long len1, long len2, String file_Name) {
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
	public long getLen1() {
		return len1;
	}
	public void setLen1(long len1) {
		this.len1 = len1;
	}
	public long getLen2() {
		return len2;
	}
	public void setLen2(long len2) {
		this.len2 = len2;
	}
	public void change_Name(String id ,long clen,String name)
	{
		if(this.url_Video.contains(id) && this.url_Video.contains(Long.toString(clen)))
		{
			this.file_Name=name;
		}
	}
	
	

}
