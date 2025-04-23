package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Role implements ModelInitializable {
	private int role_id;
	private String role_name;

	public int getId() {
		return role_id;
	}

	public String getName() {
		return role_name;
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.role_id = Integer.parseInt(data.get("role_id"));
		this.role_name = data.get("role_name");
	}
}
