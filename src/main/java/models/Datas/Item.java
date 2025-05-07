package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Item implements ModelInitializable {
	private String itemID;
	private String itemName;
	private String createdAt;
	private String updatedAt;
	private String alertSetting;
	private int quantity;
	private String supplierID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.itemID = data.get("itemID");
		this.itemName = data.get("itemName");
		this.createdAt = data.get("createdAt");
		this.updatedAt = data.get("updatedAt");
		this.alertSetting = data.get("alertSetting");
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.supplierID = data.get("supplierID");
	}

	public String getItemName() {
		return this.itemName;
	}
}
