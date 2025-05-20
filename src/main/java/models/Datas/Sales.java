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
	private final StringProperty salesID   = new SimpleStringProperty();
	private final ObjectProperty<LocalDate> createdAt  = new SimpleObjectProperty<>();
	private final ObjectProperty<LocalDate> updatedAt  = new SimpleObjectProperty<>();
	private final StringProperty userID    = new SimpleStringProperty();

	private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;

	@Override
	public void initialize(HashMap<String, String> data) {
		salesID.set(data.get("salesID"));
		createdAt.set(LocalDate.parse(data.get("createdAt"), ISO));
		updatedAt.set(LocalDate.parse(data.get("updatedAt"), ISO));
		userID.set(data.get("userID"));
	}

	public String getSalesID() {
		return this.salesID.get();
	}
	public StringProperty salesIDProperty() {
		return this.salesID;
	}

	public LocalDate getCreatedAt() {
		return this.createdAt.get();
	}
	public ObjectProperty<LocalDate> createdAtProperty() {
		return this.createdAt;
	}

	public LocalDate getUpdatedAt() {
		return this.updatedAt.get();
	}
	public ObjectProperty<LocalDate> updatedAtProperty() {
		return this.updatedAt;
	}

	public String getUserID() {
		return this.userID.get();
	}
	public StringProperty userIDProperty() {
		return this.userID;
	}

	@Override
	public String toString() {
		return "Sales#" + getSalesID() + " by " + getUserID();
	}

	public Sales(String salesID, String createdAt, String updatedAt, String userID) {
		this.salesID = salesID;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.userID = userID;
	}

	public String getSalesID() {
		return salesID;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getUserID() {
		return userID;
	}
}
