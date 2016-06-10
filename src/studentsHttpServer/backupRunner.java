package studentsHttpServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class backupRunner {
	private int count;
	private FileOutputStream out;
	private Map<Integer, Student> m;
	private File backup;
	private boolean lock1 = false;
	private boolean lock2 = false;
	private Lock lock = new ReentrantLock();

	public backupRunner(Map<Integer, Student> m, File backup) {
		this.m = m;
		this.backup = backup;
		try {
			out = new FileOutputStream(backup);
		} catch (IOException e) {
			e.printStackTrace();//do something if needed...
		}
		count = 0;
	}

	public int getVacantThread() {
		if (lock1 && lock2) {
			return -1;
		}
		if(lock1){
			lock2 = true;
			return 2;
		}
		lock1 = true;
		return 1;
	}

	public boolean isEmpty() {
		return !lock1 && !lock2;
	}

	public void wait1() {


		lock.lock();
		try {
			save();
		} finally {
			lock.unlock();
		}
		lock1 = false;
	}

	public void wait2() {


		lock.lock();
		try {
			save();
		} finally {
			lock.unlock();
		}
		lock2 = false;
	}

	private void save() {
		System.out.println("executed2");
		Properties properties = new Properties();
		for (Map.Entry<Integer, Student> entry : m.entrySet()) {
			properties.put(Integer.toString(entry.getKey()), entry.getValue().toURLString());
		}
		try {
			properties.store(out, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
