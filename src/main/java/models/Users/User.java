package models.Users;

import models.ModelInitializable;

import java.util.HashMap;

public class User implements ModelInitializable {
	private String userID;
	private String username;
	private String email;
	private String password;
	private String position;
	private int age;
	private String roleID;

	public User(String username, int age) {
		this.username = username;
		this.age = age;
	}

	public User() {

	}

	public User(String user_id, String username, String email, String password, String position, int age, String role_id) {
		this.userID = user_id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.position = position;
		this.age = age;
		this.roleID = role_id;
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.userID = data.get("userID") != null ? data.get("userID") : "none";
		this.username = data.get("username");
		this.email = data.get("email");
		this.password = data.get("password");
		this.position = data.get("position");
		this.age = data.get("age") != null ? Integer.parseInt(data.get("age")) : 0;
		this.roleID = data.get("roleID") != null ? data.get("roleID") : "none";
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

	public String getId() {
		return this.userID;
	}

	public String getRole() {
		return this.roleID;
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

	public void setId(String id) {
		this.userID = id;
	}

	public void setRole(String role) {
		this.roleID = role;
	}

	public void login(String[] data) {

	}

	public void register(String[] data) {

	}

	@Override
	public String toString() {
		return "User{" +
				"userID='" + userID + '\'' +
				", username='" + username + '\'' +
				", email='" + email + '\'' +
				", password='" + password + '\'' +
				", position='" + position + '\'' +
				", age=" + age +
				", roleID='" + roleID + '\'' +
				'}';
	}
}
