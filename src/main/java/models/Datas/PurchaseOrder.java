package models.Datas;

import models.DTO.PODataDTO;
import models.DTO.POItemDTO;
import models.ModelInitializable;
import models.Users.User;

import java.util.HashMap;
import java.util.List;

public class PurchaseOrder implements ModelInitializable {
	private String poID;
	private String prID;
	private String userID;
	private String title;
	private String payableAmount;
	private String POStatus;

	public PurchaseOrder(){

	}

	public PurchaseOrder(String poID, String prID, String userID, String title, String payableAmount, String POStatus) {
		this.poID = poID;
		this.prID = prID;
		this.userID = userID;
		this.title = title;
		this.payableAmount = payableAmount;
		this.POStatus = POStatus;
	}

	public String getpoID() {
		return poID;
	}

	public void setpoID(String poID) {
		this.poID = poID;
	}

	public String getPrRequisitionID() {
		return prID;
	}

	public void setPrRequisitionID(String prID) {
		this.prID = prID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPayableAmount() {
		return payableAmount;
	}

	public void setPayableAmount(String payableAmount) {
		this.payableAmount = payableAmount;
	}

	public String getPOStatus() {
		return POStatus;
	}

	public void setPOStatus(String POStatus) {
		this.POStatus = POStatus;
	}

	public PODataDTO getPODataDTO(User user, PurchaseRequisition PR, List<POItemDTO> itemList, int totalQuantity){
		return new PODataDTO(
					user.getName(),
					user.getId(),
					this.getPrRequisitionID(),
					this.getpoID(),
					this.getPOStatus(),
					this.getTitle(),
					PR.getCreatedDate(),
					PR.getReceivedByDate(),
					itemList,
					totalQuantity,
					Double.parseDouble(this.getPayableAmount())
				);
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.poID = data.get("poID");
		this.prID = data.get("prID");
		this.userID = data.get("userID");
		this.title = data.get("title");
		this.payableAmount = data.get("payableAmount");
		this.POStatus = data.get("POStatus");
	}
}
