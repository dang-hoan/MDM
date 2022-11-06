package BLL.DownFile;

import java.io.IOException;
import java.util.Hashtable;

import BLL.Values;

public class DownloadManager {
	private static DownloadManager instance;
	private Hashtable<Integer, DownloadTask> mTasks = new Hashtable<Integer, DownloadTask>();

	private DownloadManager() {
	}

	public static DownloadManager getInstance() {
		if (instance == null) {
			instance = new DownloadManager();
		}
		return instance;
	}

//	public void setMaxThreadCount(int MaxCount) {
//		if (MaxCount > 0)
//			mThreadPool.setCorePoolSize(MaxCount);
//	}
	
	public void addTask(String url, String saveDirectory, String saveName, int ThreadCount, Boolean now) throws IOException {
		if(ThreadCount > Values.MAX_THREAD_COUNT || ThreadCount < Values.MIN_THREAD_COUNT) {
			ThreadCount = Values.DEFAULT_THREAD_COUNT;
		}
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER++, url, saveDirectory, saveName, ThreadCount);
		addTask(downloadTask);
		if(now == true) downloadTask.startTask();
	}

	public void addTask(DownloadTask downloadTask) {
		mTasks.put(downloadTask.getTaskID(), downloadTask);
	}

	public DownloadTask getTask(int TaskID) {
		return mTasks.get(TaskID);
	}

	public void startAll() {
		for (DownloadTask task : mTasks.values()) {
			if(task.getDownloadStatus() == Values.READY);
				task.startTask();
		}
	}
	
	public Boolean isAllTasksFinished() {
		for (Integer task_id : mTasks.keySet()) {
			if (isTaskFinished(task_id) == false) {
				return false;
			}
		}
		return true;
	}

	public Boolean isTaskFinished(int task_id) {
		DownloadTask task = mTasks.get(task_id);
		return task.getDownloadStatus() == Values.FINISHED;
	}

	public void pauseAllTasks() {
		for (Integer taskID : mTasks.keySet()) {
			pauseTask(taskID);
		}
	}

	public void pauseTask(int TaskID) {
		if (mTasks.containsKey(TaskID)) {
			DownloadTask task = mTasks.get(TaskID);
			task.pause();
		}
	}

	public void cancelAllTasks() {
		for (Integer taskID : mTasks.keySet()) {
			cancelTask(taskID);
		}
	}

	public void cancelTask(int TaskID) {
		if (mTasks.containsKey(TaskID)) {
			DownloadTask Task = mTasks.remove(TaskID);
			Task.cancel();
		}
	}

	public void shutdownSafely() {
		for (Integer task_id : mTasks.keySet()) {
			mTasks.get(task_id).pause();
		}
//		mThreadPool.shutdown();
	}

	public int getTotalDownloadedSize() {
		int size = 0;
		for (DownloadTask Task : mTasks.values()) {
			size += Task.getDownloadedSize();
		}
		return size;
	}

	public String getReadableDownloadSize() {
		return DownloadUtils.getReadableSize(getTotalDownloadedSize());
	}

	public int getTotalSpeed() {
		int speed = 0;
		for (DownloadTask Task : mTasks.values()) {
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
