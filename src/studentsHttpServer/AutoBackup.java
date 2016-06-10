package studentsHttpServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;

public class AutoBackup extends TimerTask{
	private boolean isChanged;
	private FileOutputStream out;
	private Map<Integer, Student> m;
	private File backup;
	
	public AutoBackup(Map<Integer, Student> m, File backup){
		this.m = m;
		this.backup = backup;
		isChanged = false;
	}
	public void notifyChanged(){
		isChanged = true;
	}
	
	public void run(){
		if(!isChanged){
			return ;//do nothing
		}
		isChanged = false;
		Properties properties = new Properties();
		for (Map.Entry<Integer, Student> entry : m.entrySet()) {
			properties.put(Integer.toString(entry.getKey()), entry.getValue().toURLString());
		}
		try {
			out = new FileOutputStream(backup);
			properties.store(out, null);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(out != null){
			
				try {
					out.close();
				} catch (IOException e) {
					//ignore this exception
				}
			}
		}
		System.out.println("backup file updated");
	}

}
