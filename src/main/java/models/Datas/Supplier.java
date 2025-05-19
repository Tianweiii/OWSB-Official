package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Supplier implements ModelInitializable {
	private String supplierID;
	private String supplierName;
	private String company;
	private String phoneNumber;
	private String address;

	public String getSupplierId() {
		return this.supplierID;
	}

	public String getCompanyName() {
		return this.company;
	}

	public String getSupplierName() {
		return this.supplierName;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public String getAddress() {
		return this.address;
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.supplierID = data.get("supplierID");
		this.supplierName = data.get("supplierName");
		this.company = data.get("company");
		this.phoneNumber = data.get("phoneNumber");
		this.address = data.get("address");
	}

	// for the fileIO class init
	public Supplier(String[] data) {
		supplierID = data[0];
		supplierName = data[1];
		company = data[2];
		phoneNumber = data[3];
		address = data[4];
	}
}
