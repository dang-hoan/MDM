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
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.JProgressBar;

import BLL.Values;
import BLL.VideoConversion.FFmpeg;
import View.VideoPopupitem;

public class DownloadManager {
	private String DataFile = "downloads.tmp";
	private String DataDir = System.getProperty("user.home") + File.separator + ".mdm";
	private static DownloadManager instance;
	private Hashtable<Integer, DownloadTask> Tasks = new Hashtable<>();
	private Hashtable<Integer, YTVideo> VideoTasks = new Hashtable<>();

	private DownloadManager() {
	}

	public static DownloadManager getInstance(){
		if (instance == null) {
			instance = new DownloadManager();
		}
		return instance;
	}

	public void setMaxThreadCount(int MaxCount) {
		if (MaxCount >= Values.MIN_THREAD_COUNT)
			Values.MAX_THREAD_COUNT = MaxCount;
	}
	
	public int getThreadCount(int TaskID) {
		DownloadTask t = getTask(TaskID);
		if(t != null) {
			return t.getTaskThreadCount();
		}
		else {
			YTVideo v = getVideo(TaskID);
			if(v != null) {
				return v.getT()[0].getTaskThreadCount() + v.getT()[1].getTaskThreadCount();
			}
		}
		return Values.DEFAULT_THREAD_COUNT;
	}
	
	public boolean setThreadCount(int TaskID, int ThreadCount) {
		DownloadTask t = getTask(TaskID);
		if(t != null) {
			t.setTaskThreadCount(ThreadCount);
			return true;
		}
		else {
			YTVideo v = getVideo(TaskID);
			if(v != null) {
				int size1 = ThreadCount/2;
				int size2 = ThreadCount - size1;
				v.getT()[0].setTaskThreadCount(size1);
				v.getT()[1].setTaskThreadCount(size2);
				return true;
			}
		}
		return false;
	}

	//length = -1
	//https://wallup.net/wp-content/uploads/2019/09/296096-sunset-mountains-ocean-landscapes-nature-travel-hdr-photography-blue-skies-skies-cloud.jpg

	public DownloadTask addTask(String url, String saveDirectory, String saveName,
			int ThreadCount, long size, Boolean now, JProgressBar[] jProgressBars, speed_Download speedDownload, boolean fileNeedMerge) {
		if(ThreadCount > Values.MAX_THREAD_COUNT || ThreadCount < Values.MIN_THREAD_COUNT) {
			ThreadCount = Values.DEFAULT_THREAD_COUNT;
		}
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER++, url, saveDirectory, saveName, ThreadCount, size,jProgressBars,speedDownload,fileNeedMerge);
		addTask(downloadTask);

		if(now) downloadTask.startTask();

