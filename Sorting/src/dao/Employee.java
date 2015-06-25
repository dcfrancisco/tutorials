package dao;

public class Employee {
	private int id;
	private String name;
	private String email;

	public Employee(String line) {
		String[] data = line.split(",");
		setId(Integer.parseInt(data[0]));
		setName(data[1]);
		setEmail(data[2]);
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
