package BLL.DownFile;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.swing.JProgressBar;

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
	private long indexInSubFile = 0;
	private boolean finished = true;

	private JProgressBar[] jProgressBars;
	private speed_Download speed_Download;

	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName, int ThreadCount, JProgressBar[] jProgressBars,speed_Download speed_Download) {
		this.TaskID = TaskID;
		this.Url = url;
		setTargetFile(saveDirectory, saveName);
		this.ThreadCount = ThreadCount;
		System.out.println("TaskID: " + TaskID);		
		this.jProgressBars = jProgressBars;
		this.speed_Download = speed_Download;
	}
	
	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName, String progressFile, String progressFolder, int FileSize, int ThreadCount, int DownloadStatus, long createDate) {
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

	public void setTargetFile(String saveDir, String saveName) {
		try {
			
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
			File file = new File(SaveDirectory + File.separator + SaveFile);
			
			int i = 1;
			while(file.exists() == true) {
				SaveFile = saveName + "(" + Integer.toString(i) + ")" + "." + fileType;
				file = new File(SaveDirectory + File.separator + SaveFile);
				i += 1;
			}
			file.createNewFile();
			createDate = System.currentTimeMillis();
			
			ProgressFile = SaveFile.split("\\.")[0] + "_" + createDate + ".tmp";
			ProgressFolder = SaveFile.split("\\.")[0] + "_" + createDate;
			
			file = new File(DownloadManager.getInstance().getDataDir());
			file.mkdir();
			
			file = new File(DownloadManager.getInstance().getDataDir(), ProgressFolder);
			file.mkdir();
			
			file = new File(file.getAbsolutePath(), SaveFile);
			file.createNewFile();
			
			
		}
		catch(IOException e) {
			
		}
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
			int size = getFileLength(Url);
			FileSize = size;
			int sublen = size / thread_count;
			
			indexInSubFile = 0;
			
			System.out.println("split");
			File dir = new File(DownloadManager.getInstance().getDataDir(), ProgressFolder);
			if(dir.exists() == false) dir.mkdir();
			
			for (int i = 0; i < thread_count; i++) {
				int startPos = sublen * i;
				int endPos = (i == thread_count - 1) ? size-1	: (sublen * (i + 1) - 1);
						
				DownloadRunnable runnable = new DownloadRunnable(
						Url, dir.getAbsolutePath(), SaveFile + "_" + (i+1),
						startPos, endPos,
						TaskID, i+1, this.jProgressBars[i],this.speed_Download);
				ListRunnable.add(runnable);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean resumeProgress() throws IOException {   //Khôi phục công việc các luồng	
		File file = new File(DownloadManager.getInstance().getDataDir(), ProgressFile);
		if(file.exists() == false) {
			completedThread = 0;
			ListRunnable.clear();
			return false;
		}
		
		System.out.println("resume");
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
			this.completedThread = Integer.parseInt(reader.readLine());				//Số luồng hoàn thành
			String saveDir = reader.readLine();
			for (int i = 0; i < this.ThreadCount; i++) {
				int ThreadID = Integer.parseInt(reader.readLine());
				String saveFile = reader.readLine();
				int startPosition = Integer.parseInt(reader.readLine());
				int currentPosition = Integer.parseInt(reader.readLine());
				int endPosition = Integer.parseInt(reader.readLine());
				
				DownloadRunnable runnable = new DownloadRunnable(
						Url, saveDir, saveFile,
						startPosition, currentPosition, endPosition,
						TaskID, ThreadID,this.jProgressBars[i],this.speed_Download);
				ListRunnable.add(runnable);
			}
			this.indexInSubFile = Integer.parseInt(reader.readLine());
//			createDate = Long.parseLong(reader.readLine());
			reader.close();
			return true;
		}catch (Exception e) {
			completedThread = 0;
			ListRunnable.clear();
			return false;
		}
	}

	public void startTask() {
		try {

			if(TaskStatus == Values.DOWNLOADING || TaskStatus == Values.ASSEMBLING) return;
			
			TaskStatus = Values.DOWNLOADING;
			if(ListRunnable.size() == 0 || finished) {			
				if (!resumeProgress()) {
					splitDownload(ThreadCount);
				}			
			}
			System.out.println("So luong: " + ListRunnable.size() + ", hoan thanh: " + completedThread);

			if(completedThread == ThreadCount) {
				TaskStatus = Values.ASSEMBLING;
				assemble();
			}
			else
				for (DownloadRunnable runnable : ListRunnable) {
					if(!runnable.isFinished()) runnable.start();
				}
//			SpeedTimer.scheduleAtFixedRate(SpeedMonitor, 0, 1000);
			
		}
		catch(IOException e) {
			
		}
	}
	
	public int getThreadByID(int ThreadID) {
		for(int i = 0; i < ListRunnable.size(); i++) {
			if(ListRunnable.get(i).getThreadID() == ThreadID)
				return i;
		}
		return -1;
	}
	
	public synchronized void notify(int ThreadID) throws IOException {
        System.out.println("*******Task ID " + TaskID + ": Thread " + ThreadID + " download complete *********");
        completedThread++;
//        ListRunnable.remove(getThreadByID(ThreadID));
        if(completedThread == ThreadCount) assemble();
	}
	
	public void assemble() {
		try {
			System.out.println("assemble " + ThreadCount + ", " + completedThread);
			
			File saveF = new File(DownloadManager.getInstance().getDataDir() + File.separator + ProgressFolder, SaveFile);
			if(saveF.exists() == false) saveF.createNewFile();
			
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(saveF, true));
			boolean Seeked = false;
			for(int i = 0; i < ThreadCount; i++) {
				DownloadRunnable r = ListRunnable.get(i);
				File file = new File(r.getSaveDir(), r.getSaveFile());
				if(file.exists()) {
					RandomAccessFile is = new RandomAccessFile(file, "r");
					if(!Seeked && indexInSubFile > 0) {is.seek(indexInSubFile); Seeked = true;}
					int s;
					while((s = is.read()) != -1) {
						if(Thread.currentThread().isInterrupted()) {
							indexInSubFile = is.getFilePointer()-1;
							is.close();
							os.close();
				        	createDate = System.currentTimeMillis();
				        	System.out.println("Stop assembling!");
							return;
						}
						os.write(s);
					}		
					is.close();
					file.delete();						
				}
			}
			os.close();
        	createDate = System.currentTimeMillis();
    		
        	//Di chuyển file từ thư mục tạm thời trong mdm sang thư mục đích
        	File saveDir = new File(SaveDirectory);
    		if (saveDir.exists() == false) {
    			if (saveDir.mkdirs() == false) {
    				throw new RuntimeException("Error to create directory");
    			}
    		}        	
        	
    		File desF = new File(SaveDirectory + File.separator + SaveFile);
			if(desF.exists() == false) saveF.renameTo(desF);
			else {
				if(desF.length() != 0) {
					String saveName = SaveFile.split("\\.")[0], fileType = getFileType();
					int i = 1;
					while(desF.exists() == true) {
						SaveFile = saveName + "(" + Integer.toString(i) + ")" + "." + fileType;
						desF = new File(SaveDirectory + File.separator + SaveFile);
						i += 1;
					}				
				};
				desF.delete();
				saveF.renameTo(desF);
			}    		
			
			this.speed_Download.set_Check(true);
        	System.out.println("\n--------Complete file " + SaveFile + " download--------\n");
        	TaskStatus = Values.FINISHED;

			//Xoá thư mục tạm thời
        	new File(ListRunnable.get(0).getSaveDir()).delete();
        	
    		ListRunnable.clear();
        	deleteOldFile();
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
		connection.setRequestProperty("Accept-Encoding", "identity");
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
		if(TaskStatus == Values.DOWNLOADING) {
			TaskStatus = Values.PAUSED;
			pauseAllThread();					// Khi các luồng con còn đang tải
		}
		if(TaskStatus == Values.ASSEMBLING) {
			TaskStatus = Values.PAUSED;
			Thread current = Thread.currentThread();
			current.interrupt(); 				// Khi đang ghép file	
			try {
				current.join();					// Đợi cho luồng đã dừng xong r đã làm chuyện khác
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		finished = false;
	}
	
	public void pauseAllThread() {
		for(int i = 0; i < ListRunnable.size(); i++) {
			ListRunnable.get(i).pause();
		}
		
		//Đợi cho các luồng đã dừng xong r đã làm chuyện khác
		for(int i = 0; i < ListRunnable.size(); i++)
			try {
				ListRunnable.get(i).join();
				
			} catch (InterruptedException e) {
				continue;
			}
	}
	
	public void shutdown() throws IOException {
		storeProgress();
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
		if(TaskStatus != Values.PAUSED) return;
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
			writer.write(Integer.toString(completedThread));
			writer.newLine();
			String s = "";
			if(ListRunnable.get(0) != null) s += ListRunnable.get(0).getSaveDir() + newLine;
			for(DownloadRunnable i : ListRunnable) {
				s += i.getThreadID() + newLine +
					 i.getSaveFile() + newLine +
					 i.getStartPosition() + newLine +
					 i.getCurrentPosition() + newLine +
					 i.getEndPosition() + newLine;			
			}
			s += indexInSubFile + newLine;
			writer.write(s);	
			createDate = System.currentTimeMillis();
//			writer.write(createDate + newLine);
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
		if(TaskStatus == Values.CANCELED || TaskStatus == Values.FINISHED) return;
		pause();
    	TaskStatus = Values.CANCELED;
		ListRunnable.clear();
		deleteAllFile();
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
	    
	    File saveF = new File(SaveDirectory + File.separator + SaveFile);
		if(saveF.exists() && saveF.length() == 0) saveF.delete();
	}
	
	public void setJProgressBar(JProgressBar[] jProgressBars) {
		this.jProgressBars = jProgressBars;
	}
	
	public void setSpeed_Download(speed_Download speed_Download) {
		this.speed_Download = speed_Download;
	}
	
}

//test
//https://th.bing.com/th/id/R.927deebd63a9b90134ee662041e51272?rik=wIu7IyPyWYCAmQ&riu=http%3a%2f%2f2.bp.blogspot.com%2f-ccVAHYlUyP4%2fT8iJzBsa4PI%2fAAAAAAAAD2A%2fY9s6I5YaisE%2fs1600%2fdesktop-wallpaper-40.jpg&ehk=v7KSzP%2bbCxdVQHjYGxm3vwcoFHA992W3qj2J6Km%2bvio%3d&risl=&pid=ImgRaw&r=0
