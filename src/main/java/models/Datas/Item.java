package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Item implements ModelInitializable {
	private int item_id;
	private String item_name;
	private String created_at;
	private String updated_at;
	private String alert_setting;
	private int quantity;
	private int supplier_id;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.item_id = Integer.parseInt(data.get("item_id"));
		this.item_name = data.get("item_name");
		this.created_at = data.get("created_at");
		this.updated_at = data.get("updated_at");
		this.alert_setting = data.get("alert_setting");
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.supplier_id = Integer.parseInt(data.get("supplier_id"));
	}
}
