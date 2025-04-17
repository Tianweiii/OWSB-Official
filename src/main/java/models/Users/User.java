package models.Users;

import models.Initializable;

import java.util.HashMap;

public class User implements Initializable {
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

	@Override
	public void initialize(HashMap<String, String> data) {
		this.user_id = data.get("user_id") != null ? Integer.parseInt(data.get("user_id")) : 0;
		this.username = data.get("username");
		this.email = data.get("email");
		this.password = data.get("password");
		this.position = data.get("position");
		this.age = data.get("age") != null ? Integer.parseInt(data.get("age")) : 0;
		this.role_id = data.get("role_id") != null ? Integer.parseInt(data.get("role_id")) : 0;
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
