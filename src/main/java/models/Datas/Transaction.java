package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Transaction implements ModelInitializable {
	private String transactionID;
	private String dailySalesHistoryID;
	private int soldQuantity;
	private String itemID;
	private String salesID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.transactionID = data.get("transactionID");
		this.dailySalesHistoryID = data.get("dailySalesHistoryID");
		this.soldQuantity = Integer.parseInt(data.get("soldQuantity"));
		this.itemID = data.get("itemID");
		this.salesID = data.get("salesID");
	}

	public Transaction() {}

	public Transaction(String[] data) {
		transactionID = data[0];
		dailySalesHistoryID = data[1];
		soldQuantity = Integer.parseInt(data[2]);
		itemID = data[3];
		salesID = data[4];
	}

	public String getTransactionID() {
		return transactionID;
	}

	public String getDailySalesHistoryID() {
		return dailySalesHistoryID;
	}

	public int getSoldQuantity() {
		return soldQuantity;
	}

	public String getItemID() {
		return itemID;
	}

	public String getSalesID() {
		return salesID;
	}
}
