package models.Users;

public class User {
	private int user_id;
	private String username;
	private String email;
	private String password;
	private String position;
	private int age;
	private int role_id;

	public User(String username, int age) {
		this.username = username;
		this.age = age;
	}

	public User() {

	}

	public String getName() {
		return this.username;
	}

	public String getEmail() {
		return this.email;
	}

	public String getPassword() {
		return this.password;
	}

	public String getPosition() {
		return this.position;
	}

	public int getAge() {
		return this.age;
	}

	public int getId() {
		return this.user_id;
	}

	public int getRole() {
		return this.role_id;
	}

	public void setName(String username) {
		this.username = username;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setId(int id) {
		this.user_id = id;
	}

	public void setRole(int role) {
		this.role_id = role;
	}

	public void login(String[] data) {

	}

	public void register(String[] data) {

	}

}
