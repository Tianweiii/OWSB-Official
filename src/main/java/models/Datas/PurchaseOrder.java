package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseOrder implements ModelInitializable {
	private String purchaseorder_id;
	private String pr_requisition_id;
	private String user_id;
	private String pr_order_status_id;

	public PurchaseOrder(String purchaseorder_id, String pr_requisition_id, String user_id, String pr_order_status_id) {
		this.purchaseorder_id = purchaseorder_id;
		this.pr_requisition_id = pr_requisition_id;
		this.user_id = user_id;
		this.pr_order_status_id = pr_order_status_id;
	}

	public PurchaseOrder() {}

	@Override
	public void initialize(HashMap<String, String> data) {
		purchaseorder_id = data.get("purchaseorder_id");
		pr_requisition_id = data.get("pr_requisition_id");
		user_id = data.get("user_id");
		pr_order_status_id = data.get("pr_order_status_id");
	}

	public String getpurchaseorder_id() {
		return purchaseorder_id;
	}

	public String getPr_requisition_id() {
		return pr_requisition_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public String getPr_order_status_id() {
		return pr_order_status_id;
	}


}
