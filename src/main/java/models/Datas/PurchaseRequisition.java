package models.Datas;

import models.ModelInitializable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class PurchaseRequisition implements ModelInitializable {
	private String prRequisitionID;
	private LocalDate receivedByDate;
	private LocalDate createdDate;
	private String userID;
	private String PRStatus;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prRequisitionID = data.get("prRequisitionID");
		this.receivedByDate = parseDate(data.get("receivedByDate"));
		this.createdDate = parseDate(data.get("createdDate"));
		this.userID = data.get("userID");
		this.PRStatus = data.get("PRStatus");
	}

	private LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.isEmpty()) {
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
		return LocalDate.parse(dateStr, formatter);
	}

	public String getPrRequisitionID() {
		return this.prRequisitionID;
	}

	public LocalDate getReceivedByDate() {
		return this.receivedByDate;
	}

	public LocalDate getCreatedDate() {
		return this.createdDate;
	}

	public String getUserID() {
		return this.userID;
	}

	public String getPRStatus() {
		return this.PRStatus;
	}

	public void setPRStatus(String PRStatus) {
		this.PRStatus = PRStatus;
	}

	@Override
	public String toString() {
		return "PurchaseRequisition{" +
				"prRequisitionID='" + prRequisitionID + '\'' +
				", receivedByDate=" + receivedByDate +
				", createdDate=" + createdDate +
				", userID='" + userID + '\'' +
				", PRStatus='" + PRStatus + '\'' +
				'}';
	}
}
