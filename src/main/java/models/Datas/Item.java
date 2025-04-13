package models.Datas;

public class Item {
	private int itemID;
	private String itemName;
	private String createdAt;
	private String updatedAt;
	private String alertSetting;
	private int quantity;
	private int supplierID;

	public Item(int itemID, String itemName, String createdAt, String updatedAt, String alertSetting, int quantity, int supplierID) {
		this.itemID = itemID;
		this.itemName = itemName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.alertSetting = alertSetting;
		this.quantity = quantity;
		this.supplierID = supplierID;
	}

	public int getItemID() {
		return itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getAlertSetting() {
		return alertSetting;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getSupplierID() {
		return supplierID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setAlertSetting(String alertSetting) {
		this.alertSetting = alertSetting;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setSupplierID(int supplierID) {
		this.supplierID = supplierID;
	}

	@Override
	public String toString() {
		return "Item{" +
				"itemID=" + itemID +
				", itemName='" + itemName + '\'' +
				", createdAt='" + createdAt + '\'' +
				", updatedAt='" + updatedAt + '\'' +
				", alertSetting='" + alertSetting + '\'' +
				", quantity=" + quantity +
				", supplierID=" + supplierID +
				'}';
	}
}
