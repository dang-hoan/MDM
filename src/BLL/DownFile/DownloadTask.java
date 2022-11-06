package BLL.DownFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
//import java.util.Timer;
import java.util.TimerTask;

import BLL.Values;

public class DownloadTask {
	private int TaskID;
	private int TaskStatus = Values.READY;
	private long createDate;
	
	private String Url;
	private String SaveDirectory;
	private String SaveFile;
	private String ProgressFile;			//Lưu tiến trình tải
	
	private int FileSize;	
	private int ThreadCount = Values.DEFAULT_THREAD_COUNT;
	private int completedThread = 0;
	
	private ArrayList<DownloadRunnable> ListRunnable = new ArrayList<DownloadRunnable>();

	private SpeedMonitor SpeedMonitor = new SpeedMonitor(this);

//	private Timer SpeedTimer = new Timer();
//	private Timer mStoreTimer = new Timer();


	private static class SpeedMonitor extends TimerTask {
		private long LastSecondSize = 0;	
		private long CurrentSecondSize = 0;		
		private long Speed;		
		private long MaxSpeed;	
		private long AverageSpeed;
		private long Counter;

		private DownloadTask HostTask;

		public long getMaxSpeed() {
			return MaxSpeed;
		}

		public SpeedMonitor(DownloadTask TaskBelongTo) {
			HostTask = TaskBelongTo;
		}

		@Override
		public void run() {
			Counter++;
			CurrentSecondSize = HostTask.getDownloadedSize();
			Speed = CurrentSecondSize - LastSecondSize;
			LastSecondSize = CurrentSecondSize;
			if (Speed > MaxSpeed) {
				MaxSpeed = Speed;
			}

			AverageSpeed = CurrentSecondSize / Counter;
		}

		public long getDownloadedTime() {
			return Counter;
		}

		public long getSpeed() {
			return Speed;
		}

