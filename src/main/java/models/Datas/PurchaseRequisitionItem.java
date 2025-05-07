package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseRequisitionItem implements ModelInitializable {
	private String prItemID;
	private int quantity;
	private String prRequisitionID;
	private String itemID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prItemID = data.get("prItemID");
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.prRequisitionID = data.get("prRequisitionID");
		this.itemID = data.get("itemID");
	}
}
