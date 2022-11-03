package DownFile;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		DownloadManager downloadManager = DownloadManager.getInstance();
		String url = "https://1.bp.blogspot.com/-a71p9zvla98/UkP4-cPfK4I/AAAAAAAAAg8/va9AmdChErg/s1600/anh-dep-hinh-nen-thien-nhien-0.jpg";
		String saveDirectory = "D:\\Học tập\\Năm 3 kỳ 1\\PBL4";
		
		try {
			DownloadTask task = new DownloadTask(url, saveDirectory, "file110");
			downloadManager.addTask(task);
			DownloadTask task2 = new DownloadTask(url, saveDirectory, "file120");
			downloadManager.addTask(task2);
			downloadManager.start();
			int counter = 0;
			
			//Lặp để in ra tốc độ tải và các thông tin khác khi tải 1 file (thông tin Task), ở đây lặp 2 lần để mình hoạ
			while (counter < 2) {
				// System.out.println("The task has finished :"
				// + task.getReadableSize() + "Active Count:"
				// + task.getActiveTheadCount() + " speed:"
				// + task.getReadableSpeed() + " status:"
				// + task.isFinished() + " AverageSpeed:"
				// + task.getReadableAverageSpeed() + " MaxSpeed:"
				// + task.getReadableMaxSpeed() + " Time:"
				// + task.getTimePassed() + "s");
				System.out.println("Downloader information Speed:"
						+ downloadManager.getReadableTotalSpeed()
						+ " Down Size:"
						+ downloadManager.getReadableDownloadSize());
				Thread.sleep(1000);
				counter++;
				// if (counter == 6) {
				// mission.pause();
				// }
				// if (counter == 11) {
				// downloadManager.start();
				// }
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