		public long getAverageSpeed() {
			return AverageSpeed;
		}
	}

//	private class StoreMonitor extends TimerTask {
//		@Override
//		public void run() {
//			storeProgress();
//		}
//	}

	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName, int ThreadCount) throws IOException {
		this.TaskID = TaskID;
		this.Url = url;
		setTargetFile(saveDirectory, saveName);
		if(ThreadCount > 0) this.ThreadCount = ThreadCount;
		System.out.println("TaskID: " + TaskID);
	}

	public Boolean setTargetFile(String saveDir, String saveName) throws IOException {
		if (saveDir.lastIndexOf(File.separator) == saveDir.length() - 1) {
			saveDir = saveDir.substring(0, saveDir.length() - 1);
		}
		SaveDirectory = saveDir;
		
		File dirFile = new File(saveDir);
		if (dirFile.exists() == false) {
			if (dirFile.mkdirs() == false) {
				throw new RuntimeException("Error to create directory");
			}
		}

		SaveFile = saveName + "." + getFileType(Url).split("/")[1];
		ProgressFile = saveName + ".tmp";

		File file = new File(SaveDirectory, SaveFile);
		String f = saveName;
		int i = 1;
		while(file.exists() == true) {
			SaveFile = f + "(" + Integer.toString(i) + ")" + "." + getFileType(Url).split("/")[1];
			file = new File(SaveDirectory, SaveFile);
			i += 1;
		}
		file.createNewFile();
		return true;
	}

	public int getTaskID() {
		return TaskID;
	}

	public String getUrl() {
		return Url;
	}

	public void setUrl(String Url) {
		this.Url = Url;
	}

	public String getSaveDirectory() {
		return SaveDirectory;
	}

	public void setSaveDirectory(String SaveDirectory) {
		this.SaveDirectory = SaveDirectory;
	}

	public String getSaveName() {
		return SaveFile;
	}

	public void setSaveName(String SaveName) {
		this.SaveFile = SaveName;
	}

	public void setTaskThreadCount(int thread_count) {
		ThreadCount = thread_count;
	}

	public int getTaskThreadCount() {
		return ThreadCount;
	}

	private void splitDownload(int thread_count) { 				//Split thread
		try {
			int size = getFileLength(Url);
			FileSize = size;
			int sublen = size / thread_count;
			for (int i = 0; i < thread_count; i++) {
				int startPos = sublen * i;
				int endPos = (i == thread_count - 1) ? size	: (sublen * (i + 1) - 1);
				
				DownloadRunnable runnable = new DownloadRunnable(
						Url, SaveDirectory, SaveFile,
						startPos, endPos,
						TaskID, i+1, this);
				ListRunnable.add(runnable);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resumeProgress() throws IOException {   //Khôi phục công việc các luồng
		File file = new File(DownloadManager.getInstance().getDataDir(), ProgressFile);
		if(file.exists() == false) return;
		
//		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss z 'on' dd-MM-yyyy");
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
			String line = reader.readLine();
			if (line == null) {
				throw new NullPointerException("Unexpected EOF");
			}
			int count = Integer.parseInt(line.trim());
			for (int i = 0; i < count; i++) {
				int ThreadID = Integer.parseInt(reader.readLine());
				int startPosition = Integer.parseInt(reader.readLine());
				int currentPosition = Integer.parseInt(reader.readLine());
				int endPosition = Integer.parseInt(reader.readLine());
				if(currentPosition >= endPosition) continue;
				DownloadRunnable runnable = new DownloadRunnable(
						Url, SaveDirectory, SaveFile,
						startPosition, currentPosition, endPosition,
						TaskID, ThreadID, this);
				ListRunnable.add(runnable);
			}
			reader.close();
		}catch (Exception e) {
			
		}
	}

	public void startTask() throws IOException {
		setDownloadStatus(Values.DOWNLOADING);
		resumeProgress();	//Khôi phục công việc của các luồng (nếu có)
		
		if (ListRunnable.size() == 0) {
			splitDownload(ThreadCount);
		}
		
		for (DownloadRunnable runnable : ListRunnable) {
			runnable.start();
		}
//		SpeedTimer.scheduleAtFixedRate(SpeedMonitor, 0, 1000);
	}
	
	public int getThreadByID(int ThreadID) {
		for(int i = 0; i < ListRunnable.size(); i++) {
			if(ListRunnable.get(i).getThreadID() == ThreadID)
				return i;
		}
		return -1;
	}
	
	public void notify(int ThreadID) {
        System.out.println("*******Task ID " + TaskID + ": Thread " + ThreadID + " download complete *********");
        completedThread++;
        ListRunnable.remove(getThreadByID(ThreadID));
        if(completedThread == ThreadCount) {
        	TaskStatus = Values.FINISHED;
        	createDate = System.currentTimeMillis();
        	System.out.println("\n--------Complete file " + SaveFile + " download--------\n");
            DownloadManager.getInstance().cancelTask(TaskID);
        }
	}

	public void addPartedTask(DownloadRunnable runnable) {
		ListRunnable.add(runnable);
	}
	
	private int getFileLength(String fileUrl) throws IOException {
		URL url = new URL(fileUrl);
		URLConnection connection = url.openConnection();
		return connection.getContentLength();
	}

	private String getFileType(String fileUrl) throws IOException {
		URL url = new URL(fileUrl);
		URLConnection connection = url.openConnection();
		return connection.getContentType();
	}

	public File getDownloadFile() {
		return new File(SaveDirectory + File.separator + SaveFile);
	}

	public long getDownloadedSize() { //Kích thước đã tải
		long size = 0;
		for(DownloadRunnable r : ListRunnable) {
			size += r.getCurrentPosition() - r.getStartPosition();
		}
		return size;
	}

	public long getSpeed() {
		return SpeedMonitor.getSpeed();
	}

	public String getReadableSpeed() {
		return DownloadUtils.getReadableSpeed(getSpeed());
	}

	public long getMaxSpeed() {
		return SpeedMonitor.getMaxSpeed();
	}

	public String getReadableMaxSpeed() {
		return DownloadUtils.getReadableSpeed(getMaxSpeed());
	}

	public long getAverageSpeed() {
		return SpeedMonitor.getAverageSpeed();
	}

	public String getReadableAverageSpeed() {
		return DownloadUtils.getReadableSpeed(SpeedMonitor.getAverageSpeed());
	}

	public long getTimePassed() {
		return SpeedMonitor.getDownloadedTime();
	}

	public int getActiveTheadCount() {
		return ListRunnable.size();
	}

	public int getFileSize() {
		return FileSize;
	}

	public void pause() throws IOException {
		setDownloadStatus(Values.PAUSED);
		createDate = System.currentTimeMillis();
		storeProgress();
		for(DownloadRunnable i : ListRunnable) {
			i.pause();			
		}
	}

	private void setDownloadStatus(int status) {
		if (status == Values.FINISHED) {
//			SpeedTimer.cancel();
		}
		TaskStatus = status;
	}
	
	public int getDownloadStatus() {
		return TaskStatus;
	}
	
	public long getCreateDate() {
		return createDate;
	}
	
	public void storeProgress() throws IOException { //Lưu tiến trình làm việc
		File dir = new File(DownloadManager.getInstance().getDataDir());
		if (dir.exists() == false) {
			dir.mkdir();
		}

		File file = new File(DownloadManager.getInstance().getDataDir(), ProgressFile);
		if (file.exists() == false) {
			file.createNewFile();
		}
		BufferedWriter writer = null;
		String newLine = System.getProperty("line.separator");
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
			writer.write(Integer.toString(ListRunnable.size()));
			writer.newLine();
			for(DownloadRunnable i : ListRunnable) {
				String s = i.getThreadID() + newLine + 
						   i.getStartPosition() + newLine +
						   i.getCurrentPosition() + newLine +
						   i.getEndPosition() + newLine;
				writer.write(s);				
			}
			writer.write(createDate + newLine);
			writer.close();
		} catch (Exception e) {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e1) {
			}
		}
//		ListRunnable.clear();
	}

	public void cancel() {
//		deleteHistoryFile();
//		SpeedTimer.cancel();
		ListRunnable.clear();
//		mThreadPoolRef.cancel(TaskID);
	}
}
