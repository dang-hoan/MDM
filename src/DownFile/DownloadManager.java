package DownFile;

import java.io.IOException;
import java.util.Hashtable;

public class DownloadManager {

	private static DownloadManager instance;

	public static final int DEFAULT_TASK_THREAD_COUNT = 8;

	public static final int DEFAULT_KEEP_ALIVE_TIME = 0;

	private static int ID = 0;
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

	public DownloadTask addTask(String url, String saveDirectory, String saveName) throws IOException {
		DownloadTask downloadTask = new DownloadTask(url, saveDirectory, saveName);
		addTask(downloadTask);
		return downloadTask;
	}

	public void addTask(DownloadTask downloadTask) {
		mTasks.put(ID++, downloadTask);
	}

	public DownloadTask getTask(int TaskID) {
		return mTasks.get(TaskID);
	}

	public void start() {
		for (DownloadTask task : mTasks.values()) {
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
		return task.isFinished();
	}

	public void pauseAllTasks() {
		for (Integer taskID : mTasks.keySet()) {
			pauseTask(taskID);
		}
	}

	public void pauseTask(int TaskID) {
		if (mTasks.contains(TaskID)) {
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
		if (mTasks.contains(TaskID)) {
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
