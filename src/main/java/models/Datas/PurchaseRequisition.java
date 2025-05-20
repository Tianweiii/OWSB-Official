package models.Datas;

import models.DTO.PRItemDTO;
import models.DTO.PaymentDTO;
import models.ModelInitializable;
import models.Utils.FileIO;
import models.Utils.QueryBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PurchaseRequisition implements ModelInitializable {
	private String prRequisitionID;
	private String userID;
	private String PRStatus;
	private String createdDate;
	private String receivedByDate;

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prRequisitionID = data.get("prRequisitionID");
		this.userID = data.get("userID");
		this.PRStatus = data.get("PRStatus");
		this.createdDate = data.get("createdDate");
		this.receivedByDate = data.get("receivedByDate");
	}

	public PurchaseRequisition() {}

	public String getPrRequisitionID() {
		return prRequisitionID;
	}

	public String getReceivedByDate() {
		return receivedByDate;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public String getUserID() {
		return userID;
	}

	public String getPRStatus() {
		return PRStatus;
	}

//	public ArrayList<PRItemDTO> getPurchaseRequisitionItems() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
//		QueryBuilder<PurchaseRequisitionItem> qb = new QueryBuilder<>(PurchaseRequisitionItem.class);
//		ArrayList<PurchaseRequisitionItem> items = qb.select().from("db/PurchaseRequisitionItem").where("pr_requisition_id", "=", prRequisitionID).getAsObjects();
//	}

	public List<PRItemDTO> getPurchaseItemList() throws IOException, ReflectiveOperationException {
		List<PRItemDTO> itemList =  new ArrayList<>();

		HashMap<String, String> itemAndQuantity = FileIO.filterIDToHashMap("PurchaseRequisitionItem", 1, 2, 3, prRequisitionID);
		Set<String> itemIDs = itemAndQuantity.keySet();

		ArrayList<Item> items = FileIO.getIDsAsObjects(Item.class, "Item", itemIDs);

		for (Item i : items) {
			String supplierID = i.getSupplierID();
			String itemID = i.getItemID();
			String itemTitle = i.getItemName();
			int quantity = Integer.parseInt(itemAndQuantity.get(itemID));
			double unitPrice = i.getUnitPrice();
			double amount = quantity * unitPrice;

			PRItemDTO item = new PRItemDTO(supplierID, itemTitle, quantity, unitPrice, amount);

			itemList.add(item);
		}

		return itemList;
	}
}
