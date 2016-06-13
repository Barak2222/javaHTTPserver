package studentsHttpServer;

import java.io.File;
import java.util.Map;
import java.util.TimerTask;

/**
 * Responsible for every check that a backup is needed. If it is indeed needed,
 * launch the thread that performs the backup
 * 
 * @author Barak
 *
 */
public class ScheduledBackup extends TimerTask {
	private boolean isChanged;
	private BackupRunner br;

	/**
	 * Initialise the needed logic for scheduled backups
	 * 
	 * @param m
	 *            Reference to the data HashMap
	 * @param backupFile
	 *            file object
	 */
	public ScheduledBackup(Map<Integer, Student> m, File backupFile) {
		isChanged = true;
		br = new BackupRunner(m, backupFile);
	}

	public void notifyChanged() {
		isChanged = true;
	}

	/**
	 * Execute backup, and make sure that it is needed
	 */
	public void run() {
		if (!isChanged) {
			return;// do nothing
		}
		isChanged = false;
		Thread t = new Thread(br);
		t.start();
	}

}
