package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Supplier implements ModelInitializable {
	private int supplier_id;
	private String supplier_name;
	private String company;
	private String phone_number;
	private String address;

	public int getSupplierId() {
		return this.supplier_id;
	}

	public String getCompanyName() {
		return this.company;
	}

	public String getSupplierName() {
		return this.supplier_name;
	}

	public String getPhoneNumber() {
		return this.phone_number;
	}

	public String getAddress() {
		return this.address;
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.supplier_id = Integer.parseInt(data.get("supplier_id"));
		this.supplier_name = data.get("supplier_name");
		this.company = data.get("company");
		this.phone_number = data.get("phone_number");
		this.address = data.get("address");
	}
}
