package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Sales implements ModelInitializable {
	private String salesID;
	private String createdAt;
	private String updatedAt;
	private String userID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.salesID = data.get("salesID");
		this.createdAt = data.get("createdAt");
		this.updatedAt = data.get("updatedAt");
		this.userID = data.get("userID");
	}
}
