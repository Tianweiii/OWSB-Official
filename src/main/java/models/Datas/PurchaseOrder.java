package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseOrder implements ModelInitializable {
	private String PO_ID;
	private String PR_ID;
	private String userID;
	private String title;
	private double payableAmount;
	private String status;

	@Override
	public void initialize(HashMap<String, String> data) {
		PO_ID = data.get("PO_ID");
		PR_ID = data.get("PR_ID");
		userID = data.get("userID");
		title = data.get("title");
		payableAmount = Double.parseDouble(data.get("payableAmount"));
		status = data.get("status");
	}

	public PurchaseOrder() {}
}
