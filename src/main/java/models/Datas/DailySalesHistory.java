package models.Datas;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import models.ModelInitializable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class DailySalesHistory implements ModelInitializable {
	private String dailySalesHistoryID;
	private LocalDate createdAt;
	private LocalDate updatedAt;
	private String status;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.dailySalesHistoryID = data.get("dailySalesHistoryID");
		this.createdAt = LocalDate.parse(data.get("createdAt"), DateTimeFormatter.ISO_DATE);
		this.updatedAt = LocalDate.parse(data.get("updatedAt"), DateTimeFormatter.ISO_DATE);
		this.status = data.getOrDefault("status", "Pending");
	}

	public String getDailySalesHistoryID() {
		return this.dailySalesHistoryID;
	}

	public LocalDate getCreatedAt() {
		return this.createdAt;
	}

	public LocalDate getUpdatedAt() {
		return this.updatedAt;
	}
	
	public String getStatus() {
		return this.status == null ? "Pending" : this.status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "History#" + getDailySalesHistoryID()
				+ " [" + getCreatedAt() + "] - " + getStatus();
	}
}
