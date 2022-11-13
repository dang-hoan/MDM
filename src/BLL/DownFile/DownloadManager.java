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
import java.util.Hashtable;

import BLL.Values;

public class DownloadManager {
	private String DataFile = "downloads.tmp";
	private String DataDir = System.getProperty("user.home") + File.separator + ".mdm";
	private static DownloadManager instance;
	private Hashtable<Integer, DownloadTask> Tasks = new Hashtable<Integer, DownloadTask>();

	private DownloadManager() {
	}

	public static DownloadManager getInstance() throws IOException {
		if (instance == null) {
			instance = new DownloadManager();
			instance.resumeTasks();
		}
		return instance;
	}

//	public void setMaxThreadCount(int MaxCount) {
//		if (MaxCount > 0)
//			Values.MAX_THREAD_COUNT = MaxCount;
//	}
	//length = -1 ??
	//https://wallup.net/wp-content/uploads/2019/09/296096-sunset-mountains-ocean-landscapes-nature-travel-hdr-photography-blue-skies-skies-cloud.jpg
	
	public void addTask(String url, String saveDirectory, String saveName, int ThreadCount, Boolean now) throws IOException {
		if(ThreadCount > Values.MAX_THREAD_COUNT || ThreadCount < Values.MIN_THREAD_COUNT) {
			ThreadCount = Values.DEFAULT_THREAD_COUNT;
		}
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER++, url, saveDirectory, saveName, ThreadCount);
		addTask(downloadTask);
		if(now == true) downloadTask.startTask();
	}

	public void addTask(DownloadTask downloadTask) {
		Tasks.put(downloadTask.getTaskID(), downloadTask);
	}

	public DownloadTask getTask(int TaskID) {
		return Tasks.get(TaskID);
	}

	public void startTask(int TaskID) throws IOException {
		for (DownloadTask task : Tasks.values()) {
			if(task.getTaskID() == TaskID) {
				task.startTask();
				break;
			}
		}
	}
	
	public void startAll() throws IOException {
		for (DownloadTask task : Tasks.values()) {
			if(task.getDownloadStatus() == Values.READY)
				task.startTask();
		}
	}
	
	public Boolean isAllTasksFinished() {
		for (DownloadTask task : Tasks.values()) {
			if(task.getDownloadStatus() != Values.FINISHED) return false;
		}
		return true;
	}

	public Boolean isTaskFinished(int task_id) {
		DownloadTask task = Tasks.get(task_id);
		return task.getDownloadStatus() == Values.FINISHED;
	}

	public void pauseAllTasks() throws IOException {
		for (Integer taskID : Tasks.keySet()) {
			pauseTask(taskID);
		}
		storeTasks();
	}

	public void pauseTask(int TaskID) throws IOException {
		if (Tasks.containsKey(TaskID)) {
			DownloadTask task = Tasks.get(TaskID);
			task.pause();
		}
	}
	
	private void resumeTasks() throws IOException {   				//Khôi phục thông tin các task (file)
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
				int FileSize = Integer.parseInt(reader.readLine());
				int ThreadCount = Integer.parseInt(reader.readLine());
				int DownloadStatus = Integer.parseInt(reader.readLine());
				Long createDate = Long.parseLong(reader.readLine());
				DownloadTask task = new DownloadTask(
						Values.Task_ID_COUNTER++,
						url, SaveDirectory, SaveName, ProgressFile, ProgressFolder, FileSize, ThreadCount, DownloadStatus, createDate);
				addTask(task);
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
			writer.write(Integer.toString(Tasks.size()));		//Số file
			writer.newLine();
			String s = "";
			for(DownloadTask i : Tasks.values()) {
				s += i.getUrl() + newLine +
				     i.getSaveName() + newLine +
				     i.getSaveDirectory() + newLine +
				     i.getProgressFile() + newLine +
				     i.getProgressFolder() + newLine +
				     i.getFileSize() + newLine +
				     i.getThreadCount() + newLine +
				     i.getDownloadStatus() + newLine +
				     i.getCreateDate() + newLine;			
			}
			writer.write(s);	
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

	public void cancelAllTasks() throws IOException {
		for(DownloadTask Task : Tasks.values()) {
			Task.cancel();
		}
	}

	public void cancelTask(int TaskID) throws IOException {
		if (Tasks.containsKey(TaskID)) {
			DownloadTask Task = Tasks.get(TaskID);
			Task.cancel();
		}
	}

	public void shutdownSafely() throws IOException {
		for (Integer task_id : Tasks.keySet()) {
			Tasks.get(task_id).pause();
		}
		storeTasks();
//		mThreadPool.shutdown();
	}

	public String getDataDir() {
		return DataDir;
	}
	
	public int getTotalDownloadedSize() {
		int size = 0;
		for (DownloadTask Task : Tasks.values()) {
			size += Task.getDownloadedSize();
		}
		return size;
	}

	public String getReadableDownloadSize() {
		return DownloadUtils.getReadableSize(getTotalDownloadedSize());
	}

	public int getTotalSpeed() {
		int speed = 0;
		for (DownloadTask Task : Tasks.values()) {
			speed += Task.getSpeed();
		}
		return speed;
	}

	public String getReadableTotalSpeed() {
		return DownloadUtils.getReadableSpeed(getTotalSpeed());
	}
		
	public void shutdDownloadRudely() {
//		mThreadPool.shutdownNow();
	}
}
