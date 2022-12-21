package BLL.VideoConversion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import BLL.Utils;
import BLL.DownFile.DownloadManager;

public class FFmpeg {
	public final static int FF_NOT_FOUND = 10, FF_LAUNCH_ERROR = 20, FF_CONVERSION_FAILED = 30, FF_SUCCESS = 0;
	private List<String> inputFiles;
	private String outputFile;
	private int ffExitCode;

	public FFmpeg(List<String> inputFiles, String outputFile) {
		this.inputFiles = inputFiles;
		this.outputFile = outputFile;
	}

	public int convert() {
		try {
			File ffFile = new File(DownloadManager.getInstance().getDataDir(),
					System.getProperty("os.name").toLowerCase().contains("windows") ? "ffmpeg.exe" : "ffmpeg");
			if (!ffFile.exists()) {
				ffFile = new File(Utils.getJarFile().getParentFile() + File.separator + "lib",
						System.getProperty("os.name").toLowerCase().contains("windows") ? "ffmpeg.exe" : "ffmpeg");
				if (!ffFile.exists()) {
					return FF_NOT_FOUND;
				}
			}	
//			ffmpeg -i videoFile.mp4 -i audioFile.au -y -acodec copy -vcodec copy mergedFile.mp4 
			Process proc = Runtime.getRuntime().exec(ffFile.getAbsolutePath() + " -i "  + inputFiles.get(0) + " -i "  + inputFiles.get(1) + " -y -acodec copy -vcodec copy "  + outputFile);
			ffExitCode = proc.waitFor();
			
			if(ffExitCode == 0) {
				System.out.println("merge audio and video success");
				new File(inputFiles.get(0)).delete();
				new File(inputFiles.get(1)).delete();	
				new File(inputFiles.get(0)).getParentFile().delete();
				new File(inputFiles.get(1)).getParentFile().delete();
			}
			else
				System.out.println("merge audio and video fail");
			
			return ffExitCode == 0 ? FF_SUCCESS : FF_CONVERSION_FAILED;
		} catch (RuntimeException | InterruptedException | IOException e) {
			return FF_LAUNCH_ERROR;
		}
	}
	
	//còn vài phần phục vụ cho việc lấy tiến trình ghép file, có thể thêm vào sau...

	
}
