package models.Datas;

import models.DTO.PODataDTO;
import models.DTO.POItemDTO;
import models.DTO.PRDataDTO;
import models.DTO.PRItemDTO;
import models.ModelInitializable;
import models.Users.User;

import java.util.HashMap;
import java.util.List;

public class PurchaseRequisition implements ModelInitializable {
	private String prRequisitionID;
	private String userID;
	private String PRStatus;
	private String createdDate;
	private String receivedByDate;

	public PurchaseRequisition() {
	}

	public PurchaseRequisition(String prRequisitionID, String receivedByDate, String createdDate, String userID, String PRStatus) {
		this.prRequisitionID = prRequisitionID;
		this.receivedByDate = receivedByDate;
		this.createdDate = createdDate;
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
		return receivedByDate;
	}

	public void setReceivedByDate(String receivedByDate) {
		this.receivedByDate = receivedByDate;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
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
		this.receivedByDate = data.get("receivedByDate");
		this.createdDate = data.get("createdDate");
		this.userID = data.get("userID");
		this.PRStatus = data.get("PRStatus");
	}
}
