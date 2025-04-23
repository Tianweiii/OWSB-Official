package models.Datas;

import models.ModelInitializable;
import models.Utils.QueryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class Supplier implements ModelInitializable {
	private int supplierID;
	private String companyName;
	private String phoneNumber;
	private String address;

	public Supplier() {

	}

	public Supplier(int supplierID, String companyName, String phoneNumber, String address) {
		this.supplierID = supplierID;
		this.companyName = companyName;
		this.phoneNumber = phoneNumber;
		this.address = address;
	}

	public int getSupplierID() {
		return supplierID;
	}

	public void setSupplierID(int supplierID) {
		this.supplierID = supplierID;
	}

	public String getCompany() {
		return companyName;
	}

	public void setCompany(String companyName) {
		this.companyName = companyName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public void initialize(HashMap<String, String> data) {

	}

	public static ArrayList<HashMap<String, String>> getSupplierNameById(int supplierID) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
		ArrayList<HashMap<String, String>> suppliers = qb.select(new String[]{"companyName"})
				.from("db/Supplier")
				.where("supplierID", "=" , String.valueOf(supplierID))
				.get();
		return suppliers;
	}


}
