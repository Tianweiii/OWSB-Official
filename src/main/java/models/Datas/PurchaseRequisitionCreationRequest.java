package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseRequisitionCreationRequest implements ModelInitializable {
	private String purchaseRequisitionCreationRequestID;
	private String itemID;
	private int quantity;
	private int alertSetting;
	private int minimumPurchaseQuantity;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.purchaseRequisitionCreationRequestID = data.get("purchaseRequisitionCreationRequestID");
		this.itemID = data.get("itemID");
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.alertSetting = Integer.parseInt(data.get("alertSetting"));
		this.minimumPurchaseQuantity = Integer.parseInt(data.get("minimumPurchaseQuantity"));
	}

	public String getPurchaseRequisitionCreationRequestID() {
		return purchaseRequisitionCreationRequestID;
	}

	public void setPurchaseRequisitionCreationRequestID(String purchaseRequisitionCreationRequestID) {
		this.purchaseRequisitionCreationRequestID = purchaseRequisitionCreationRequestID;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getAlertSetting() {
		return alertSetting;
	}

	public void setAlertSetting(int alertSetting) {
		this.alertSetting = alertSetting;
	}

	public int getMinimumPurchaseQuantity() {
		return minimumPurchaseQuantity;
	}

	public void setMinimumPurchaseQuantity(int minimumPurchaseQuantity) {
		this.minimumPurchaseQuantity = minimumPurchaseQuantity;
	}
}
