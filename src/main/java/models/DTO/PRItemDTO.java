package models.DTO;

import controllers.EditPRPOController;
import models.Datas.Item;
import models.Datas.PurchaseOrderItem;
import models.Datas.PurchaseRequisitionItem;
import models.Utils.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PRItemDTO implements EditPRPOController.ItemRow  {
    private String itemID;
    private String prID;
    private String itemName;
    private int itemQuantity;
    private double unitPrice;
    private double total;

    public PRItemDTO(String itemID, String prID, String itemName, int itemQuantity, double unitPrice) {
        this.itemID = itemID;
        this.prID = prID;
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    @Override
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getPrID() {
        return prID;
    }

    public void setPrID(String prID) {
        this.prID = prID;
    }

    @Override
    public String getItemName() {
        return itemName;
    }

    @Override
    public int getQuantity() {
        return itemQuantity;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public static List<PRItemDTO> getPRItemDTOs() throws  Exception{
        List<PRItemDTO> prItemDTOs = new ArrayList<>();
        QueryBuilder<Item> itemQB = new QueryBuilder<>(Item.class).select().from("db/Item.txt");
        List<Item> itemList = itemQB.getAsObjects();

        QueryBuilder<PurchaseRequisitionItem> prItemQB = new QueryBuilder<>(PurchaseRequisitionItem.class).select().from("db/PurchaseRequisitionItem.txt");
        List<PurchaseRequisitionItem> prItemList = prItemQB.getAsObjects();

        HashMap<String, Item> itemMap = new HashMap<>();
        for(Item item : itemList){
            itemMap.put(item.getItemID(), item);
        }

        for(PurchaseRequisitionItem prItem : prItemList) {
            Item item = itemMap.get(prItem.getItemID());
            prItemDTOs.add(prItem.getPRItemDTO(item));
        }

        return prItemDTOs;
    }

    public double getTotal() {
        return total;
    }
}
