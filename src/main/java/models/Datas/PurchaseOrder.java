package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseOrder implements ModelInitializable {
	private String prOrderID;
	private String prRequisitionID;
	private String userID;
	private String title;
	private String payableAmount;
	private String POStatus;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prOrderID = data.get("prOrderID");
		this.prRequisitionID = data.get("prRequisitionID");
		this.userID = data.get("userID");
		this.title = data.get("title");
		this.payableAmount = data.get("payableAmount");
		this.POStatus = data.get("POStatus");
	}
}
