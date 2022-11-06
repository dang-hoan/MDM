package BLL.DownFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
//import java.util.Timer;
import java.util.TimerTask;

import BLL.Values;

public class DownloadTask {
	private int mTaskID;
	private int mTaskStatus = Values.READY;
	
	private String mUrl;
	private String mSaveDirectory;
	private String mSaveName;
	
	private int mFileSize;	
	private int mThreadCount = Values.DEFAULT_THREAD_COUNT;
	private int completedThread = 0;
	
	private ArrayList<DownloadRunnable> listRunnable = new ArrayList<DownloadRunnable>();
	private ArrayList<RunnableRecoveryInfo> mRunnableRecoveryInfos = new ArrayList<RunnableRecoveryInfo>(); //Thông tin khôi phục cho các luồng

	private SpeedMonitor mSpeedMonitor = new SpeedMonitor(this);

//	private Timer mSpeedTimer = new Timer();
//	private Timer mStoreTimer = new Timer();

	static class RunnableRecoveryInfo {

		private int mStartPosition;
		private int mCurrentPosition;
		private int mEndPosition;
		private boolean isFinished = false;

		public RunnableRecoveryInfo(int start, int current, int end) {
			if (end > start && current > start) {
				mStartPosition = start;
				mCurrentPosition = current;
				mEndPosition = end;
			} else {
				throw new RuntimeException("position logical error");
			}
			if (mCurrentPosition >= mEndPosition) {
				isFinished = true;
			}
		}

		public int getStartPosition() {
			return mStartPosition;
		}

		public int getCurrentPosition() {
			return mCurrentPosition;
		}

		public int getEndPosition() {
			return mEndPosition;
		}

		public boolean isFinished() {
			return isFinished;
		}
	}


	private static class SpeedMonitor extends TimerTask {
		private long  mLastSecondSize = 0;	
		private long  mCurrentSecondSize = 0;		
		private long mSpeed;		
		private long mMaxSpeed;	
		private long mAverageSpeed;
		private long mCounter;

		private DownloadTask mHostTask;

		public long getMaxSpeed() {
			return mMaxSpeed;
		}

		public SpeedMonitor(DownloadTask TaskBelongTo) {
			mHostTask = TaskBelongTo;
		}

		@Override
		public void run() {
			mCounter++;
			mCurrentSecondSize = mHostTask.getDownloadedSize();
			mSpeed = mCurrentSecondSize - mLastSecondSize;
			mLastSecondSize = mCurrentSecondSize;
			if (mSpeed > mMaxSpeed) {
				mMaxSpeed = mSpeed;
			}

			mAverageSpeed = mCurrentSecondSize / mCounter;
		}

		public long getDownloadedTime() {
			return mCounter;
		}

		public long getSpeed() {
			return mSpeed;
		}

		public long getAverageSpeed() {
			return mAverageSpeed;
		}
	}

//	private class StoreMonitor extends TimerTask {
//		@Override
//		public void run() {
//			storeProgress();
//		}
//	}

	public DownloadTask(int TaskID, String url, String saveDirectory, String saveName, int ThreadCount) throws IOException {
		this.mTaskID = TaskID;
		this.mUrl = url;
		setTargetFile(saveDirectory, saveName);
		if(ThreadCount > 0) this.mThreadCount = ThreadCount;
		System.out.println("TaskID: " + mTaskID);
	}

	public Boolean setTargetFile(String saveDir, String saveName) throws IOException {
		if (saveDir.lastIndexOf(File.separator) == saveDir.length() - 1) {
			saveDir = saveDir.substring(0, saveDir.length() - 1);
		}
		mSaveDirectory = saveDir;
		
		File dirFile = new File(saveDir);
		if (dirFile.exists() == false) {
			if (dirFile.mkdirs() == false) {
				throw new RuntimeException("Error to create directory");
			}
		}

		mSaveName = saveName + "." + getFileType(mUrl).split("/")[1];

		File file = new File(dirFile.getPath() + File.separator + mSaveName);
		if (file.exists() == false) {
			file.createNewFile();
		}
		return true;
	}

	public int getTaskID() {
		return mTaskID;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String Url) {
		this.mUrl = Url;
	}

