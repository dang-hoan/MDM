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
//			instance.resumeTasks();
		}
		return instance;
	}

//	public void setMaxThreadCount(int MaxCount) {
//		if (MaxCount > 0)
//			ThreadPool.setCorePoolSize(MaxCount);
//	}
	//length = -1 ??
	//https://wallup.net/wp-content/uploads/2019/09/296096-sunset-mountains-ocean-landscapes-nature-travel-hdr-photography-blue-skies-skies-cloud.jpg
	
	public void addTask(String url, String saveDirectory, String saveName, int ThreadCount, Boolean now) throws IOException {
		if(ThreadCount > Values.MAX_THREAD_COUNT || ThreadCount < Values.MIN_THREAD_COUNT) {
			ThreadCount = Values.DEFAULT_THREAD_COUNT;
		}
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER++, url, saveDirectory, saveName, ThreadCount, Values.dateFormat.format(System.currentTimeMillis()));
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
		for (Integer task_id : Tasks.keySet()) {
			if (isTaskFinished(task_id) == false) {
				return false;
			}
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
				int FileSize = Integer.parseInt(reader.readLine());
				String SaveDirectory = reader.readLine();
				int DownloadStatus = Integer.parseInt(reader.readLine());
				String createDate = reader.readLine();
				DownloadTask task = new DownloadTask(
						Values.Task_ID_COUNTER++,
						url, SaveDirectory, SaveName, FileSize, DownloadStatus, createDate);
				addTask(task);
			}
			reader.close();
			file.delete();
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
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
			writer.write(Integer.toString(Tasks.size()));
			writer.newLine();
			for(DownloadTask i : Tasks.values()) {
				String s = i.getUrl() + newLine +
						   i.getSaveName() + newLine +
						   i.getFileSize() + newLine +
						   i.getSaveDirectory() + newLine +
						   i.getDownloadStatus() + newLine +
						   i.getCreateDate() + newLine;
				writer.write(s);				
			}
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

	public void cancelAllTasks() {
		for (Integer taskID : Tasks.keySet()) {
			cancelTask(taskID);
		}
	}

	public void cancelTask(int TaskID) {
		if (Tasks.containsKey(TaskID)) {
			DownloadTask Task = Tasks.remove(TaskID);
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
