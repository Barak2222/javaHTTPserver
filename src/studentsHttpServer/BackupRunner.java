package studentsHttpServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BackupRunner implements Runnable{
	private FileOutputStream out;
	private Map<Integer, Student> m;
	private File backup;

	public BackupRunner(Map<Integer, Student> m, File backup) {
		this.m = m;
		this.backup = backup;
	}
	public void run(){
		saveData();
	}
	/**
	 * Save the current data in MAP to the backup file
	 */
	private synchronized void saveData(){
		Properties properties = new Properties();
		for (Map.Entry<Integer, Student> entry : m.entrySet()) {
			properties.put(Integer.toString(entry.getKey()), entry.getValue().toURLString());
		}
		try {
			out = new FileOutputStream(backup);
			properties.store(out, null);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
