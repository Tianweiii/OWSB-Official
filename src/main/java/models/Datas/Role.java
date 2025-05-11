package models.Datas;

import models.ModelInitializable;
import models.Utils.QueryBuilder;

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

	public static Role getRole(String role) {
		try {
			QueryBuilder<Role> qb = new QueryBuilder<>(Role.class);
			String className = qb.getClassName();
			return qb
					.select()
					.from("db/" + className + ".txt")
					.where("roleName", "=", role)
					.getAsObjects().get(0);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	@Override
	public void initialize(HashMap<String, String> data) {
		this.roleID = data.get("roleID");
		this.roleName = data.get("roleName");
	}
}
