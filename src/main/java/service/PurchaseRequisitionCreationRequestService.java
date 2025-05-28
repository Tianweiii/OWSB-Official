package service;

import models.Datas.Item;
import models.Datas.PurchaseRequisitionCreationRequest;
import models.Datas.PurchaseRequisitionItem;
import models.Utils.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PurchaseRequisitionCreationRequestService extends Service<PurchaseRequisitionCreationRequest> {
	private static final String DATA_FILE = "db/PurchaseRequisitionCreationRequest.txt";
	@Override
	public List<PurchaseRequisitionCreationRequest> getAll() {
		try {
			QueryBuilder<PurchaseRequisitionCreationRequest> qb = new QueryBuilder<>(PurchaseRequisitionCreationRequest.class);
			return qb.select().from(DATA_FILE).getAsObjects();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return Collections.emptyList();
		}
	}

	@Override
	public boolean add(String... varargs) {
		/*
		  [0] itemID
		  [1] quantity
		  [2] alertSetting
		  [3] minimumPurchaseQuantity
		  [4] status
		  */
		try {
			ArrayList<String[]> data = getRequiredData();
			if (data.isEmpty()) {
				return false;
			}

			QueryBuilder<PurchaseRequisitionCreationRequest> qb = new QueryBuilder<>(PurchaseRequisitionCreationRequest.class);
			for (String[] row : data) {
				if (!this.doesExist(row[0])) {
					qb.target(DATA_FILE).values(row).create();
				}
			}
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public boolean doesExist(String id) {
		try {
			QueryBuilder<PurchaseRequisitionCreationRequest> qb = new QueryBuilder<>(PurchaseRequisitionCreationRequest.class);
			return qb.select().from(DATA_FILE).getAsObjects().stream().anyMatch(pr -> pr.getItemID().equals(id));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public boolean delete(String itemID, int addItemQuantity) {
		try {
			QueryBuilder<PurchaseRequisitionCreationRequest> qb = new QueryBuilder<>(PurchaseRequisitionCreationRequest.class);
			QueryBuilder<Item> itemQb = new QueryBuilder<>(Item.class);
			Item item = itemQb.select().from("db/Item").where("itemID", "=", itemID).getAsObjects().get(0);

			if (item.getAlertSetting() - item.getQuantity() > addItemQuantity) {
				PurchaseRequisitionCreationRequest pr = qb.select().from(DATA_FILE).where("itemID", "=", itemID).getAsObjects().get(0);
				HashMap<String, String> data = new HashMap<>();
				data.put("minimumPurchaseQuantity", String.valueOf(pr.getMinimumPurchaseQuantity() - addItemQuantity));
				if (pr.getMinimumPurchaseQuantity() - addItemQuantity > 0) {
					return qb.target(DATA_FILE).update(pr.getPurchaseRequisitionCreationRequestID(), data);
				}
			}

			return qb.target(DATA_FILE).deleteAnyMatching("itemID", "=", itemID);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public boolean deleteMany(String... ids) {
		try {
			QueryBuilder<PurchaseRequisitionCreationRequest> qb = new QueryBuilder<>(PurchaseRequisitionCreationRequest.class);
			return qb.target(DATA_FILE).deleteMany(ids);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public List<PurchaseRequisitionItem> hasExistingPR(Item item) {
		try {
			QueryBuilder<PurchaseRequisitionItem> qb = new QueryBuilder<>(PurchaseRequisitionItem.class);
			List<PurchaseRequisitionItem> items = qb
					.select()
					.from("db/PurchaseRequisitionItem.txt")
					.where("itemID", "=", item.getItemID())
					.getAsObjects();

			if (!items.isEmpty()) {
				return items;
			}
			return Collections.emptyList();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return Collections.emptyList();
		}
	}

	public ArrayList<String[]> getRequiredData() {
		ArrayList<String[]>	data = new ArrayList<>();
		try {
				QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
				ArrayList<Item> items = qb
					.select()
					.from("db/Item.txt")
					.getAsObjects();

				for (Item item : items) {
					List<PurchaseRequisitionItem> prItems = hasExistingPR(item);
					int currentOrderedQuantity = 0;
					if (!prItems.isEmpty()) {
						currentOrderedQuantity = prItems.stream().mapToInt(PurchaseRequisitionItem::getQuantity).sum();
					}
					if (item.getQuantity() < item.getAlertSetting()) {
						String quantity = Integer.toString(item.getQuantity());
						String alertSetting = Integer.toString(item.getAlertSetting());
						int minimumPurchaseQuantity =item.getAlertSetting() - item.getQuantity() - currentOrderedQuantity;

						if (minimumPurchaseQuantity > 0) {
							String[] prRequest = new String[]{
									item.getItemID(),
									quantity,
									alertSetting,
									Integer.toString(minimumPurchaseQuantity),
							};

							data.add(prRequest);
						}
					}
				}

				return data;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public void restore() {
		try {
			QueryBuilder<PurchaseRequisitionCreationRequest> qb = new QueryBuilder<>(PurchaseRequisitionCreationRequest.class);

			//Delete all PRRequests
			qb.target(DATA_FILE).deleteAnyMatching("quantity", ">", "0");

			// Restore the Requests
			this.add();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}