package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Transaction implements ModelInitializable {
	private String transactionID;
	private String createdAt;
	private String updatedAt;
	private int soldQuantity;
	private String itemID;
	private String salesID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.transactionID = data.get("transactionID");
		this.createdAt = data.get("createdAt");
		this.updatedAt = data.get("updatedAt");
		this.soldQuantity = Integer.parseInt(data.get("soldQuantity"));
		this.itemID = data.get("itemID");
		this.salesID = data.get("salesID");
	}
}
