package studentsHttpServer;

/**
 * Define a class for Student
 * @author Barak
 *
 */
public class Student {
	static final String DEF = "DEFAULT_VALUE";
	private int id;
	private String name;
	private String gender;
	private int grade;

	public Student(int id) {
		this.id = id;
		this.name = DEF;
		this.gender = DEF;
		this.grade = -1;
	}
	
	public Student(int id, String name, String gender, int grade) {
		this.id = id;
		this.name = name;
		this.gender = gender;
		this.grade = grade;
	}
	
	/**
	 * Parse string as a HTTP URL params to create Student object
	 * @param url
	 * @throws IllegalArgumentException if the string dont not have a legal id number
	 */
	public Student(String url) throws IllegalArgumentException{
		this(1);
		int verifyID = -1;
		String[] keys = url.split("&");
		for(String couple : keys){
			String[] current = couple.split("=");
			if(current[0].equals("id")){
				verifyID = Integer.parseInt(current[1]);
			} else if(current[0].equals("name")){
				this.name = current[1];
			} else if(current[0].equals("gender")){
				this.gender = current[1];
			} else if(current[0].equals("grade")){
				this.grade = Integer.parseInt(current[1]);
			}
		}
		if(verifyID < 0){
			throw new IllegalArgumentException("Student must have ID");
		}
		this.id = verifyID;
	}
	
	@Override
	public String toString() {
		return "id:" + id + "<br/>name:" + name + "<br/>gender:" + gender + "<br/>grade:" + grade;
	}
	public String toURLString(){
		return "id=" + id + "&name=" + name + "&gender=" + gender + "&grade=" + grade;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}
}
