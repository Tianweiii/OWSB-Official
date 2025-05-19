package models.Datas;

import models.DTO.POItemDTO;
import models.DTO.PRItemDTO;
import models.ModelInitializable;

import java.util.HashMap;


public class PurchaseRequisitionItem implements ModelInitializable {
	private String prItemID;
	private String prRequisitionID;
	private String itemID;
	private int quantity;

	public PurchaseRequisitionItem(){

	}

	public PurchaseRequisitionItem(String prItemID, int quantity, String prRequisitionID, String itemID) {
		this.prItemID = prItemID;
		this.quantity = quantity;
		this.prRequisitionID = prRequisitionID;
		this.itemID = itemID;
	}

	public String getPrItemID() {
		return prItemID;
	}

	public void setPrItemID(String prItemID) {
		this.prItemID = prItemID;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getPrRequisitionID() {
		return prRequisitionID;
	}

	public void setPrRequisitionID(String prRequisitionID) {
		this.prRequisitionID = prRequisitionID;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public PRItemDTO getPRItemDTO(Item item){
		return new PRItemDTO(
				this.getItemID(),
				this.getPrRequisitionID(),
				item.getItemName(),
				this.getQuantity(),
				item.getUnitPrice()
		);
	}

	public PurchaseRequisitionItem() {}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prItemID = data.get("prItemID");
		this.prRequisitionID = data.get("prRequisitionID");
		this.itemID = data.get("itemID");
		this.quantity = Integer.parseInt(data.get("quantity"));
	}

	public String getPrItemID() {
		return this.prItemID;
	}
	public int getQuantity() {
		return this.quantity;
	}

	public String getPrRequisitionID() {
		return this.prRequisitionID;
	}
	public String getItemID() {
		return this.itemID;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	@Override
	public String toString() {
		return "PurchaseRequisitionItem{" +
				"prItemID='" + prItemID + '\'' +
				", quantity=" + quantity +
				", prRequisitionID='" + prRequisitionID + '\'' +
				", itemID='" + itemID + '\'' +
				'}';
	}
}
