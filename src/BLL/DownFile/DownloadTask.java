package BLL.DownFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
	private String ProgressFolder;
	
	private int FileSize;	
	private int ThreadCount;
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

	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName, int ThreadCount) throws IOException {
		this.TaskID = TaskID;
		this.Url = url;
		setTargetFile(saveDirectory, saveName);
		this.ThreadCount = ThreadCount;
		System.out.println("TaskID: " + TaskID);
	}
	
	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName, String progressFile, String progressFolder, int FileSize, int ThreadCount, int DownloadStatus, long createDate) throws IOException {
		this.TaskID = TaskID;
		this.Url = url;
		this.createDate = createDate;
		this.SaveDirectory = saveDirectory;
		this.SaveFile = saveName;
		this.ProgressFile = progressFile;
		this.ProgressFolder = progressFolder;
		this.FileSize = FileSize;
		this.ThreadCount = ThreadCount;
		this.TaskStatus = DownloadStatus;
	}

	public void setTargetFile(String saveDir, String saveName) throws IOException {
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
		String fileType = getFileType();

		SaveFile = saveName + "." + fileType;
		File file = new File(SaveDirectory, SaveFile);
		
		int i = 1;
		while(file.exists() == true) {
			SaveFile = saveName + "(" + Integer.toString(i) + ")" + "." + fileType;
			file = new File(SaveDirectory, SaveFile);
			i += 1;
		}
		
		createDate = System.currentTimeMillis();
		file.createNewFile();
		
		ProgressFile = SaveFile.split("\\.")[0] + "_" + createDate + ".tmp";
		ProgressFolder = SaveFile.split("\\.")[0] + "_" + createDate;
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
	
	public String getSaveName() {
		return SaveFile;
	}
	
	public String getProgressFile() {
		return ProgressFile;
	}

	public String getProgressFolder() {
		return ProgressFolder;
	}

	public void setSaveDirectory(String SaveDirectory) {
		this.SaveDirectory = SaveDirectory;
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
			completedThread = 0;
			int size = getFileLength(Url);
			FileSize = size;
			int sublen = size / thread_count;
			
			System.out.println("split");
			File dir = new File(DownloadManager.getInstance().getDataDir(), ProgressFolder);
			if(dir.exists() == false) dir.mkdir();
			
			for (int i = 0; i < thread_count; i++) {
				int startPos = sublen * i;
				int endPos = (i == thread_count - 1) ? size-1	: (sublen * (i + 1) - 1);
						
				DownloadRunnable runnable = new DownloadRunnable(
						Url, dir.getAbsolutePath(), SaveFile + "_" + (i+1),
						startPos, endPos,
						TaskID, i+1);
				ListRunnable.add(runnable);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean resumeProgress() throws IOException {   //Khôi phục công việc các luồng		
		File file = new File(DownloadManager.getInstance().getDataDir(), ProgressFile);
		if(file.exists() == false) {
			ListRunnable.clear();
			return false;
		}
		System.out.println("resume");
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
			String line = reader.readLine();
			if (line == null) {
				throw new NullPointerException("Unexpected EOF");
			}
			
			int count = Integer.parseInt(line.trim());			//Số luồng của file
			ThreadCount = count;
			for (int i = 0; i < count; i++) {
				int ThreadID = Integer.parseInt(reader.readLine());
				String saveDir = reader.readLine();
				String saveFile = reader.readLine();
				int startPosition = Integer.parseInt(reader.readLine());
				int currentPosition = Integer.parseInt(reader.readLine());
				int endPosition = Integer.parseInt(reader.readLine());
				
				DownloadRunnable runnable = new DownloadRunnable(
						Url, saveDir, saveFile,
						startPosition, currentPosition, endPosition,
						TaskID, ThreadID);
				ListRunnable.add(runnable);
			}
			createDate = Long.parseLong(reader.readLine());
			reader.close();
			completedThread = 0;
			return true;
		}catch (Exception e) {
			ListRunnable.clear();
			return false;
		}
	}

	public void startTask() throws IOException {
		if(TaskStatus == Values.DOWNLOADING) return;
		setDownloadStatus(Values.DOWNLOADING);
			
		if (!resumeProgress()) {
			splitDownload(ThreadCount);
		}
		System.out.println(ThreadCount);
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
	
	public void notify(int ThreadID) throws IOException {
        System.out.println("*******Task ID " + TaskID + ": Thread " + ThreadID + " download complete *********");
        completedThread++;
//        ListRunnable.remove(getThreadByID(ThreadID));
        if(completedThread == ThreadCount) {
        	assemble();
        	System.out.println("\n--------Complete file " + SaveFile + " download--------\n");
        	TaskStatus = Values.FINISHED;
    		ListRunnable.clear();
        	deleteOldFile();
        }
	}
	
	public void assemble() {
		try {
			System.out.println("assemble " + ThreadCount + ", " + completedThread);
			
			File saveF = new File(SaveDirectory, SaveFile);
			if(saveF.exists() == false) saveF.createNewFile();
			else {
				if(saveF.length() != 0) {
					String saveName = SaveFile.split("\\.")[0], fileType = getFileType();
					int i = 1;
					while(saveF.exists() == true) {
						SaveFile = saveName + "(" + Integer.toString(i) + ")" + "." + fileType;
						saveF = new File(SaveDirectory, SaveFile);
						i += 1;
					}
					saveF.createNewFile();					
				};
			}
			
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(saveF));
			for(int i = 0; i < ThreadCount; i++) {
				DownloadRunnable r = ListRunnable.get(i);
				File file = new File(r.getSaveDir(), r.getSaveFile());
				if(file.exists()) {
					BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
					int s;
					while((s = is.read()) != -1) {
						os.write(s);
					}		
					is.close();
					file.delete();						
				}
			}
			os.close();
        	createDate = System.currentTimeMillis();
        	new File(ListRunnable.get(0).getSaveDir()).delete();
		} catch (IOException e) {
			e.printStackTrace();
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

	private String getFileType() throws IOException {
		URL url = new URL(this.Url);
		URLConnection connection = url.openConnection();
		return connection.getContentType().split("/")[1];
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
		int count = 0;
		for(DownloadRunnable r : ListRunnable) {
			if(!r.isFinished()) count++;
		}
		return count;
	}

	public int getFileSize() {
		return FileSize;
	}

	public void pause() throws IOException {
		if(TaskStatus != Values.DOWNLOADING) return;
		
		setDownloadStatus(Values.PAUSED);
		pauseAllThread();		
		storeProgress();
		ListRunnable.clear();
	}
	
	public void pauseAllThread() {
		for(int i = 0; i < ListRunnable.size(); i++) {
			ListRunnable.get(i).pause();
		}
	}

	private void setDownloadStatus(int status) throws IOException {
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
	
	public int getThreadCount() {
		return ThreadCount;
	}
	
	public void storeProgress() throws IOException { //Lưu tiến trình làm việc
		deleteOldFile();
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
			String s = "";
			for(DownloadRunnable i : ListRunnable) {
				s += i.getThreadID() + newLine + 
					 i.getSaveDir() + newLine +
					 i.getSaveFile() + newLine +
					 i.getStartPosition() + newLine +
					 i.getCurrentPosition() + newLine +
					 i.getEndPosition() + newLine;			
			}
			writer.write(s);	
			createDate = System.currentTimeMillis();
			writer.write(createDate + newLine);
			writer.close();
		} catch (Exception e) {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e1) {
			}
		}
	}

	public void cancel() throws IOException {
		pauseAllThread();
		ListRunnable.clear();
		deleteAllFile();
    	TaskStatus = Values.CANCELED;
    	System.out.println("Task ID: " + TaskID + " is canceled!");
	}
	
	public void deleteOldFile() throws IOException {
		File file = new File(DownloadManager.getInstance().getDataDir(), ProgressFile);
		if (file.exists()) {
			file.delete();
		}
	}
	
	public void deleteAllFile() throws IOException {
		File file = new File(DownloadManager.getInstance().getDataDir(), ProgressFile);
		if (file.exists()) {
			file.delete();
		}
				
		File directory = new File(DownloadManager.getInstance().getDataDir(), ProgressFolder);
		if(directory.exists()) {
			File[] files = directory.listFiles();
		    for(File subFile : files) { // delete each file from the director			    
		      subFile.delete();
		    }
	    
		    directory.delete();
	    }
	    
	    File saveF = new File(SaveDirectory, SaveFile);
		if(saveF.exists() && saveF.length() == 0) saveF.delete();
	}
}
