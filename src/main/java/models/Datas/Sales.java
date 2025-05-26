package models.Datas;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import models.ModelInitializable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Sales implements ModelInitializable {
	private String salesID;
	private LocalDate createdAt;
	private LocalDate updatedAt;
	private String userID;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.salesID = data.get("salesID");
		this.createdAt = LocalDate.parse(data.get("createdAt"), DateTimeFormatter.ISO_DATE);
		this.updatedAt = LocalDate.parse(data.get("updatedAt"), DateTimeFormatter.ISO_DATE);
		this.userID = data.get("userID");
	}

	public String getSalesID() {
		return this.salesID;
	}
	public StringProperty salesIDProperty() {
		return new SimpleStringProperty(this.salesID);
	}

	public LocalDate getCreatedAt() {
		return this.createdAt;
	}
	public LocalDate createdAtProperty() {
		return this.createdAt;
	}

	public LocalDate getUpdatedAt() {
		return this.updatedAt;
	}
	public LocalDate updatedAtProperty() {
		return this.updatedAt;
	}

	public String getUserID() {
		return this.userID;
	}
	public StringProperty userIDProperty() {
		return new SimpleStringProperty(this.userID);
	}

	@Override
	public String toString() {
		return "Sales#" + getSalesID() + " by " + getUserID();
	}

	public Sales(String salesID, String createdAt, String updatedAt, String userID) {
		this.salesID = salesID;
		this.createdAt = LocalDate.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.updatedAt = LocalDate.parse(updatedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.userID = userID;
	}

}
