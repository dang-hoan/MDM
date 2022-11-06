package BLL;

public class Values {
	public static int Task_ID_COUNTER = 0;
	public static int DEFAULT_THREAD_COUNT = 8;  //Number thread default
	public static final int DEFAULT_KEEP_ALIVE_TIME = 0;
	public static final int READY = 1;
	public static final int DOWNLOADING = 2;
	public static final int PAUSED = 3;
	public static final int FINISHED = 4;
	
	private Values _this = null;
	public Values getInstance() {
		if(_this == null) _this = new Values();
		return _this;
	}
}