		return downloadTask;
	}

	public YTVideo addTask(String url, String url2, String saveDirectory, String saveName,
			int ThreadCount, int ThreadCount2, long size1, long size2, Boolean now, JProgressBar[] jProgressBars, speed_Download speedDownload,
			JProgressBar[] jProgressBars2, speed_Download speedDownload2) {
		
		if(saveName.lastIndexOf(".") == -1) saveName += ".mkv";
		String FileName = saveName.substring(0, saveName.lastIndexOf("."));
		
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER, url, saveDirectory, FileName, ThreadCount, size1,jProgressBars,speedDownload, true);
		DownloadTask downloadTask2 = new DownloadTask(Values.Task_ID_COUNTER++, url2, saveDirectory, FileName, ThreadCount2, size2,jProgressBars2,speedDownload2, true);


		
		YTVideo v = new YTVideo(new DownloadTask[] {downloadTask, downloadTask2}, saveName);
		addVideo(v);

		if(now) {
			downloadTask.startTask();
			downloadTask2.startTask();
		}

		return v;
	}
	
	public YTVideo addVideo(VideoPopupitem videoItem, String saveDirectory, int size1, int size2, Boolean now, JProgressBar[] jProgressBars, speed_Download speedDownload,
			JProgressBar[] jProgressBars2, speed_Download speedDownload2) {
		
		String saveName = videoItem.getFile_Name();
		if(saveName.lastIndexOf(".") == -1) saveName += ".mkv";
		String FileName = saveName.substring(0, saveName.lastIndexOf("."));
		
		DownloadTask downloadTask = new DownloadTask(Values.Task_ID_COUNTER, videoItem.getUrl_Video(), saveDirectory, FileName, size1, videoItem.getLen1(),jProgressBars,speedDownload, true);
		DownloadTask downloadTask2 = new DownloadTask(Values.Task_ID_COUNTER++, videoItem.getUrl_Audio(), saveDirectory, FileName, size2, videoItem.getLen2(),jProgressBars2,speedDownload2, true);
		
		YTVideo v = new YTVideo(new DownloadTask[] {downloadTask, downloadTask2}, saveName);
		addVideo(v);
		videoItem.setFile_Name(saveName);

		if(now) {
			downloadTask.startTask();
			downloadTask2.startTask();
		}

		return v;
	}

	public void addTask(DownloadTask downloadTask) {
		Tasks.put(downloadTask.getTaskID(), downloadTask);
	}

	public void addVideo(YTVideo v) {
		VideoTasks.put(v.getT()[0].getTaskID(), v);
	}

	public boolean checkFile(int TaskID, String type, String checksum) {
		DownloadTask task = Tasks.get(TaskID);
		if(task != null) return task.checkFile(type, checksum);
		return VideoTasks.get(TaskID).checkFile(type, checksum);
	}

	public DownloadTask getTask(int TaskID) {
		return Tasks.get(TaskID);
	}

	public YTVideo getVideo(int TaskID) {
		return VideoTasks.get(TaskID);
	}

	public DownloadTask getTask(int TaskID, speed_Download sp) {
		DownloadTask t = Tasks.get(TaskID);
		if(t != null) return t;
		DownloadTask[] v = VideoTasks.get(TaskID).getT();
		if(v[0].getSpeed_Download() == sp) return v[0];
		else return v[1];

	}
	
	public boolean isTaskExist(int TaskID) {
		return Tasks.containsKey(TaskID);
	}
	
	public boolean isVideoExist(int VideoID) {
		return VideoTasks.containsKey(VideoID);
	}

	public void startTask(int TaskID) {
		for (DownloadTask task : Tasks.values()) {
			if(task.getTaskID() == TaskID) {
				task.startTask();
				break;
			}
		}
		for(YTVideo v : VideoTasks.values()) {
			if(v.getT()[0].getTaskID() == TaskID) {
				if(v.getDownloadStatus() == Values.FINISHED || v.getDownloadStatus() == Values.MERGING ||
						v.getDownloadStatus() == FFmpeg.FF_NOT_FOUND || v.getDownloadStatus() == FFmpeg.FF_LAUNCH_ERROR) v.merge();
				else {
					if(v.getDownloadStatus() == FFmpeg.FF_SUCCESS || v.getDownloadStatus() == FFmpeg.FF_CONVERSION_FAILED) {
						v.setDownloadStatus(Values.DOWNLOADING);
						v.getT()[0].startTask();
						v.getT()[1].startTask();
					}
					else {
						v.setDownloadStatus(Values.DOWNLOADING);
						int st1 = v.getT()[0].getDownloadStatus();
						int st2 = v.getT()[1].getDownloadStatus();
						if(st1 != Values.FINISHED) v.getT()[0].startTask();
						else v.getT()[0].setFinished();
						if(st2 != Values.FINISHED) v.getT()[1].startTask();
						else v.getT()[1].setFinished();
					}
					
				}
				
				break;
			}
		}
	}

	public void startAll() throws IOException {
		for (DownloadTask task : Tasks.values()) {
			if(task.getDownloadStatus() == Values.READY)
				task.startTask();
		}
		for(YTVideo v : VideoTasks.values()) {
			if(v.getT()[0].getDownloadStatus() == Values.READY) {
				v.getT()[0].startTask();
				v.getT()[1].startTask();
			}
		}
	}

	public Boolean isAllTasksFinished() {
		for (DownloadTask task : Tasks.values()) {
			if(task.getDownloadStatus() != Values.FINISHED) return false;
		}
		for (YTVideo v : VideoTasks.values()) {
			if(v.getT()[0].getDownloadStatus() != Values.FINISHED || v.getT()[1].getDownloadStatus() != Values.FINISHED) return false;
		}
		return true;
	}

	public Boolean isTaskFinished(int task_id) {
		DownloadTask task = Tasks.get(task_id);
		if(task != null) return task.getDownloadStatus() == Values.FINISHED;
		DownloadTask[] tasks = VideoTasks.get(task_id).getT();
		if(tasks != null) return (tasks[0].getDownloadStatus() == Values.FINISHED && tasks[1].getDownloadStatus() == Values.FINISHED);
		return false;
	}

	public void pauseAllTasks() throws IOException {
		for (DownloadTask task : Tasks.values()) {
			task.pause();
		}
		for (YTVideo v : VideoTasks.values()) {
			v.getT()[0].pause();
			v.getT()[1].pause();
		}
	}

	public void pauseTask(int TaskID) throws IOException {
		DownloadTask task = Tasks.get(TaskID);
		if(task != null) {
			task.pause();
			return;
		}
		DownloadTask[] tasks = VideoTasks.get(TaskID).getT();
		if(tasks != null) {
			tasks[0].pause();
			tasks[1].pause();
		}
	}

	public void shutdown() throws IOException {
		pauseAllTasks();
		for (DownloadTask task : Tasks.values()) {
			task.shutdown();
		}
		for (YTVideo v : VideoTasks.values()) {
			v.getT()[0].shutdown();
			v.getT()[1].shutdown();
		}
		storeTasks();
	}
	public void shutdownRudely() throws IOException {
		pauseAllTasks();
	}
	
	public void setStatus(int TaskID, int status) {
		DownloadTask t = getTask(TaskID);
		if(t != null) t.set_Status(status);
		else {
			YTVideo v = getVideo(TaskID);
			if(v != null) {
				v.set_Status(status);
			}
		}
	}

	public void resumeTasks() throws IOException {   				//Khôi phục thông tin các task (file)
		File file = new File(DataDir, DataFile);
		if (!file.exists()) return;

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
				String FileType = reader.readLine();
				String SaveDirectory = reader.readLine();
				String ProgressFile = reader.readLine();
				String ProgressFolder = reader.readLine();
				long FileSize = Integer.parseInt(reader.readLine());
				int ThreadCount = Integer.parseInt(reader.readLine());
				int DownloadStatus = Integer.parseInt(reader.readLine());
				Long createDate = Long.parseLong(reader.readLine());
				Long downloadTime = Long.parseLong(reader.readLine());
				boolean fileNeedMerge = Boolean.parseBoolean(reader.readLine());

				DownloadTask task = new DownloadTask(
						Values.Task_ID_COUNTER++,
						url, SaveDirectory, SaveName, FileType, ProgressFile, ProgressFolder, FileSize, ThreadCount, DownloadStatus, createDate, downloadTime, fileNeedMerge);
				addTask(task);
			}
			
			//Khôi phục video
			line = reader.readLine();
			if (line != null) {
				count = Integer.parseInt(line.trim());
				for (int i = 0; i < count; i++) {
					String url = reader.readLine();
					String SaveName = reader.readLine();
					String FileType = reader.readLine();
					String SaveDirectory = reader.readLine();
					String ProgressFile = reader.readLine();
					String ProgressFolder = reader.readLine();
					long FileSize = Integer.parseInt(reader.readLine());
					int ThreadCount = Integer.parseInt(reader.readLine());
					int DownloadStatus = Integer.parseInt(reader.readLine());
					Long createDate = Long.parseLong(reader.readLine());
					Long downloadTime = Long.parseLong(reader.readLine());

					DownloadTask task = new DownloadTask(
							Values.Task_ID_COUNTER,
							url, SaveDirectory, SaveName, FileType, ProgressFile, ProgressFolder, FileSize, ThreadCount, DownloadStatus, createDate, downloadTime, true);

					url = reader.readLine();
					SaveName = reader.readLine();
					FileType = reader.readLine();
					ProgressFile = reader.readLine();
					ProgressFolder = reader.readLine();
					FileSize = Integer.parseInt(reader.readLine());
					ThreadCount = Integer.parseInt(reader.readLine());
					DownloadStatus = Integer.parseInt(reader.readLine());
					createDate = Long.parseLong(reader.readLine());
					downloadTime = Long.parseLong(reader.readLine());

					DownloadTask task2 = new DownloadTask(
							Values.Task_ID_COUNTER++,
							url, SaveDirectory, SaveName, FileType, ProgressFile, ProgressFolder, FileSize, ThreadCount, DownloadStatus, createDate, downloadTime, true);

					String name = reader.readLine();
					downloadTime = Long.parseLong(reader.readLine());
					int downloadStatus = Integer.parseInt(reader.readLine());

					YTVideo v = new YTVideo(new DownloadTask[] {task, task2}, name, downloadTime, downloadStatus);
					addVideo(v);
				}
			}
			reader.close();
		}catch (Exception e) {

		}
	}

	public void storeTasks() throws IOException {
		File dir = new File(DataDir);
		if (!dir.exists()) {
			dir.mkdir();
		}

		File file = new File(DataDir, DataFile);
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter writer = null;

		String newLine = System.getProperty("line.separator");
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8")));

			//ghi file thường
			writer.write(Integer.toString(Tasks.size()));
			writer.newLine();
			String s = "";
			for(DownloadTask t : Tasks.values())
			{
				s += t.getUrl() + newLine +
					 t.getSaveName() + newLine +
					 t.getType() + newLine +
					 t.getSaveDirectory() + newLine +
					 t.getProgressFile() + newLine +
					 t.getProgressFolder() + newLine +
					 t.getFileSize() + newLine +
					 t.getThreadCount() + newLine +
					 t.getDownloadStatus() + newLine +
					 t.getCreateDate() + newLine +
					 t.getDownloadTime() + newLine +
					 t.isFileNeedMerge() + newLine;
			}
			writer.write(s);

			//ghi video
			writer.write(Integer.toString(VideoTasks.size()));
			writer.newLine();
			s = "";
			for(YTVideo v : VideoTasks.values())
			{
				DownloadTask[] t = v.getT();
				s += t[0].getUrl() + newLine +
					 t[0].getSaveName() + newLine +
					 t[0].getType() + newLine +
					 t[0].getSaveDirectory() + newLine +
					 t[0].getProgressFile() + newLine +
					 t[0].getProgressFolder() + newLine +
					 t[0].getFileSize() + newLine +
					 t[0].getThreadCount() + newLine +
					 t[0].getDownloadStatus() + newLine +
					 t[0].getCreateDate() + newLine +
					 t[0].getDownloadTime() + newLine +
					 t[1].getUrl() + newLine +
					 t[1].getSaveName() + newLine +
					 t[1].getType() + newLine +
					 t[1].getProgressFile() + newLine +
					 t[1].getProgressFolder() + newLine +
					 t[1].getFileSize() + newLine +
					 t[1].getThreadCount() + newLine +
					 t[1].getDownloadStatus() + newLine +
					 t[1].getCreateDate() + newLine +
					 t[1].getDownloadTime() + newLine +
					 v.getFileName() + newLine +
					 v.getDownloadTime() + newLine +
					 v.getDownloadStatus() + newLine;
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
	}

	public void cancelAllTasks() throws IOException {
		for(DownloadTask Task : Tasks.values()) {
			Task.cancel();
		}
		for(YTVideo v : VideoTasks.values()) {
			v.getT()[0].cancel();
			v.getT()[1].cancel();
		}
	}

	public void cancelTask(int TaskID) throws IOException {
		if (Tasks.containsKey(TaskID)) {
			DownloadTask Task = Tasks.get(TaskID);
			Task.cancel();
		}
		if (VideoTasks.containsKey(TaskID)) {
			YTVideo v = VideoTasks.get(TaskID);
			v.getT()[0].cancel();
			v.getT()[1].cancel();
		}
	}

	public void deleteTask(int TaskID) throws IOException {
		if (Tasks.containsKey(TaskID)) {
			Tasks.remove(TaskID);
		}
		if (VideoTasks.containsKey(TaskID)) {
			VideoTasks.remove(TaskID);
		}
	}

	public String getDataDir() {
		return DataDir;
	}

	public void delete(int id){
		try{
			DownloadTask t = Tasks.get(id);
			if(t != null){
				t.deleteAllFile();
				t.set_Status(Values.DELETED);
			}
			else{
				YTVideo v = VideoTasks.get(id);
				if(v != null){
					v.getT()[0].deleteAllFile();
					v.getT()[1].deleteAllFile();
					v.set_Status(Values.DELETED);
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}

	public int getTotalDownloadedSize() {
		int size = 0;
		for (DownloadTask Task : Tasks.values()) {
			size += Task.getDownloadedSize();
		}
		for (YTVideo v : VideoTasks.values()) {
			size += v.getT()[0].getDownloadedSize();
			size += v.getT()[1].getDownloadedSize();
		}
		return size;
	}

	public String getReadableDownloadSize() {
		return DownloadUtils.getReadableSize(getTotalDownloadedSize());
	}

	public int mergeFile(String path1, String path2, String fileName) {
		return new FFmpeg(Arrays.asList(path1, path2), fileName).convert();
	}
	
	public void doNext(String request, int TaskID) {
		if(request.equals("setStatus")) {
			YTVideo v = getVideo(TaskID);
			if(v != null) {
				int state1 = v.getT()[0].getDownloadStatus();
				int state2 = v.getT()[1].getDownloadStatus();
//				System.out.println("state1: " + Values.State(state1) + ", state2: " + Values.State(state2));
				switch(state1) {
					case Values.READY, Values.DOWNLOADING, Values.PAUSED, Values.CANCELED, Values.DELETED:{
						v.setDownloadStatus(state1);
						break;
					}
					case Values.ASSEMBLING:{
						if(state2 == Values.FINISHED) v.setDownloadStatus(state1);
						break;
					}
					case Values.FINISHED:{
						v.setDownloadStatus(state2);
						break;
					}
				}
			}
		}
		else if(request.equals("merge")) {
			YTVideo v = getVideo(TaskID);
			if(v != null && v.getDownloadStatus() == Values.FINISHED) {
				v.merge();
			}
		}
	}
}
