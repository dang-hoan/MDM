package BLL.DownFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.JProgressBar;

import BLL.Values;
import BLL.VideoConversion.FFmpeg;

public class DownloadManager {
	private String DataFile = "downloads.tmp";
	private String DataDir = System.getProperty("user.home") + File.separator + ".mdm";
	private static DownloadManager instance;
	private Hashtable<Integer, DownloadTask> Tasks = new Hashtable<Integer, DownloadTask>();
	private Hashtable<Integer, DownloadTask[]> VideoTasks = new Hashtable<Integer, DownloadTask[]>();

	private DownloadManager() {
	}

	public static DownloadManager getInstance(){
		if (instance == null) {
			instance = new DownloadManager();
		}
		return instance;
	}

	public void setMaxThreadCount(int MaxCount) {
		if (MaxCount > 0)
			Values.MAX_THREAD_COUNT = MaxCount;
	}
	
	//length = -1
	//https://wallup.net/wp-content/uploads/2019/09/296096-sunset-mountains-ocean-landscapes-nature-travel-hdr-photography-blue-skies-skies-cloud.jpg
	
	public DownloadTask addTask(String url, String saveDirectory, String saveName, 
			int ThreadCount, Boolean now, JProgressBar[] jProgressBars, speed_Download speedDownload, boolean fileNeedMerge) {
		if(ThreadCount > Values.MAX_THREAD_COUNT || ThreadCount < Values.MIN_THREAD_COUNT) {
			ThreadCount = Values.DEFAULT_THREAD_COUNT;
		}
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER++, url, saveDirectory, saveName, ThreadCount,jProgressBars,speedDownload,fileNeedMerge);
		addTask(downloadTask);
		
		if(now == true) downloadTask.startTask();
		
