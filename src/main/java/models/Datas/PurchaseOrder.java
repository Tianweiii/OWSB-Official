package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseOrder implements ModelInitializable {
	private String prOrderID;
	private String prRequisitionID;
	private String userID;
	private String title;
	private String payableAmount;
	private String POStatus;



	@Override
	public void initialize(HashMap<String, String> data) {
		this.prOrderID = data.get("prOrderID");
		this.prRequisitionID = data.get("prRequisitionID");
		this.userID = data.get("userID");
		this.title = data.get("title");
		this.payableAmount = data.get("payableAmount");
		this.POStatus = data.get("POStatus");
	}

    public String getPrOrderID() {
        return prOrderID;
    }

    public void setPrOrderID(String prOrderID) {
        this.prOrderID = prOrderID;
    }

    public String getPrRequisitionID() {
        return prRequisitionID;
    }

    public void setPrRequisitionID(String prRequisitionID) {
        this.prRequisitionID = prRequisitionID;
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
}
