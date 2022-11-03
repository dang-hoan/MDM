package DownFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

public class DownloadRunnable implements Runnable {

	private static final int BUFFER_SIZE = 1024;

	private String mFileUrl;
	private String mSaveDirectory;
	private String mSaveFileName;
	
	private int mStartPosition;
	private int mCurrentPosition;
	private int mEndPosition;
	
	DownloadTask Task;
	public final int Task_ID;
	public final int Thread_ID;

//	private DownloadRunnable() {
//		// just use for annotation
//		// -1 is meanningless
//		Task_ID = -1;
//	}

	public DownloadRunnable(String mFileUrl,
			String mSaveDirectory, String mSaveFileName, int mStartPosition,
			int mEndPosition, int Task_ID, int Thread_ID, DownloadTask Task) {
		super();
		this.mFileUrl = mFileUrl;
		this.mSaveDirectory = mSaveDirectory;
		this.mSaveFileName = mSaveFileName;
		
		this.mStartPosition = mStartPosition;
		this.mEndPosition = mEndPosition;
		this.mCurrentPosition = this.mStartPosition;
		
		this.Task = Task;
		this.Task_ID = Task_ID;
		this.Thread_ID = Thread_ID;
	}

	public DownloadRunnable(String mFileUrl, String mSaveDirectory, String mSaveFileName, 
			int mStartPosition,	int mCurrentPosition, int mEndPosition, int Task_ID, int Thread_ID, DownloadTask Task) {
		this(mFileUrl, mSaveDirectory, mSaveFileName, mStartPosition, mEndPosition, Task_ID, Thread_ID, Task);
		this.mCurrentPosition = mCurrentPosition;
	}

	@Override
	public void run() {
		File targetFile;
		synchronized (this) {
			File dir = new File(mSaveDirectory);
			if (dir.exists() == false) {
				dir.mkdirs();
			}
			
			targetFile = new File(mSaveDirectory + File.separator + mSaveFileName);
			if (targetFile.exists() == false) {
				try {
					targetFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Download Task ID " + Task_ID + ": Thread " + Thread_ID
				+ " has been started! Range From " + mCurrentPosition + " To "
				+ mEndPosition);
		BufferedInputStream bufferedInputStream = null;
		RandomAccessFile randomAccessFile = null;
		byte[] buf = new byte[BUFFER_SIZE];
		URLConnection urlConnection = null;
		try {
			URL url = new URL(mFileUrl);
			urlConnection = url.openConnection();
			urlConnection.setRequestProperty("Range", "bytes=" + mCurrentPosition + "-" + mEndPosition);
			
			randomAccessFile = new RandomAccessFile(targetFile, "rw");
			randomAccessFile.seek(mCurrentPosition);
			
			bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
			
			while (mCurrentPosition < mEndPosition) {
				if (Thread.currentThread().isInterrupted()) {
					System.out.println("Download TaskID: "
							+ Task_ID + ": Thread " + Thread_ID
							+ " was interrupted, Start:" + mStartPosition
							+ " Current:" + mCurrentPosition + " End:"
							+ mEndPosition);
					break;
				}
				int len = bufferedInputStream.read(buf, 0, BUFFER_SIZE);
//				System.out.println("Thread " + Thread_ID);
				if (len == -1)
					break;
				else {
					randomAccessFile.write(buf, 0, len);
					mCurrentPosition += len;
				}
			}
			
			Task.notify(Thread_ID);
			bufferedInputStream.close();
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public DownloadRunnable split() {
//		int end = mEndPosition;
//		int remaining = mEndPosition - mCurrentPosition;
//		int remainingCenter = remaining / 2;
//		System.out.print("CurrentPosition:" + mCurrentPosition
//				+ " EndPosition:" + mEndPosition + "Rmaining:" + remaining
//				+ " ");
//		if (remainingCenter > 1048576) {
//			int centerPosition = remainingCenter + mCurrentPosition;
//			System.out.print(" Center position:" + centerPosition);
//			mEndPosition = centerPosition;
//
//			DownloadRunnable newSplitedRunnable = new DownloadRunnable(
//					mDownloadMonitor, mFileUrl, mSaveDirectory, mSaveFileName,
//					centerPosition + 1, end);
//			mDownloadMonitor.mHostTask.addPartedTask(newSplitedRunnable);
//			return newSplitedRunnable;
//		} else {
//			System.out.println(toString() + " can not be splited ,less than 1M");
//			return null;
//		}
//	}

	public boolean isFinished() {
		return mCurrentPosition >= mEndPosition;
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

}