		return downloadTask;
	}
	
	public DownloadTask[] addTask(String url, String url2, String saveDirectory, String saveName, 
			int ThreadCount, int ThreadCount2, Boolean now, JProgressBar[] jProgressBars, speed_Download speedDownload,
			JProgressBar[] jProgressBars2, speed_Download speedDownload2) {
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER, url, saveDirectory, saveName, ThreadCount,jProgressBars,speedDownload, true);
		DownloadTask downloadTask2 = new DownloadTask(Values.Task_ID_COUNTER++, url2, saveDirectory, saveName, ThreadCount2,jProgressBars2,speedDownload2, true);
		addTask(downloadTask, downloadTask2);
		
		if(now == true) {
			downloadTask.startTask();
			downloadTask2.startTask();
		}
		
		return new DownloadTask[]{downloadTask, downloadTask2};
	}
	
	public void addTask(DownloadTask downloadTask) {
		Tasks.put(downloadTask.getTaskID(), downloadTask);
	}

	public void addTask(DownloadTask downloadTask, DownloadTask downloadTask2) {
		VideoTasks.put(downloadTask.getTaskID(), new DownloadTask[] {downloadTask, downloadTask2});
	}

	public boolean checkFile(int TaskID, String type, String checksum) {
		DownloadTask task = Tasks.get(TaskID);
		if(task != null) return task.checkFile(type, checksum);
		return VideoTasks.get(TaskID)[0].checkFile(type, checksum);
	}
	
	public DownloadTask getTask(int TaskID) {
		return Tasks.get(TaskID);
	}
	
	public DownloadTask getTask(int TaskID, speed_Download sp) {
		DownloadTask t = Tasks.get(TaskID);
		if(t != null) return t;
		DownloadTask[] v = VideoTasks.get(TaskID);
		if(v[0].getSpeed_Download() == sp) return v[0];
		else return v[1];
				
	}

	public void startTask(int TaskID) {
		for (DownloadTask task : Tasks.values()) {
			if(task.getTaskID() == TaskID) {
				task.startTask();
				break;
			}
		}
		for(DownloadTask[] tasks : VideoTasks.values()) {
			if(tasks[0].getTaskID() == TaskID) {
				tasks[0].startTask();
				tasks[1].startTask();
				break;
			}
		}
	}
	
	public void startAll() throws IOException {
		for (DownloadTask task : Tasks.values()) {
			if(task.getDownloadStatus() == Values.READY)
				task.startTask();
		}
		for(DownloadTask[] tasks : VideoTasks.values()) {
			if(tasks[0].getDownloadStatus() == Values.READY) {
				tasks[0].startTask();
				tasks[1].startTask();
			}
		}
	}
	
	public Boolean isAllTasksFinished() {
		for (DownloadTask task : Tasks.values()) {
			if(task.getDownloadStatus() != Values.FINISHED) return false;
		}
		for (DownloadTask[] tasks : VideoTasks.values()) {
			if(tasks[0].getDownloadStatus() != Values.FINISHED || tasks[1].getDownloadStatus() != Values.FINISHED) return false;
		}
		return true;
	}

	public Boolean isTaskFinished(int task_id) {
		DownloadTask task = Tasks.get(task_id);
		if(task != null) return task.getDownloadStatus() == Values.FINISHED;
		DownloadTask[] tasks = VideoTasks.get(task_id);
		if(tasks != null) return (tasks[0].getDownloadStatus() == Values.FINISHED && tasks[1].getDownloadStatus() == Values.FINISHED);
		return false;
	}

	public void pauseAllTasks() throws IOException {
		for (DownloadTask task : Tasks.values()) {
			task.pause();
		}
		for (DownloadTask[] tasks : VideoTasks.values()) {
			tasks[0].pause();
			tasks[1].pause();
		}
	}

	public void pauseTask(int TaskID) throws IOException {
		DownloadTask task = Tasks.get(TaskID);
		if(task != null) task.pause();
		DownloadTask[] tasks = VideoTasks.get(TaskID);
		if(tasks != null) {
			tasks[0].pause();
			tasks[1].pause();
		}
	}
	
	public void shutdown() throws IOException {
		pauseAllTasks();
		for (DownloadTask task : Tasks.values()) {
			task.shutdown();
		}
		for (DownloadTask[] tasks : VideoTasks.values()) {
			tasks[0].shutdown();
			tasks[1].shutdown();
		}
		storeTasks();
	}
	public void shutdownRudely() throws IOException {
		pauseAllTasks();
	}
	
	public void resumeTasks() throws IOException {   				//Khôi phục thông tin các task (file)
		File file = new File(DataDir, DataFile);
		if (file.exists() == false) return;

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
			String line = reader.readLine();
			if (line == null) {
				throw new NullPointerException("Unexpected EOF");
			}
			int count = Integer.parseInt(line.trim());
			for (int i = 0; i < count; i++) {
				String url = reader.readLine();
				String SaveName = reader.readLine();
				String SaveDirectory = reader.readLine();
				String ProgressFile = reader.readLine();
				String ProgressFolder = reader.readLine();
				long FileSize = Integer.parseInt(reader.readLine());
				int ThreadCount = Integer.parseInt(reader.readLine());
				int DownloadStatus = Integer.parseInt(reader.readLine());
				Long createDate = Long.parseLong(reader.readLine());
				Long downloadTime = Long.parseLong(reader.readLine());
				boolean fileNeedMerge = Boolean.parseBoolean(reader.readLine());

				DownloadTask task = new DownloadTask(
						Values.Task_ID_COUNTER++,
						url, SaveDirectory, SaveName, ProgressFile, ProgressFolder, FileSize, ThreadCount, DownloadStatus, createDate, downloadTime, fileNeedMerge);
				addTask(task);
			}
			line = reader.readLine();
			if (line != null) {
				count = Integer.parseInt(line.trim());
				for (int i = 0; i < count; i++) {
					String url = reader.readLine();
					String SaveName = reader.readLine();
					String SaveDirectory = reader.readLine();
					String ProgressFile = reader.readLine();
					String ProgressFolder = reader.readLine();
					long FileSize = Integer.parseInt(reader.readLine());
					int ThreadCount = Integer.parseInt(reader.readLine());
					int DownloadStatus = Integer.parseInt(reader.readLine());
					Long createDate = Long.parseLong(reader.readLine());
					Long downloadTime = Long.parseLong(reader.readLine());

					DownloadTask task = new DownloadTask(
							Values.Task_ID_COUNTER,
							url, SaveDirectory, SaveName, ProgressFile, ProgressFolder, FileSize, ThreadCount, DownloadStatus, createDate, downloadTime, true);
					
					url = reader.readLine();
					SaveName = reader.readLine();
					ProgressFile = reader.readLine();
					ProgressFolder = reader.readLine();
					FileSize = Integer.parseInt(reader.readLine());
					ThreadCount = Integer.parseInt(reader.readLine());
					DownloadStatus = Integer.parseInt(reader.readLine());
					createDate = Long.parseLong(reader.readLine());
					downloadTime = Long.parseLong(reader.readLine()); 
					
					DownloadTask task2 = new DownloadTask(
							Values.Task_ID_COUNTER++,
							url, SaveDirectory, SaveName, ProgressFile, ProgressFolder, FileSize, ThreadCount, DownloadStatus, createDate, downloadTime, true);
					
					addTask(task, task2);
				}
			}
			reader.close();
		}catch (Exception e) {
			
		}
	}
	
	public void storeTasks() throws IOException {
		File dir = new File(DataDir);
		if (dir.exists() == false) {
			dir.mkdir();
		}

		File file = new File(DataDir, DataFile);
		if (file.exists() == false) {
			file.createNewFile();
		}
		BufferedWriter writer = null;
		
		String newLine = System.getProperty("line.separator");
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8")));
			
			//ghi file thường
			writer.write(Integer.toString(Tasks.size()));		
			writer.newLine();
			String s = "";
			for(int i = 0; i < Tasks.size(); i++) 
			{
				DownloadTask t = Tasks.get(i);
				s += t.getUrl() + newLine +
					 t.getSaveName() + newLine +
					 t.getSaveDirectory() + newLine +
					 t.getProgressFile() + newLine +
					 t.getProgressFolder() + newLine +
					 t.getFileSize() + newLine +
					 t.getThreadCount() + newLine +
					 t.getDownloadStatus() + newLine +
					 t.getCreateDate() + newLine + 
					 t.getDownloadTime() + newLine +			
					 t.isFileNeedMerge() + newLine;		
			}
			writer.write(s);
			
			//ghi video
			writer.write(Integer.toString(VideoTasks.size()));		
			writer.newLine();
			s = "";
			for(int i = 0; i < VideoTasks.size(); i++) 
			{
				DownloadTask[] v = VideoTasks.get(i);
				s += v[0].getUrl() + newLine +
					 v[0].getSaveName() + newLine +
					 v[0].getSaveDirectory() + newLine +
					 v[0].getProgressFile() + newLine +
					 v[0].getProgressFolder() + newLine +
					 v[0].getFileSize() + newLine +
					 v[0].getThreadCount() + newLine +
					 v[0].getDownloadStatus() + newLine +
					 v[0].getCreateDate() + newLine + 
					 v[0].getDownloadTime() + newLine +	
					 v[1].getUrl() + newLine +
					 v[1].getSaveName() + newLine +	
					 v[1].getProgressFile() + newLine +
					 v[1].getProgressFolder() + newLine +
					 v[1].getFileSize() + newLine +
					 v[1].getThreadCount() + newLine +
					 v[1].getDownloadStatus() + newLine +
					 v[1].getCreateDate() + newLine + 
					 v[1].getDownloadTime() + newLine;		
			}
			writer.write(s);
//			System.out.println(s);
			writer.close();
		} catch (Exception e) {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e1) {
			}
		}
	}

	public void cancelAllTasks() throws IOException {
		for(DownloadTask Task : Tasks.values()) {
			Task.cancel();
		}
		for(DownloadTask[] Tasks : VideoTasks.values()) {
			Tasks[0].cancel();
			Tasks[1].cancel();
		}
	}

	public void cancelTask(int TaskID) throws IOException {
		if (Tasks.containsKey(TaskID)) {
			DownloadTask Task = Tasks.get(TaskID);
			Task.cancel();
		}
		if (VideoTasks.containsKey(TaskID)) {
			DownloadTask[] Tasks = VideoTasks.get(TaskID);
			Tasks[0].cancel();
			Tasks[1].cancel();
		}
	}
	
	public void deleteTask(int TaskID) throws IOException {
		if (Tasks.containsKey(TaskID)) {
			Tasks.remove(TaskID);
		}
		if (VideoTasks.containsKey(TaskID)) {
			VideoTasks.remove(TaskID);
		}
	}

	public String getDataDir() {
		return DataDir;
	}
	
	public int getTotalDownloadedSize() {
		int size = 0;
		for (DownloadTask Task : Tasks.values()) {
			size += Task.getDownloadedSize();
		}
		for (DownloadTask[] Tasks : VideoTasks.values()) {
			size += Tasks[0].getDownloadedSize();
			size += Tasks[1].getDownloadedSize();
		}
		return size;
	}

	public String getReadableDownloadSize() {
		return DownloadUtils.getReadableSize(getTotalDownloadedSize());
	}
	
	public int mergeFile(String path1, String path2, String fileName) {
		return new FFmpeg(Arrays.asList(path1, path2), fileName).convert();
	}
		
}
