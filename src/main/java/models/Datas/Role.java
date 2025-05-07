package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Role implements ModelInitializable {
	private String roleID;
	private String roleName;

	public String getId() {
		return roleID;
	}

	public String getName() {
		return roleName;
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.roleID = data.get("roleID");
		this.roleName = data.get("roleName");
	}
}
