package models.Datas;

import controllers.EditPRPOController;
import models.ModelInitializable;
import models.Utils.QueryBuilder;

import java.time.format.DateTimeFormatter;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Item implements ModelInitializable, EditPRPOController.ItemRow {
	private String itemID;
	private String itemName;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private int alertSetting;
	private int quantity;
	private double unitPrice;
	private String supplierID;

	public Item() {

	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.itemID = data.get("itemID");
		this.itemName = data.get("itemName");
		this.description = data.get("description");
		this.createdAt = formatDateTime(data.get("createdAt"));
		this.updatedAt = formatDateTime(data.get("updatedAt"));
		this.alertSetting = Integer.parseInt(data.get("alertSetting"));
		this.quantity = Integer.parseInt(data.get("quantity"));
		this.unitPrice = Double.parseDouble(data.get("unitPrice"));
		this.supplierID = data.get("supplierID");

	}

	public Item(String itemID, String itemName, LocalDateTime createdAt, LocalDateTime updatedAt, int alertSetting, int quantity) {
		this.itemID = itemID;
		this.itemName = itemName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.alertSetting = alertSetting;
		this.quantity = quantity;
	}

	public Item(String itemID, String itemName, LocalDateTime createdAt, LocalDateTime updatedAt, int alertSetting, int quantity, double unitPrice, String supplierID) {
		this.itemID = itemID;
		this.itemName = itemName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.alertSetting = alertSetting;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.supplierID = supplierID;
	}

	public Item(String itemID, String itemName, int quantity){
		this.itemID = itemID;
		this.itemName = itemName;
		this.quantity = quantity;
	}

    public Item(String itemID, String itemName, double unitPrice) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
    }

	public Item(String[] data) {
		itemID = data[0];
		itemName = data[1];
		description = data[2];
		createdAt = formatDateTime(data[3]);
		updatedAt = formatDateTime(data[4]);
		alertSetting = Integer.parseInt(data[5]);
		quantity = Integer.parseInt(data[6]);
		unitPrice = Double.parseDouble(data[7]);
		supplierID = data[8];
	}

    public String getItemID() {
        return itemID;
    }

	public String getItemName() {
		return itemName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public int getAlertSetting() {
		return alertSetting;
	}

	public int getQuantity() {
		return quantity;
	}

    @Override
    public String toString() {
        return itemID + " - " + itemName;
    }


    public static HashMap<Item, String> getItems(boolean withSupplier) throws Exception {
		HashMap<Item, String> itemSupplierMap = new HashMap<>();

		QueryBuilder<Item> itemQb = new QueryBuilder<>(Item.class);
		String[] itemColumns = new String[]{"itemID", "itemName", "createdAt", "updatedAt", "alertSetting", "quantity", "supplierID"};
		ArrayList<HashMap<String, String>> items = itemQb.select(itemColumns).from("db/Item").get();

		for (HashMap<String, String> itemData : items) {
			String supplierID = itemData.get("supplierID");

			ArrayList<HashMap<String, String>> supplierResult = Supplier.getSupplierNameById(supplierID);
			String companyName = "";

			if (!supplierResult.isEmpty()) {
				companyName = supplierResult.get(0).get("supplierName");
			}

			Item item = new Item(
					itemData.get("itemID"),
					itemData.get("itemName"),
					formatDateTime(itemData.get("createdAt")),
					formatDateTime(itemData.get("updatedAt")),
					Integer.parseInt(itemData.get("alertSetting")),
					Integer.parseInt(itemData.get("quantity"))
			);

			itemSupplierMap.put(item, companyName);
		}

		return itemSupplierMap;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = LocalDateTime.parse(createdAt);
	}

	public static HashMap<String, Item> getItemMap() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		HashMap<String, Item> itemMap = new HashMap<>();

		// Store all items into an array list
		QueryBuilder<Item> itemQb = new QueryBuilder<>(Item.class).select().from("db/Item");
		List<Item> itemList = itemQb.getAsObjects();
		// Add all items into HashMap before returning it
		for(Item item  : itemList){
			itemMap.put(item.getItemID(), item);
		}
		return itemMap;
	}

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getSupplierID() {
        return supplierID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setAlertSetting(int alertSetting) {
        this.alertSetting = alertSetting;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID;
    }

    public static ArrayList<HashMap<String, String>> getItems() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        QueryBuilder<Item> qb = new QueryBuilder(Item.class);
        return qb.select().from("db/Item").get();
    }

    public static HashMap<Item, String> getItems(boolean withSupplier) throws Exception {
        HashMap<Item, String> itemSupplierMap = new HashMap<>();

        QueryBuilder<Item> itemQb = new QueryBuilder<>(Item.class);
        String[] itemColumns = new String[]{"itemID", "itemName", "createdAt", "updatedAt", "alertSetting", "quantity", "supplierID"};
        ArrayList<HashMap<String, String>> items = itemQb.select(itemColumns).from("db/Item").get();

        for (HashMap<String, String> itemData : items) {
            String supplierID = itemData.get("supplierID");

            ArrayList<HashMap<String, String>> supplierResult = Supplier.getSupplierNameById(supplierID);
            String companyName = "";

            if (!supplierResult.isEmpty()) {
                companyName = supplierResult.get(0).get("supplierName");
            }

            Item item = new Item(
                    itemData.get("itemID"),
                    itemData.get("itemName"),
                    formatDateTime(itemData.get("createdAt")),
                    formatDateTime(itemData.get("updatedAt")),
                    Integer.parseInt(itemData.get("alertSetting")),
                    Integer.parseInt(itemData.get("quantity"))
            );

            itemSupplierMap.put(item, companyName);
        }

        return itemSupplierMap;
    }

    public static LocalDateTime formatDateTime(String strDatetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(strDatetime, formatter);
    }

    public static String stringifyDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }
}


