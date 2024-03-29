package BLL.DownFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import javax.swing.JProgressBar;

public class DownloadRunnable implements Runnable {
	private Thread t;
	private static final int BUFFER_SIZE = 8 * 8192;

	private String FileUrl;
	private String SaveDirectory;
	private String SaveFileName;

	private long StartPosition;
	private long CurrentPosition;
	private long EndPosition;

	public final int TaskID;
	public final int ThreadID;

	private JProgressBar jProgressBar;
	private speed_Download speed_Download;

//	private DownloadRunnable() {
//		// just use for annotation
//		// -1 is meanningless
//		Task_ID = -1;
//	}

	public DownloadRunnable(String FileUrl,
			String SaveDirectory, String SaveFileName, long StartPosition,
			long EndPosition, int TaskID, int ThreadID, JProgressBar jProgressBar,speed_Download speed_Download) {
		super();
		this.FileUrl = FileUrl;
		this.SaveDirectory = SaveDirectory;
		this.SaveFileName = SaveFileName;

		this.StartPosition = StartPosition;
		this.EndPosition = EndPosition;
		this.CurrentPosition = this.StartPosition;

		this.TaskID = TaskID;
		this.ThreadID = ThreadID;

		this.speed_Download=speed_Download;
		this.jProgressBar=jProgressBar;
		this.jProgressBar.setMinimum((int)(100*this.StartPosition/this.EndPosition));

		this.jProgressBar.setMaximum(100);

		this.jProgressBar.setValue((int) (100*CurrentPosition/EndPosition));

	}

	public DownloadRunnable(
			String FileUrl, String SaveDirectory, String SaveFileName,
			long StartPosition,	long CurrentPosition, long EndPosition,
			int TaskID, int ThreadID,JProgressBar jProgressBar,speed_Download speed_Download) {

		this(FileUrl, SaveDirectory, SaveFileName, StartPosition, EndPosition, TaskID, ThreadID,jProgressBar,speed_Download);
		this.CurrentPosition = CurrentPosition;

		this.jProgressBar.setValue((int)(100*CurrentPosition/EndPosition));
	}

	public void start() {
		t = new Thread(this);
		t.start();
	}


	public void pause() {
		if(t != null) t.interrupt();
	}

	public void join() throws InterruptedException {
		if(t != null) t.join();
	}

	@Override
	public void run() {
		File targetFile;
		synchronized (this) {
			File dir = new File(SaveDirectory);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			targetFile = new File(SaveDirectory, SaveFileName);
			if (!targetFile.exists()) {
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

		byte[] buf = new byte[BUFFER_SIZE];
		URLConnection urlConnection;
		try {
			URI uri = new URI(FileUrl);
			String userInfo = uri.getRawUserInfo();
			if(userInfo != null && userInfo.length() > 0) {
				String userName = userInfo.split(":")[0];
				String passWord = userInfo.split(":")[1];
			    userInfo = Base64.getEncoder().encodeToString(userInfo.getBytes());
			    Authenticator.setDefault(new Authenticator() {
			        @Override
			        protected PasswordAuthentication getPasswordAuthentication() {          
			            return new PasswordAuthentication(userName, passWord.toCharArray());
			        }
			    });
			}

			URL url = uri.toURL();
			urlConnection = url.openConnection();
			urlConnection.setReadTimeout(6000);

			if(EndPosition != -1) urlConnection.setRequestProperty("Range", "bytes=" + CurrentPosition + "-" + EndPosition);
			
			if(userInfo != null && userInfo.length() > 0) {
//				urlConnection.setRequestProperty("Authorization", "Basic " + userInfo);
			}
				

			BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile, true));

			if(EndPosition == -1 && CurrentPosition > 0) is.skip(CurrentPosition);
			
			while (CurrentPosition <= EndPosition || EndPosition == -1) {
				if (t.isInterrupted()) {
					System.out.println("Download Task ID "
							+ TaskID + ": Thread " + ThreadID
							+ " was interrupted, Start:" + StartPosition
							+ " Current:" + CurrentPosition + " End:"
							+ EndPosition);
					is.close();
					os.close();
					return;
				}
				int len = is.read(buf, 0, BUFFER_SIZE);
//				System.out.println("Thread " + Thread_ID);
				if (len == -1)
					break;
				else {
					os.write(buf, 0, len);
					CurrentPosition += len;

					this.jProgressBar.setValue((int)(100*CurrentPosition/EndPosition));
					this.speed_Download.plus_Size_DownLoad_1s(len);
				}
			}

			is.close();
			os.close();
			this.jProgressBar.setValue(jProgressBar.getMaximum());
			DownloadManager.getInstance().getTask(TaskID, speed_Download).notify(ThreadID);
		}
		catch(SocketTimeoutException e) {
			run();
			System.out.println("reset connection");
		}
		catch(SocketException e) {
			run(); 
			System.out.println("server reset");
		}
		catch (IOException | URISyntaxException e) {
			try {
				Thread.sleep(2000);
				
			} catch (InterruptedException e1) {
				return;
			}
			run();
			System.out.println("reset because server overload");
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
		return CurrentPosition > EndPosition;
	}

	public int getThreadID() {
		return ThreadID;
	}

	public long getStartPosition() {
		return StartPosition;
	}

	public long getCurrentPosition() {
		return CurrentPosition;
	}

	public long getEndPosition() {
		return EndPosition;
	}

	public String getSaveDir() {
		return SaveDirectory;
	}

	public String getSaveFile() {
		return SaveFileName;
	}

	public JProgressBar getjProgressBar() {
		return jProgressBar;
	}

	public void setjProgressBar(JProgressBar jProgressBar) {
		this.jProgressBar = jProgressBar;
	}

	public speed_Download getSpeed_Download() {
		return speed_Download;
	}

	public void setSpeed_Download(speed_Download speed_Download) {
		this.speed_Download = speed_Download;
	}
	

}
