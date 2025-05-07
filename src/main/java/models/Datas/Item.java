package models.Datas;

import models.ModelInitializable;
import models.Utils.QueryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Item implements ModelInitializable {
    private String itemID;
    private String itemName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int alertSetting;
    private int quantity;
    private double unitPrice;
    private int supplierID;

    public Item() {
    }

    public Item(String itemID, String itemName, LocalDateTime createdAt, LocalDateTime updatedAt, int alertSetting, int quantity) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.alertSetting = alertSetting;
        this.quantity = quantity;
    }

    public Item(String itemID, String itemName, LocalDateTime createdAt, LocalDateTime updatedAt, int alertSetting, int quantity, double unitPrice, int supplierID) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.alertSetting = alertSetting;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.supplierID = supplierID;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getSupplierID() {
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

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemID=" + itemID +
                ", itemName='" + itemName + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", alertSetting='" + alertSetting + '\'' +
                ", quantity=" + quantity +
                ", supplierID=" + supplierID +
                '}';
    }

    @Override
    public void initialize(HashMap<String, String> data) {
        this.itemID = data.get("itemID");
        this.itemName = data.get("itemName");
        this.createdAt = formatDateTime(data.get("createdAt"));
        this.updatedAt = formatDateTime(data.get("updatedAt"));
        this.alertSetting = Integer.parseInt(data.get("alertSetting"));
        this.quantity = Integer.parseInt(data.get("quantity"));
        this.unitPrice = Double.parseDouble(data.get("unitPrice"));
        this.supplierID = Integer.parseInt(data.get("supplierID"));

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
            int supplierID = Integer.parseInt(itemData.get("supplierID"));

            ArrayList<HashMap<String, String>> supplierResult = Supplier.getSupplierNameById(supplierID);
            String companyName = "";

            if (!supplierResult.isEmpty()) {
                companyName = supplierResult.get(0).get("companyName");
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
