package studentsHttpServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

/**
 * This class contains all the logic for the responding to add/remove/find
 * request, The requests are handled using HashMap on memory. In addition, this
 * class contains the logic to support scheduled backups performed by other
 * classes.
 * 
 * @author Barak
 *
 */
public class DataHandler {
	static final int MAX_ENTRIES = 1000;
	static final String CURRENT_PATH = DataHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath();

	private Map<Integer, Student> m;
	private File backupFile;
	private FileInputStream in;
	private Timer timer;
	private ScheduledBackup backup;

	/**
	 * Load the data to memory, and set up the needed logic for upcoming backups
	 */
	public DataHandler() {
		m = new HashMap<>();

		// Path used when executed from eclipse
		backupFile = new File(CURRENT_PATH + "/../backup.txt");
		if (!backupFile.canRead()) {

			// Path used when executed using Apache Ant
			backupFile = new File(CURRENT_PATH + "/../../../backup.txt");
		}
		loadFromBackup();
		timer = new Timer();
		backup = new ScheduledBackup(m, backupFile);
		backup.run();

		// Than backup every one second
		timer.schedule(backup, 0, 1000);
	}

	/**
	 * Add a student to the collection
	 * 
	 * @param s
	 *            Student
	 * @return true if the student was successfully added
	 */
	public boolean add(Student s) {
		if (m.size() >= MAX_ENTRIES) {
			return false;
		}
		m.put(s.getId(), s);
		saveToBackup();
		return true;
	}

	/**
	 * Removes a student from the collection
	 * 
	 * @param id
	 *            Student's id number
	 */
	public void remove(Integer id) {
		if (m.remove(id) != null) {
			saveToBackup();
		}
	}

	/**
	 * Find a student using an id number
	 * 
	 * @param id
	 *            Studen't id number
	 * @return The student, or null if a student wasn't found
	 */
	public Student get(int id) {
		return m.get(id);
	}

	/**
	 * Inform that the data on memory was changed, and a a backup is needed
	 */
	public void saveToBackup() {
		backup.notifyChanged();
	}

	/**
	 * 
	 * @return True if backup is empty
	 * @throws IOException
	 */
	private boolean checkifBackupIsEmpty() throws IOException {
		return backupFile.length() < 10;
	}

	/**
	 * Load data from the backup file to memory
	 */
	private void loadFromBackup() {
		Properties properties = new Properties();
		try {
			in = new FileInputStream(backupFile);
			if (checkifBackupIsEmpty()) {
				return;
			}
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (String key : properties.stringPropertyNames()) {
			Student s = new Student(properties.get(key).toString());
			m.put(s.getId(), s);
		}
	}
}