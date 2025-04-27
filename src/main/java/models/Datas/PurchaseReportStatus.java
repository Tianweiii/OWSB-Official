package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseReportStatus implements ModelInitializable {
	private int pr_report_status_id;
	private String status_name;
	private String status_description;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.pr_report_status_id = Integer.parseInt(data.get("pr_report_status_id"));
		this.status_name = data.get("status_name");
		this.status_description = data.get("status_description");
	}
}
