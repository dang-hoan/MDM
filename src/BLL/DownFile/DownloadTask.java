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
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
	private boolean fileNeedMerge = false;
	
	private long FileSize;
	private String FileType;
	private int ThreadCount;
	private int completedThread = 0;
	
	private ArrayList<DownloadRunnable> ListRunnable = new ArrayList<DownloadRunnable>();
	private long indexInSubFile = 0;
	private boolean finished = true;

	private JProgressBar[] jProgressBars;
	private speed_Download speed_Download;
	
	private long previousTimeLine;
	private long downloadTime = 0;

	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName, int ThreadCount, JProgressBar[] jProgressBars,speed_Download speed_Download, boolean fileNeedMerge) {
		this.TaskID = TaskID;
		this.Url = url;
		setTargetFile(saveDirectory, saveName);
		System.out.println("TaskID: " + TaskID);		
		this.jProgressBars = jProgressBars;
		this.speed_Download = speed_Download;
		this.FileSize = getFileLength(url);
		if(FileSize == -1) this.ThreadCount = 1;
		else this.ThreadCount = ThreadCount;
		this.fileNeedMerge = fileNeedMerge;
	}
	
	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName
			, String progressFile, String progressFolder, long FileSize, int ThreadCount
			, int DownloadStatus, long createDate, long downloadTime, boolean fileNeedMerge) {
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
		this.downloadTime = downloadTime;
		this.fileNeedMerge = fileNeedMerge;
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
			
			FileType = URLConnection.guessContentTypeFromName(saveName);
			if(FileType != null) {
				saveName = saveName.substring(0, saveName.lastIndexOf("."));
				System.out.println("Guess type C1: " + FileType);
				FileType = "." + FileType.split("/")[1];
			}
			else FileType = getFileType();

			SaveFile = saveName + FileType;
			File file = new File(SaveDirectory + File.separator + SaveFile);
			
			int i = 1;
			while(file.exists() == true) {
				SaveFile = saveName + "(" + Integer.toString(i) + ")" + FileType;
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
		long size = getFileLength(Url);
		FileSize = size;
		long sublen = size / thread_count;
		
		indexInSubFile = 0;
		
		System.out.println("split");
		File dir = new File(DownloadManager.getInstance().getDataDir(), ProgressFolder);
		if(dir.exists() == false) dir.mkdir();
		
		for (int i = 0; i < thread_count; i++) {
			long startPos = sublen * i;
			long endPos = (i == thread_count - 1) ? size-1	: (sublen * (i + 1) - 1);
			
			DownloadRunnable runnable = new DownloadRunnable(
					Url, dir.getAbsolutePath(), SaveFile + "_" + (i+1),
					startPos, endPos,
					TaskID, i+1, this.jProgressBars[i],this.speed_Download);
			ListRunnable.add(runnable);
		}
	}	
	
	private void singleDownload() {
		System.out.println("create single thread");
		FileSize = -1; ThreadCount = 1;
		File dir = new File(DownloadManager.getInstance().getDataDir(), ProgressFolder);
		if(dir.exists() == false) dir.mkdir();
		DownloadRunnable runnable = new DownloadRunnable(
				Url, dir.getAbsolutePath(), SaveFile,
				0, -1,
				TaskID, 1, this.jProgressBars[0],this.speed_Download);
		ListRunnable.add(runnable);
	}

	private boolean resumeProgress() throws IOException {   //Khôi phục công việc của các luồng
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
			createDate = Long.parseLong(reader.readLine());
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
			
			previousTimeLine = System.currentTimeMillis();
			
			TaskStatus = Values.DOWNLOADING;
			this.speed_Download.set_Check(Values.DOWNLOADING);
			
			if(ListRunnable.size() == 0 || finished) {	
				downloadTime = 0;
				if (!resumeProgress()) {
					if(FileSize < 0) singleDownload();
					else splitDownload(ThreadCount);
				}			
			}
			System.out.println("So luong: " + ListRunnable.size() + ", hoan thanh: " + completedThread);

			if(completedThread == ThreadCount && FileSize < 0) {
				moveFile();
			}
			else if(completedThread == ThreadCount) {
				TaskStatus = Values.ASSEMBLING;
				assemble();
			}
			else
				for (DownloadRunnable runnable : ListRunnable) {
					if(!runnable.isFinished() || FileSize < 0) runnable.start();
				}
//			SpeedTimer.scheduleAtFixedRate(SpeedMonitor, 0, 1000);
			
		}
		catch(IOException e) {
			e.printStackTrace();
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
        if(FileSize < 0){
			moveFile();
		}
		else if(completedThread == ThreadCount) assemble();
	}
	
	public void assemble() {
		try {
			System.out.println("assemble " + ThreadCount + ", " + completedThread);
			
			this.speed_Download.set_Check(Values.ASSEMBLING);
			
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
					int s; byte[] buf = new byte[1024*1024];
					while((s = is.read(buf, 0, buf.length)) != -1) {
						if(Thread.currentThread().isInterrupted()) {
							indexInSubFile = is.getFilePointer()-s;
							is.close();
							os.close();
				        	createDate = System.currentTimeMillis();
				        	System.out.println("Stop assembling!");
							return;
						}
						os.write(buf, 0, s);
					}		
					is.close();
					file.delete();						
				}
			}
			os.close();
        	createDate = System.currentTimeMillis();

    		//Di chuyển file từ thư mục tạm thời trong mdm sang thư mục đích
        	moveFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void moveFile() {
		if(fileNeedMerge) {
			System.out.println("need merge^^^^^^^^^^^^^^");
			
			//Cập nhật kiểu file nếu chưa có
			FileType = getFileType();
			if(FileType == "") {
				File desF = new File(SaveDirectory + File.separator + SaveFile);
				if(desF.exists() && desF.length() == 0) desF.delete();
				FileType = getFileType();
				System.out.println(FileType);
				SaveFile += FileType;
			}
			
			//Cập nhật tên file nếu tên đã tồn tại trong thư mục đích
			File desF = new File(SaveDirectory + File.separator + SaveFile);
			if(desF.exists() && desF.length() == 0) desF.delete();
			else {
				if(desF.length() != 0) {
					int index = SaveFile.lastIndexOf(".");
					String saveName = (index != -1)?SaveFile.substring(0, index):SaveFile;
					int i = 1;
					while(desF.exists() == true) {
						SaveFile = saveName + "(" + Integer.toString(i) + ")" + FileType;
						desF = new File(SaveDirectory + File.separator + SaveFile);
						i += 1;
					}
				}
			}			

			this.speed_Download.set_Check(Values.FINISHED);
			TaskStatus = Values.FINISHED;
			downloadTime += System.currentTimeMillis() - previousTimeLine;
			return; //Thoát mà k move file
		}
		File saveF = new File(DownloadManager.getInstance().getDataDir() + File.separator + ProgressFolder, SaveFile);
		File saveDir = new File(SaveDirectory);
		if (saveDir.exists() == false) {
			if (saveDir.mkdirs() == false) {
				throw new RuntimeException("Error to create directory");
			}
		}        	

		File desF;
		FileType = getFileType();
		if(FileType == "") {
			desF = new File(SaveDirectory + File.separator + SaveFile);
			if(desF.exists() && desF.length() == 0) desF.delete();
			FileType = getFileType();
			System.out.println(FileType);
			SaveFile += FileType;
		}
		desF = new File(SaveDirectory + File.separator + SaveFile);
		if(desF.exists() == false) {
			saveF.renameTo(desF);
		}
		else {
			if(desF.length() != 0) {
				int index = SaveFile.lastIndexOf(".");
				String saveName = (index != -1)?SaveFile.substring(0, index):SaveFile;
				int i = 1;
				while(desF.exists() == true) {
					SaveFile = saveName + "(" + Integer.toString(i) + ")" + FileType;
					desF = new File(SaveDirectory + File.separator + SaveFile);
					i += 1;
				}
			}
			else desF.delete();
			saveF.renameTo(desF);
		}    		
		
		this.speed_Download.set_Check(Values.FINISHED);
		System.out.println("\n--------Complete file " + SaveFile + " download--------\n");
		TaskStatus = Values.FINISHED;

		downloadTime += System.currentTimeMillis() - previousTimeLine;
		
		//Xoá thư mục tạm thời
		new File(ListRunnable.get(0).getSaveDir()).delete();
		
		ListRunnable.clear();
		deleteOldFile();
	}
	
	public void addPartedTask(DownloadRunnable runnable) {
		ListRunnable.add(runnable);
	}
	
	private long getFileLength(String fileUrl) {
		try {
			URI uri = new URI(fileUrl);
			URL url = uri.toURL();
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("Accept-Encoding", "identity");
			return connection.getContentLengthLong();
			
		}catch(IOException | URISyntaxException e) {
			return -1;
		}
	}

	private String getFileType() {
		try {
			URL url = new URL(this.Url);
			URLConnection connection = url.openConnection();
			String type = connection.getContentType();
			if(type != null) return "." + type.split("/")[1];			
			
			if(TaskStatus == Values.READY) return "";
			
			File dir = new File(DownloadManager.getInstance().getDataDir() + File.separator + ProgressFolder);
			if(dir.exists() == false) return "";
			
			File file = new File(dir.getAbsoluteFile() + File.separator + SaveFile);
			if(file.exists() == false) return "";			
			
			BufferedInputStream os = new BufferedInputStream(new FileInputStream(file));
			type = URLConnection.guessContentTypeFromStream(os);
			
			if(type != null) System.out.println("Guess type C2: " + type);
			os.close();
			if(type != null) return "." + type.split("/")[1];	
		 
			return "";
		}catch(IOException e) {
			return "";
		}
	}

	public File getDownloadFile() {
		return new File(SaveDirectory + File.separator + SaveFile);
	}

	public String getFilePath() {
		return DownloadManager.getInstance().getDataDir() + File.separator + ProgressFolder + File.separator + SaveFile;
	}
	
	public long getDownloadedSize() {
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

	public long getFileSize() {
		return FileSize;
	}
	
	public long getCurrentSize() {
		if(TaskStatus == Values.DOWNLOADING) return Math.min(getDownloadedSize(), FileSize);
		if(TaskStatus == Values.ASSEMBLING) return FileSize;
		if(TaskStatus == Values.FINISHED){	// chỉ cần lấy kích thước file đã tải xong
			File file = new File(SaveDirectory + File.separator +  SaveFile);
			if(file.exists()) return file.length();
		}
		if(TaskStatus == Values.PAUSED) {
			//Đã bật app và bắt đầu tải r
			if(ListRunnable.size() != 0) return Math.min(getDownloadedSize(), FileSize);
			//Mới bật app
			File dir = new File(DownloadManager.getInstance().getDataDir() + File.separator + ProgressFolder);
			
			// Đối với file không lấy được chiều dài khi kết nối tới server -> lấy kích thước file đang tải
			if(dir.exists() && FileSize == -1) {	
				File file = new File(dir.getAbsolutePath() + File.separator +  SaveFile);
				if(file.exists()) return file.length();
			}
			
			// Đối với file thông thường -> cộng tổng kích thước các file con
			else if(dir.exists()){									
				long sum = 0; int i = 2;
				File file = new File(dir.getAbsolutePath() + File.separator +  SaveFile + "_" + 1);
				while(file.exists()) {
					sum += file.length();
					file = new File(dir.getAbsolutePath() + File.separator +  SaveFile + "_" + i++);
				}
				return sum;
			}
		}
		return -1;
	}
	
	public long getDownloadTime() {
		return downloadTime;
	}
	
	public boolean isFileNeedMerge() {
		return fileNeedMerge;
	}

	public void pause() throws IOException {
		if(TaskStatus == Values.DOWNLOADING) {
			TaskStatus = Values.PAUSED;
			pauseAllThread();					// Khi các luồng con đang tải
		}
		if(TaskStatus == Values.ASSEMBLING) {
			TaskStatus = Values.PAUSED;
			Thread current = Thread.currentThread();
			current.interrupt(); 				// Khi đang ghép file
			try {
				current.join();					// Đợi cho các luồng thực hiện xong
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(this.speed_Download != null) this.speed_Download.set_Check(Values.PAUSED);
		finished = false;
		downloadTime += System.currentTimeMillis() - previousTimeLine;
	}
	
	public void pauseAllThread() {
		for(int i = 0; i < ListRunnable.size(); i++) {
			ListRunnable.get(i).pause();
		}
		
		//Đợi cho các luồng thực hiện xong r đã làm chuyện khác
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
		if(TaskStatus == Values.CANCELED || TaskStatus == Values.FINISHED) return;
		pause();
    	TaskStatus = Values.CANCELED;
		ListRunnable.clear();
		deleteAllFile();
    	System.out.println("Task ID: " + TaskID + " is canceled!");
	}
	
	public void deleteOldFile() {
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
	
	public void set_Status(int status)
	{
		if(status<1&&status>7)
		{
			return;
		}
		else
		{
			this.TaskStatus= status;
		}
	}
	
	public boolean checkFile(String type, String checksum) {
		try {
			List<MessageDigest> md = new ArrayList<>();
			switch(type) {
				case "All":{
					md.add(MessageDigest.getInstance("MD5"));
					md.add(MessageDigest.getInstance("SHA1"));
					md.add(MessageDigest.getInstance("SHA256"));
					if(getChecksum(md)) {
						for(int i = 0; i < md.size(); i++)
							if(checksum.equals(calcChecksum(md.get(i)))) return true;
					}
					break;
				}
				case "MD5":{
					md.add(MessageDigest.getInstance("MD5"));
					if(getChecksum(md)) {
						if(checksum.equals(calcChecksum(md.get(0)))) return true;
					}
					break;
				}
				case "SHA-1":{
					md.add(MessageDigest.getInstance("SHA-1"));
					if(getChecksum(md)) {
						if(checksum.equals(calcChecksum(md.get(0)))) return true;
					}
					break;
				}
				case "SHA-256":{
					md.add(MessageDigest.getInstance("SHA-256"));
					if(getChecksum(md)) {
						if(checksum.equals(calcChecksum(md.get(0)))) return true;
					}
					break;
				}
			}
		}catch(NoSuchAlgorithmException e) {
			return false;			
		}
		return false;
	}

	private boolean getChecksum(List<MessageDigest> md) {
		try {
			File saveF = new File(DownloadManager.getInstance().getDataDir() + File.separator + ProgressFolder, SaveFile);
			if(saveF.exists() == false) {
				saveF = new File(SaveDirectory + File.separator + SaveFile);
				if(saveF.exists() == false) return false;
			}
			
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(saveF));
			int s; byte[] buf = new byte[1024*1024];
			while((s = is.read(buf, 0, buf.length)) != -1) {
				for(int i = 0; i < md.size(); i++)
					md.get(i).update(buf, 0, s);
			}		
			is.close();	
			return true;
		}catch(IOException e) {
			return false;
		}
	}
	
	public String calcChecksum(MessageDigest md) {
		String result = "";
	    byte[] b = md.digest();
	
	    for (int i=0; i < b.length; i++) {
	        result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	    }
	   
	    return result;
	}
}

//test
//https://th.bing.com/th/id/R.927deebd63a9b90134ee662041e51272?rik=wIu7IyPyWYCAmQ&riu=http%3a%2f%2f2.bp.blogspot.com%2f-ccVAHYlUyP4%2fT8iJzBsa4PI%2fAAAAAAAAD2A%2fY9s6I5YaisE%2fs1600%2fdesktop-wallpaper-40.jpg&ehk=v7KSzP%2bbCxdVQHjYGxm3vwcoFHA992W3qj2J6Km%2bvio%3d&risl=&pid=ImgRaw&r=0
