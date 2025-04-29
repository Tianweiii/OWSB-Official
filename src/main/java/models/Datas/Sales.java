package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class Sales implements ModelInitializable {
	private int sales_id;
	private String created_at;
	private String updated_at;
	private int user_id;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.sales_id = Integer.parseInt(data.get("sales_id"));
		this.created_at = data.get("created_at");
		this.updated_at = data.get("updated_at");
		this.user_id = Integer.parseInt(data.get("user_id"));
	}
}
