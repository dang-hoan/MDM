package BLL.DownFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

public class DownloadRunnable implements Runnable {
	private Thread t;
	private static final int BUFFER_SIZE = 1024;

	private String FileUrl;
	private String SaveDirectory;
	private String SaveFileName;
	
	private int StartPosition;
	private int CurrentPosition;
	private int EndPosition;
	
	DownloadTask Task;
	public final int TaskID;
	public final int ThreadID;

//	private DownloadRunnable() {
//		// just use for annotation
//		// -1 is meanningless
//		Task_ID = -1;
//	}

	public DownloadRunnable(String FileUrl,
			String SaveDirectory, String SaveFileName, int StartPosition,
			int EndPosition, int TaskID, int ThreadID, DownloadTask Task) {
		super();
		this.FileUrl = FileUrl;
		this.SaveDirectory = SaveDirectory;
		this.SaveFileName = SaveFileName;
		
		this.StartPosition = StartPosition;
		this.EndPosition = EndPosition;
		this.CurrentPosition = this.StartPosition;
		
		this.Task = Task;
		this.TaskID = TaskID;
		this.ThreadID = ThreadID;
	}

	public DownloadRunnable(
			String FileUrl, String SaveDirectory, String SaveFileName, 
			int StartPosition,	int CurrentPosition, int EndPosition,
			int TaskID, int ThreadID, DownloadTask Task) {
		
		this(FileUrl, SaveDirectory, SaveFileName, StartPosition, EndPosition, TaskID, ThreadID, Task);
		this.CurrentPosition = CurrentPosition;
	}
	
	public void start() {
		t = new Thread(this);
		t.start();
	}
	
	public void pause() {
		if(t != null) t.interrupt();
	}

	@Override
	public void run() {
		File targetFile;
		synchronized (this) {
			File dir = new File(SaveDirectory);
			if (dir.exists() == false) {
				dir.mkdirs();
			}
			
			targetFile = new File(SaveDirectory + File.separator + SaveFileName);
			if (targetFile.exists() == false) {
				try {
					targetFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Download Task ID " + TaskID + ": Thread " + ThreadID
				+ " has been started! Range From " + CurrentPosition + " To "
				+ EndPosition);
		BufferedInputStream bufferedInputStream = null;
		RandomAccessFile randomAccessFile = null;
		byte[] buf = new byte[BUFFER_SIZE];
		URLConnection urlConnection = null;
		try {
			URL url = new URL(FileUrl);
			urlConnection = url.openConnection();
			urlConnection.setRequestProperty("Range", "bytes=" + CurrentPosition + "-" + EndPosition);
			
			randomAccessFile = new RandomAccessFile(targetFile, "rw");
			randomAccessFile.seek(CurrentPosition);
			
			bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
			
			while (CurrentPosition < EndPosition) {
				if (Thread.currentThread().isInterrupted()) {
					System.out.println("Download TaskID: "
							+ TaskID + ": Thread " + ThreadID
							+ " was interrupted, Start:" + StartPosition
							+ " Current:" + CurrentPosition + " End:"
							+ EndPosition);
					bufferedInputStream.close();
					randomAccessFile.close();
					break;
				}
				int len = bufferedInputStream.read(buf, 0, BUFFER_SIZE);
//				System.out.println("Thread " + Thread_ID);
				if (len == -1)
					break;
				else {
					randomAccessFile.write(buf, 0, len);
					CurrentPosition += len;
				}
			}
			
			Task.notify(ThreadID);
			bufferedInputStream.close();
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public DownloadRunnable split() {
//		int end = EndPosition;
//		int remaining = EndPosition - CurrentPosition;
//		int remainingCenter = remaining / 2;
//		System.out.print("CurrentPosition:" + CurrentPosition
//				+ " EndPosition:" + EndPosition + "Rmaining:" + remaining
//				+ " ");
//		if (remainingCenter > 1048576) {
//			int centerPosition = remainingCenter + CurrentPosition;
//			System.out.print(" Center position:" + centerPosition);
//			EndPosition = centerPosition;
//
//			DownloadRunnable newSplitedRunnable = new DownloadRunnable(
//					DownloadMonitor, FileUrl, SaveDirectory, SaveFileName,
//					centerPosition + 1, end);
//			mDownloadMonitor.mHostTask.addPartedTask(newSplitedRunnable);
//			return newSplitedRunnable;
//		} else {
//			System.out.println(toString() + " can not be splited ,less than 1M");
//			return null;
//		}
//	}

	public boolean isFinished() {
		return CurrentPosition >= EndPosition;
	}

	public int getThreadID() {
		return ThreadID;
	}
	
	public int getStartPosition() {
		return StartPosition;
	}

	public int getCurrentPosition() {
		return CurrentPosition;
	}

	public int getEndPosition() {
		return EndPosition;
	}

}
