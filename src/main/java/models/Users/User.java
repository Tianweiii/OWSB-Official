package models.Users;

import models.ModelInitializable;

import java.util.HashMap;

public class User implements ModelInitializable {
	private String user_id;
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

	public User(String user_id, String username, String email, String password, String position, int age, int role_id) {
		this.user_id = user_id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.position = position;
		this.age = age;
		this.role_id = role_id;
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.user_id = data.get("user_id");
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

	public String getId() {
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

	public void setId(String id) {
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
