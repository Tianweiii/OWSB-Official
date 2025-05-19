package models.Datas;

import models.DTO.PODataDTO;
import models.DTO.POItemDTO;
import models.DTO.PRDataDTO;
import models.DTO.PRItemDTO;
import models.ModelInitializable;
import models.Users.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class PurchaseRequisition implements ModelInitializable {
	private String prRequisitionID;
	private LocalDate receivedByDate;
	private LocalDate createdDate;
	private String userID;
	private String PRStatus;

	public PurchaseRequisition() {
	}

	public PurchaseRequisition(String prRequisitionID, String receivedByDate, String createdDate, String userID, String PRStatus) {
		this.prRequisitionID = prRequisitionID;
		this.receivedByDate = LocalDate.parse(receivedByDate);
		this.createdDate = LocalDate.parse(createdDate);
		this.userID = userID;
		this.PRStatus = PRStatus;
	}

	public String getPrRequisitionID() {
		return prRequisitionID;
	}

	public void setPrRequisitionID(String prRequisitionID) {
		this.prRequisitionID = prRequisitionID;
	}

	public String getReceivedByDate() {
		return receivedByDate.toString();
	}

	public void setReceivedByDate(String receivedByDate) {
		this.receivedByDate = LocalDate.parse(receivedByDate);
	}

	public String getCreatedDate() {
		return createdDate.toString();
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = LocalDate.parse(createdDate);
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getPRStatus() {
		return PRStatus;
	}

	public void setPRStatus(String PRStatus) {
		this.PRStatus = PRStatus;
	}

	public PRDataDTO getPRDataDTO(User user, List<PRItemDTO> itemList, int totalQuantity){
		return new PRDataDTO(
				user.getName(),
				user.getId(),
				this.getPrRequisitionID(),
				this.getPRStatus(),
				this.getCreatedDate(),
				this.getReceivedByDate(),
				itemList,
				totalQuantity
		);
	}

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
