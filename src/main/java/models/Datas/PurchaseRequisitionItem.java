package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseRequisitionItem implements ModelInitializable {
	private int pr_item_id;
	private int quantity;
	private int pr_requisition_id;
	private int item_id;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.pr_item_id = Integer.parseInt(data.get("pr_item_id"));
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.pr_requisition_id = Integer.parseInt(data.get("pr_requisition_id"));
		this.item_id = Integer.parseInt(data.get("item_id"));
	}

	public PurchaseRequisitionItem() {}
}
