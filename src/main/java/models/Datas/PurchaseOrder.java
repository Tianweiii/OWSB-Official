package models.Datas;

import models.DTO.PaymentDTO;
import models.ModelInitializable;
import models.Utils.FileIO;
import models.Utils.QueryBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PurchaseOrder implements ModelInitializable {
	private String PO_ID;
	private String PR_ID;
	private String userID;
	private String title;
	private double payableAmount;
	private String status;



	@Override
	public void initialize(HashMap<String, String> data) {
		PO_ID = data.get("PO_ID");
		PR_ID = data.get("PR_ID");
		userID = data.get("userID");
		title = data.get("title");
		payableAmount = Double.parseDouble(data.get("payableAmount"));
		status = data.get("status");
	}

	public String getPO_ID() {
		return PO_ID;
	}

	public String getPR_ID() {
		return PR_ID;
	}

	public String getUserID() {
		return userID;
	}

	public String getTitle() {
		return title;
	}

	public double getPayableAmount() {
		return payableAmount;
	}

	public String getStatus() {
		return status;
	}

	public PurchaseOrder() {}

	public PurchaseOrder(String[] data) {
		PO_ID = data[0];
		PR_ID = data[1];
		userID = data[2];
		title = data[3];
		payableAmount = Double.parseDouble(data[4]);
		status = data[5];
	}

	@Override
	public String toString() {
		return "PurchaseOrder{" +
				"PO_ID='" + PO_ID + '\'' +
				", PR_ID='" + PR_ID + '\'' +
				", userID='" + userID + '\'' +
				", title='" + title + '\'' +
				", payableAmount=" + payableAmount +
				", status='" + status + '\'' +
				'}';
	}

	public ArrayList<PurchaseOrderItem> getPurchaseOrderItems() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		QueryBuilder<PurchaseOrderItem> qb = new QueryBuilder<>(PurchaseOrderItem.class);
		return qb.select().from("db/PurchaseOrderItem").where("PO_ID", "=", PO_ID).getAsObjects();
	}

	public Map<String, List<PaymentDTO>> getPurchaseItemList() throws IOException, ReflectiveOperationException {
		Map<String, List<PaymentDTO>> payments = new HashMap<>();
		// get item ids and quantity
		HashMap<String, String> itemAndQuantity = FileIO.filterIDToHashMap("PurchaseOrderItem", 1, 2, 3, PO_ID);
		Set<String> itemIDs = itemAndQuantity.keySet();

		// getting items of PO
		ArrayList<Item> items = FileIO.getIDsAsObjects(Item.class, "Item", itemIDs);

		for (Item i : items) {
			String supplierID = i.getSupplierID();
			String itemName = FileIO.getXFromID("Item", 0, 1, i.getItemID());
			String itemID = i.getItemID();
			int quantity = Integer.parseInt(itemAndQuantity.get(itemID));
			double amount = quantity * i.getUnitPrice();

			PaymentDTO payment = new PaymentDTO(PO_ID, itemName, amount, itemID, quantity);

			// Group by supplier ID
			payments.computeIfAbsent(supplierID, k -> new ArrayList<>()).add(payment);
		}

		return payments;
	}

//	public HashMap<String, String> get
}
