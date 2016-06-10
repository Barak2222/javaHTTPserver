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

public class DataHandler {
	static final int MAX_ENTRIES = 1000;
	private Map<Integer, Student> m;
	private File backup;
	private FileInputStream in;
	protected Queue<Thread> backupQueue;
	private Timer timer;
	private AutoBackup autoBackup;

	// {TOCHECK} why after reading the file deletes itself?
	public DataHandler() {
		m = new HashMap<>();

		backup = new File("/Users/Barak/workspace/studentsHttpServer/src/studentsHttpServer/backup.txt");
		try {
			in = new FileInputStream(backup);
			loadFromBackup();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		timer = new Timer();
		autoBackup = new AutoBackup(m, backup);
		timer.schedule(autoBackup, 0, 1000);
		saveToBackup();
	}

	// {TODO} handle duplicates?
	public boolean add(Student s) {
		if (m.size() >= MAX_ENTRIES) {
			return false;
		}
		m.put(s.getId(), s);
		saveToBackup();
		return true;
	}

	public void remove(Integer id) {
		if (m.remove(id) != null) {
			saveToBackup();
		}
	}

	public Student get(int id) {
		return m.get(id);
	}

	public void reset() {
		m = new HashMap<>();

		// Delete backup
		try {
			PrintWriter writer = new PrintWriter(backup);
			writer.print("");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveToBackup() {
		autoBackup.notifyChanged();
	}

	private boolean checkifBackupIsEmpty() throws IOException {
		return backup.length() < 10;
	}

	public void loadFromBackup() {
		Properties properties = new Properties();

		try {
			if (checkifBackupIsEmpty()) {
				return;
			}
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String key : properties.stringPropertyNames()) {
			Student s = new Student(properties.get(key).toString());
			m.put(s.getId(), s);
		}
		System.out.println("After reading rom backup, size: " + m.size());
	}
}