package studentsHttpServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The logic that supports each backup thread
 * 
 * @author Barak
 *
 */
public class BackupRunner implements Runnable {
	private FileOutputStream out;
	private Map<Integer, Student> m;
	private File backupFile;

	/**
	 * Initialises the logic needed for the data to be saved the file system
	 * 
	 * @param m
	 * @param f
	 */
	public BackupRunner(Map<Integer, Student> m, File f) {
		this.m = m;
		this.backupFile = f;
	}

	/**
	 * Calls the synchronised backup function
	 */
	public void run() {
		saveData();
	}

	/**
	 * Save the current data in memory to the backup file
	 */
	private synchronized void saveData() {
		Properties properties = new Properties();
		for (Map.Entry<Integer, Student> entry : m.entrySet()) {
			properties.put(Integer.toString(entry.getKey()), entry.getValue().toURLString());
		}
		try {
			out = new FileOutputStream(backupFile);
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
