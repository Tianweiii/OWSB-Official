package models.DTO;

import java.time.LocalDateTime;
import java.util.HashMap;

public class ItemListDTO {
	private String itemID;
	private String itemName;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private int alertSetting;
	private int quantity;
	private double unitPrice;
	private String supplierID;
	private String supplierName;

	public ItemListDTO(String itemID, String itemName, String description, LocalDateTime createdAt, LocalDateTime updatedAt, int alertSetting, int quantity, double unitPrice, String supplierID, String supplierName) {
		this.itemID = itemID;
		this.itemName = itemName;
		this.description = description;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.alertSetting = alertSetting;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.supplierID = supplierID;
		this.supplierName = supplierName;
	}

	public String getItemID() {
		return itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public String getDescription() {
		return description;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public int getAlertSetting() {
		return alertSetting;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public String getSupplierID() {
		return supplierID;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public void setSupplierID(String supplierID) {
		this.supplierID = supplierID;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setAlertSetting(int alertSetting) {
		this.alertSetting = alertSetting;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public HashMap<String, String> toMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("itemID", itemID);
		map.put("itemName", itemName);
		map.put("description", description);
		map.put("createdAt", createdAt.toString());
		map.put("updatedAt", updatedAt.toString());
		map.put("alertSetting", String.valueOf(alertSetting));
		map.put("quantity", String.valueOf(quantity));
		map.put("unitPrice", String.valueOf(unitPrice));
		map.put("supplierID", supplierID);
		map.put("supplierName", supplierName);
		return map;
	}

}
