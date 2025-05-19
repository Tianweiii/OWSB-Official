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

	public Item(String[] data) {
		item_id = data[0];
		item_name = data[1];
		created_at = data[2];
		updated_at = data[3];
		alert_setting = data[4];
		quantity = Integer.parseInt(data[5]);
		unitPrice = Double.parseDouble(data[6]);
		supplier_id = data[7];
	}

	public Item() {}

	public Item(String itemID, String itemName, double unitPrice) {
		this.item_id = itemID;
		this.item_name = itemName;
		this.unitPrice = unitPrice;
    }

	public String getItemID() { return this.item_id; }

	public String getItemName() {
		return this.itemName;
	}

	public String getSupplierID() {return supplier_id; }

	public double getUnitPrice() { return unitPrice; }

}
