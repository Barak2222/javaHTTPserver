package studentsHttpServer;

import java.io.File;
import java.util.Map;
import java.util.TimerTask;

public class ScheduledBackup extends TimerTask{
	private boolean isChanged;
	private BackupRunner br;
	
	public ScheduledBackup(Map<Integer, Student> m, File backup){
		isChanged = true;
		br = new BackupRunner(m, backup);
	}
	public void notifyChanged(){
		isChanged = true;
	}
	
	/**
	 * Execute backup, and make sure that it is needed
	 */
	public void run(){
		if(!isChanged){
			return ;//do nothing
		}
		isChanged = false;
		Thread t = new Thread(br);
		t.start();
	}

}
