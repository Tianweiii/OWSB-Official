package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Supplier implements ModelInitializable {
	private int supplier_id;
	private String supplier_name;
	private String company;
	private String phone_number;
	private String address;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.supplier_id = Integer.parseInt(data.get("supplier_id"));
		this.supplier_name = data.get("name");
		this.company = data.get("company");
		this.phone_number = data.get("phone_number");
		this.address = data.get("address");
	}
}
