package studentsHttpServer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataHandlerTest {
	DataHandler d;
	
	@Before
	public void setup(){
		d = new DataHandler();
	}
	
	private Student randStudent(){
		int firstDigit = (int)(Math.random()*9 + 1);
		int id = (int)(Math.random()*100000000) + firstDigit*100000000;
		int grade = (int)(Math.random()*100 + 1);
		return new Student(id, Student.DEF, Student.DEF, grade);
	}
	private Student[] createRandomStudentsArr(int N){
		Student[] arr = new Student[N];
		for (int i = 0; i < N; i++) {
			arr[i] = randStudent();
			d.add(arr[i]);
		}
		return arr;
	}
	/**
	@Test
	public void testMapping() {
		Student[] students = createRandomStudentsArr(20);
		String st1 = students[0].toURLString();
		d.remove(students[0].getId());
		assertEquals(null, d.get(students[0].getId()));
		d.add(new Student(st1));
		assertNotEquals(null, d.get(students[0].getId()));
	}
	*/
	@Test
	public void testBackup(){
		Student[] arr1 = createRandomStudentsArr(20);
		d.closeConnection();
		waitf();
		d = new DataHandler();
		Student[] arr2 = createRandomStudentsArr(20);
		assertNotEquals(null, d.get(arr1[0].getId()));
		assertNotEquals(null, d.get(arr1[10].getId()));
		assertNotEquals(null, d.get(arr2[0].getId()));
		d.remove(arr1[5].getId());
		d.remove(arr2[5].getId());
		assertEquals(null, d.get(arr1[5].getId()));
		assertEquals(null, d.get(arr2[5].getId()));
		d.closeConnection();

		waitf();
		d = new DataHandler();
		
		assertNotEquals(null, d.get(arr1[0].getId()));
		assertEquals(null, d.get(arr1[5].getId()));
		System.out.println("finished");
	}
	@After
	public void restore(){
		d.reset();
		d.closeConnection();
	}
	
	private void waitf(){
		try {
		    Thread.sleep(1500);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
}
