package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;


public class PurchaseRequisitionItem implements ModelInitializable {
	private String prItemID;
	private int quantity;
	private String prRequisitionID;
	private String itemID;

	public PurchaseRequisitionItem() {}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prItemID = data.get("prItemID");
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.prRequisitionID = data.get("prRequisitionID");
		this.itemID = data.get("itemID");
	}

	public String getPrItemID() {
		return this.prItemID;
	}
	public int getQuantity() {
		return this.quantity;
	}

	public String getPrRequisitionID() {
		return this.prRequisitionID;
	}
	public String getItemID() {
		return this.itemID;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	@Override
	public String toString() {
		return "PurchaseRequisitionItem{" +
				"prItemID='" + prItemID + '\'' +
				", quantity=" + quantity +
				", prRequisitionID='" + prRequisitionID + '\'' +
				", itemID='" + itemID + '\'' +
				'}';
	}
}
