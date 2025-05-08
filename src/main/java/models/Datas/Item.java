package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Item implements ModelInitializable {
	private String item_id;
	private String item_name;
	private String created_at;
	private String updated_at;
	private String alert_setting;
	private int quantity;
	private double unitPrice;
	private String supplier_id;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.item_id = data.get("item_id");
		this.item_name = data.get("item_name");
		this.created_at = data.get("created_at");
		this.updated_at = data.get("updated_at");
		this.alert_setting = data.get("alert_setting");
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.unitPrice = Double.parseDouble(data.get("unitPrice"));
		this.supplier_id = data.get("supplier_id");
	}

	public String getItemID() { return this.item_id; }

	public String getItemName() {
		return this.item_name;
	}

	public String getSupplierID() {return supplier_id; }

	public double getUnitPrice() { return unitPrice; }

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
}
