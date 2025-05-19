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

	public Sales(String salesID, String createdAt, String updatedAt, String userID) {
		this.salesID = salesID;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.userID = userID;
	}

	public String getSalesID() {
		return salesID;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getUserID() {
		return userID;
	}
}
