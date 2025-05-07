package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseRequisition implements ModelInitializable {
	private String prRequisitionID;
	private String receivedByDate;
	private String createdDate;
	private String userID;
	private String PRStatus;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prRequisitionID = data.get("prRequisitionID");
		this.receivedByDate = data.get("receivedByDate");
		this.createdDate = data.get("createdDate");
		this.userID = data.get("userID");
		this.PRStatus = data.get("PRStatus");
	}
}
