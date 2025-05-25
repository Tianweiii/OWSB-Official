package models.Datas;

import models.DTO.PODataDTO;
import models.DTO.POItemDTO;
import models.DTO.PRDataDTO;
import models.DTO.PRItemDTO;
import models.DTO.PRItemDTO;
import models.DTO.PaymentDTO;
import models.ModelInitializable;
import models.Users.User;
import models.Utils.FileIO;
import models.Utils.QueryBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PurchaseRequisition implements ModelInitializable {
	private String prRequisitionID;
	private String userID;
	private String PRStatus;
	private LocalDate createdDate;
	private LocalDate receivedByDate;

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
				this.getCreatedDate().toString(),
				this.getReceivedByDate(),
				itemList,
				totalQuantity
		);
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.prRequisitionID = data.get("prRequisitionID");
		this.userID = data.get("userID");
		this.PRStatus = data.get("PRStatus");
		this.createdDate = parseDate(data.get("createdDate"));
		this.receivedByDate = parseDate(data.get("receivedByDate"));
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

			PRItemDTO item = new PRItemDTO(itemID, "PR1", itemTitle, quantity, unitPrice);

			itemList.add(item);
		}

		return itemList;
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
