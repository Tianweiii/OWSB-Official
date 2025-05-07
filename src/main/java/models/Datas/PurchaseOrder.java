package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseOrder implements ModelInitializable {
	private int pr_order_id;
	private int pr_requisition_id;
	private int user_id;
	private int pr_order_status_id;



	@Override
	public void initialize(HashMap<String, String> data) {
		this.pr_order_id = Integer.parseInt(data.get("pr_order_id"));
		this.pr_requisition_id = Integer.parseInt(data.get("pr_requisition_id"));
		this.user_id = Integer.parseInt(data.get("user_id"));
		this.pr_order_status_id = Integer.parseInt(data.get("pr_order_status_id"));
	}
}
