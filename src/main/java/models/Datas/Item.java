package models.Datas;

import models.ModelInitializable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Item implements ModelInitializable {
	private String itemID;
	private String itemName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String alertSetting;
	private int quantity;
	private double unitPrice;
	private String supplierID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.itemID = data.get("itemID");
		this.itemName = data.get("itemName");
		this.createdAt = LocalDateTime.parse(data.get("createdAt"));
		this.updatedAt = LocalDateTime.parse(data.get("updatedAt"));
		this.alertSetting = data.get("alertSetting");
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.supplierID = data.get("supplierID");
	}

	public Item(String[] data) {
		itemID = data[0];
		itemName = data[1];
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		createdAt = LocalDateTime.parse(data[2], formatter);
		updatedAt = LocalDateTime.parse(data[3], formatter);
		alertSetting = data[4];
		quantity = Integer.parseInt(data[5]);
		unitPrice = Double.parseDouble(data[6]);
		supplierID = data[7];
	}

	public Item() {}

	public Item(String itemID, String itemName, double unitPrice) {
		this.itemID = itemID;
		this.itemName = itemName;
		this.unitPrice = unitPrice;
    }

	public String getItemID() { return this.itemID; }

	public String getItemName() {
		return this.itemName;
	}

	public String getSupplierID() {return supplierID; }

	public double getUnitPrice() { return unitPrice; }

}