	public String getSaveDirectory() {
		return mSaveDirectory;
	}

	public void setSaveDirectory(String SaveDirectory) {
		this.mSaveDirectory = SaveDirectory;
	}

	public String getSaveName() {
		return mSaveName;
	}

	public void setSaveName(String SaveName) {
		this.mSaveName = SaveName;
	}

	public void setTaskThreadCount(int thread_count) {
		mThreadCount = thread_count;
	}

	public int getTaskThreadCount() {
		return mThreadCount;
	}

	private ArrayList<DownloadRunnable> splitDownload(int thread_count) { 				//Split thread
		ArrayList<DownloadRunnable> runnables = new ArrayList<DownloadRunnable>();
		try {
			int size = getFileLength(mUrl);
			mFileSize = size;
			int sublen = size / thread_count;
			for (int i = 0; i < thread_count; i++) {
				int startPos = sublen * i;
				int endPos = (i == thread_count - 1) ? size	: (sublen * (i + 1) - 1);
				
				DownloadRunnable runnable = new DownloadRunnable(mUrl, mSaveDirectory, mSaveName, startPos, endPos, mTaskID, i+1, this);
				runnables.add(runnable);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return runnables;
	}

//	private void resumeProgress() throws IOException {   //Khôi phục công việc trước còn dang dở
//		try {
//			File progressFile = new File(FileUtils.getSafeDirPath(mProgressDir)
//					+ File.separator + mProgressFileName);
//			if (progressFile.exists() == false) {
//				throw new IOException("Progress File does not exsist");
//			}
//
//			JAXBContext context = JAXBContext.newInstance(DownloadTask.class);
//			Unmarshaller unmarshaller = context.createUnmarshaller();
//			
//			DownloadTask task = (DownloadTask) unmarshaller.unmarshal(progressFile);
//			
//			File targetSaveFile = new File(
//					FileUtils.getSafeDirPath(task.mSaveDirectory
//							+ File.separator + task.mSaveName));
//			
//			if (targetSaveFile.exists() == false) {
//				throw new IOException(
//						"Try to continue download file , but target file does not exist");
//			}
//			ArrayList<RunnableRecoveryInfo> mRunnableRecoveryInfos = getDownloadProgress();
//			recoveryRunnableInfos.clear();
//			for (DownloadRunnable runnable : task.listRunnable) {
//				recoveryRunnableInfos.add(new RecoveryRunnableInfo(runnable
//						.getStartPosition(), runnable.getCurrentPosition(),
//						runnable.getEndPosition()));
//			}
//			mSpeedMonitor = new SpeedMonitor(this);
//			mStoreMonitor = new StoreMonitor();
//			System.out.println("Resume finished");
//			listRunnable.clear();
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}
//	}

	public void startTask() {
		setDownloadStatus(Values.DOWNLOADING);
		//resumeProgress();	//Khôi phục công việc của các luồng
		
		if (mRunnableRecoveryInfos.size() != 0) {
//			for (RecoveryInfo runnableInfo : mRecoveryInfos) {
//				if (runnableInfo.isFinished == false) {
//					DownloadRunnable runnable = new DownloadRunnable(mMonitor,
//							mUrl, mSaveDirectory, mSaveName,
//							runnableInfo.getStartPosition(),
//							runnableInfo.getCurrentPosition(),
//							runnableInfo.getEndPosition());
//					System.out.println("Position: " + 1);
//					Threads.add(thread);
//					System.out.println("Position: " + 2);
//					threadPool.submit(runnable);
//				}
//			}
			System.out.println("recovery");
		} else {
			for (DownloadRunnable runnable : splitDownload(mThreadCount)) {
				Thread t = new Thread(runnable);
				listRunnable.add(runnable);
				t.start();
			}
		}
//		mSpeedTimer.scheduleAtFixedRate(mSpeedMonitor, 0, 1000);
	}
	
	public void notify(int Thread_ID) {
        System.out.println("*******Task ID " + mTaskID + ": Thread " + Thread_ID + " download complete *********");
        completedThread++;
        if(completedThread == mThreadCount) {
        	mTaskStatus = Values.FINISHED;
        	System.out.println("\n--------Complete file " + mSaveName + " download--------\n");
            DownloadManager.getInstance().cancelTask(mTaskID);
        }
	}

	public void addPartedTask(DownloadRunnable runnable) {
		listRunnable.add(runnable);
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

//	public File getHistoryFile() {
//		return new File(mHistoryDirectory + File.separator + mHistoryName);
//	}

	public File getDownloadFile() {
		return new File(mSaveDirectory + File.separator + mSaveName);
	}

//	public File getHistoryDir() {
//	return new File(mHistoryDirectory + File.separator + mHistoryName);
//}

	public long getDownloadedSize() { //Kích thước đã tải
		long size = 0;
		for(DownloadRunnable r : listRunnable) {
			size += r.getCurrentPosition() - r.getStartPosition();
		}
		return size;
	}

	public long getSpeed() {
		return mSpeedMonitor.getSpeed();
	}

	public String getReadableSpeed() {
		return DownloadUtils.getReadableSpeed(getSpeed());
	}

	public long getMaxSpeed() {
		return mSpeedMonitor.getMaxSpeed();
	}

	public String getReadableMaxSpeed() {
		return DownloadUtils.getReadableSpeed(getMaxSpeed());
	}

	public long getAverageSpeed() {
		return mSpeedMonitor.getAverageSpeed();
	}

	public String getReadableAverageSpeed() {
		return DownloadUtils.getReadableSpeed(mSpeedMonitor.getAverageSpeed());
	}

	public long getTimePassed() {
		return mSpeedMonitor.getDownloadedTime();
	}

//	public int getActiveTheadCount() {
//		return mThreadPoolRef.getActiveCount();
//	}

	public int getFileSize() {
		return mFileSize;
	}

	public void pause() {
		setDownloadStatus(Values.PAUSED);
		storeProgress();
//		mThreadPoolRef.pause(mTaskID);
	}

	private void setDownloadStatus(int status) {
		if (status == Values.FINISHED) {
//			mSpeedTimer.cancel();
		}
		mTaskStatus = status;
	}
	
	public int getDownloadStatus() {
		return mTaskStatus;
	}

	public void storeProgress() { //Lưu tiến trình làm việc
		mRunnableRecoveryInfos.clear();
		for(int i = 0; i < listRunnable.size(); i++) {
			
		}
	}

//	public static DownloadTask recoverTaskFromProgressFile(
//			String progressDirectory, String progressFileName)
//			throws IOException {
//		try {
//			File progressFile = new File(
//					FileUtils.getSafeDirPath(progressDirectory)
//							+ File.separator + progressFileName);
//			if (progressFile.exists() == false) {
//				throw new IOException("Progress File does not exsist");
//			}
//
//			JAXBContext context = JAXBContext
//					.newInstance(DownloadTask.class);
//			Unmarshaller unmarshaller = context.createUnmarshaller();
//			DownloadTask Task = (DownloadTask) unmarshaller
//					.unmarshal(progressFile);
//			File targetSaveFile = new File(
//					FileUtils.getSafeDirPath(Task.mSaveDirectory
//							+ File.separator + Task.mSaveName));
//			if (targetSaveFile.exists() == false) {
//				throw new IOException(
//						"Try to continue download file , but target file does not exist");
//			}
//			Task.setProgessFile(progressDirectory, progressFileName);
//			Task.mTaskID = Task_ID_COUNTER++;
//			ArrayList<RecoveryRunnableInfo> recoveryRunnableInfos = Task
//					.getDownloadProgress();
//			for (DownloadRunnable runnable : Task.listRunnable) {
//				recoveryRunnableInfos.add(new RecoveryRunnableInfo(runnable
//						.getStartPosition(), runnable.getCurrentPosition(),
//						runnable.getEndPosition()));
//			}
//			Task.listRunnable.clear();
//			return Task;
//		} catch (JAXBException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

	private void deleteHistoryFile() {
//		getHistoryFile().delete();
	}

	public ArrayList<RunnableRecoveryInfo> getDownloadProgress() {
		return mRunnableRecoveryInfos;
	}

	public void cancel() {
		deleteHistoryFile();
//		mSpeedTimer.cancel();
		listRunnable.clear();
		mRunnableRecoveryInfos.clear();
//		mThreadPoolRef.cancel(mTaskID);
	}
}
