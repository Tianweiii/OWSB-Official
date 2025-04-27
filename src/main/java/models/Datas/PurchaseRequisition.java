package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseRequisition implements ModelInitializable {
	private int pr_requisition_id;
	private String received_by_date;
	private int user_id;
	private int pr_report_status_id;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.pr_requisition_id = Integer.parseInt(data.get("pr_requisition_id"));
		this.received_by_date = data.get("received_by_date");
		this.user_id = Integer.parseInt(data.get("user_id"));
		this.pr_report_status_id = Integer.parseInt(data.get("pr_report_status_id"));
	}
}
