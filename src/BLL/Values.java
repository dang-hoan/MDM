package BLL;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Values {
	public static int Task_ID_COUNTER = 0;
	public static int DEFAULT_THREAD_COUNT = 8;  //Number thread default
	public static int MIN_THREAD_COUNT = 1;
	public static int MAX_THREAD_COUNT = 100;
	public static final int DEFAULT_KEEP_ALIVE_TIME = 0;
	
	public static final int READY = 1;
	public static final int DOWNLOADING = 2;
	public static final int PAUSED = 3;
	public static final int ASSEMBLING = 4;
	public static final int FINISHED = 5;
	public static final int CANCELED = 6;
	public static final int DELETED=7;
	public static final int MERGING=8;
	public static final double[] ZOOM_LEVEL_VALUES = { -1, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5,
			5.0 };
	
	public static final int WINDOWS = 10, MAC = 20, LINUX = 30;
	public static final String datadir = System.getProperty("user.home") + File.separator + ".mdm";
	public static String State(int i)
	{
		switch(i)
		{
			case 0: return "DEFAULT_KEEP_ALIVE_TIME";
			case 1: return "READY";
			case 2: return "DOWNLOADING";
			case 3: return "PAUSED";
			case 4: return "ASSEMBLING";
			case 5: return "FINISHED";
			case 6: return "CANCELED";
			case 7: return "DELETED";
			case 8: return "MERGING";
			case 31: return "FF_NOT_FOUND";
			case 32: return "FF_LAUNCH_ERROR";
			case 33: return "FF_CONVERSION_FAILED";
			case 34: return "FINISHED";
		}
		return ("");
	}
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss Z 'on' dd-MM-yyyy", Locale.getDefault());
}
