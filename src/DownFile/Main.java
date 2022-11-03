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
			
			//Láº·p Ä‘á»ƒ in ra tá»‘c Ä‘á»™ truyá»�n vÃ  cÃ¡c thÃ´ng tin khÃ¡c khi táº£i 1 file (thÃ´ng tin task), á»Ÿ Ä‘Ã¢y láº·p 2 láº§n Ä‘á»ƒ minh hoáº¡
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
