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

	@Override
	public void initialize(HashMap<String, String> data) {
		this.dailySalesHistoryID = data.get("dailySalesHistoryID");
		this.createdAt = LocalDate.parse(data.get("createdAt"), DateTimeFormatter.ISO_DATE);
		this.updatedAt = LocalDate.parse(data.get("updatedAt"), DateTimeFormatter.ISO_DATE);
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
	@Override
	public String toString() {
		return "History#" + getDailySalesHistoryID()
				+ " [" + getCreatedAt() + "]";
	}
}
