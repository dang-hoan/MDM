package BLL;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Values {
	public static int Task_ID_COUNTER = 0;
	public static int DEFAULT_THREAD_COUNT = 8;  //Number thread default
	public static int MIN_THREAD_COUNT = 1;
	public static int MAX_THREAD_COUNT = 50;
	public static final int DEFAULT_KEEP_ALIVE_TIME = 0;
	public static final int READY = 1;
	public static final int DOWNLOADING = 2;
	public static final int PAUSED = 3;
	public static final int FINISHED = 4;
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss Z 'on' dd-MM-yyyy", Locale.getDefault());
}
