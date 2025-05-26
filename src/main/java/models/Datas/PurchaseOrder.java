package models.Datas;

import javafx.beans.property.SimpleStringProperty;
import models.DTO.PODataDTO;
import models.DTO.POItemDTO;
import models.DTO.PaymentDTO;
import models.ModelInitializable;
import models.Users.User;
import models.Utils.FileIO;
import models.Utils.QueryBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PurchaseOrder implements ModelInitializable {
	private String poID;
	private String prID;
	private String userID;
	private String title;
	private String payableAmount;
	private String POStatus;

	public PurchaseOrder(){

	}

	public PurchaseOrder(String[] data) {
		this.poID = data[0];
		this.prID = data[1];
		this.userID = data[2];
		this.title = data[3];
		this.payableAmount = String.valueOf(Double.parseDouble(data[4]));
		this.POStatus = data[5];
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

	public String getPoID() {
		return this.poID;
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
	@Override
	public String toString() {
		return "PurchaseOrder{" +
				"poID='" + poID + '\'' +
				", prID='" + prID + '\'' +
				", userID='" + userID + '\'' +
				", title='" + title + '\'' +
				", payableAmount=" + payableAmount +
				", status='" + POStatus + '\'' +
				'}';
	}

	public ArrayList<PurchaseOrderItem> getPurchaseOrderItems() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		QueryBuilder<PurchaseOrderItem> qb = new QueryBuilder<>(PurchaseOrderItem.class);
		return qb.select().from("db/PurchaseOrderItem").where("poID", "=", poID).getAsObjects();
	}

	public Map<String, List<PaymentDTO>> getPurchaseItemList() throws IOException, ReflectiveOperationException {
		Map<String, List<PaymentDTO>> payments = new HashMap<>();
		// get item ids and quantity
		HashMap<String, String> itemAndQuantity = FileIO.filterIDToHashMap("PurchaseOrderItem", 1, 2, 3, poID);
		Set<String> itemIDs = itemAndQuantity.keySet();

		// getting items of PO
		ArrayList<Item> items = FileIO.getIDsAsObjects(Item.class, "Item", itemIDs);

		for (Item i : items) {
			String supplierID = i.getSupplierID();
			String itemName = FileIO.getXFromID("Item", 0, 1, i.getItemID());
			String itemID = i.getItemID();
			double unitPrice = i.getUnitPrice();
			int quantity = Integer.parseInt(itemAndQuantity.get(itemID));
			double amount = quantity * unitPrice;

			PaymentDTO payment = new PaymentDTO(poID, itemName, amount, itemID, quantity, unitPrice);

			// Group by supplier ID
			payments.computeIfAbsent(supplierID, k -> new ArrayList<>()).add(payment);
		}

		return payments;
	}

	//	public HashMap<String, String> get
}
