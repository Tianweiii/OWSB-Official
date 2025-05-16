package models.DTO;

import controllers.EditPRPOController;
import models.Datas.Item;
import models.Datas.PurchaseOrder;
import models.Datas.PurchaseOrderItem;
import models.Datas.PurchaseRequisition;
import models.Users.User;
import models.Utils.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class POItemDTO implements EditPRPOController.ItemRow {
    private String itemID;
    private String poID;
    private String itemName;
    private int itemQuantity;
    private double unitPrice;

    public POItemDTO(String itemID, String poID, String itemName, int itemQuantity, double unitPrice) {
        this.itemID = itemID;
        this.poID = poID;
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.unitPrice = unitPrice;
    }

    // ItemRow implementation
    @Override
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getPoID() {
        return poID;
    }

    public void setPoID(String poID) {
        this.poID = poID;
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

    public static List<POItemDTO> getPOItemDTOs() throws  Exception{
        List<POItemDTO> poItemDTOs = new ArrayList<>();
        List<Item> itemList = new QueryBuilder<>(Item.class).select().from("db/Item.txt").getAsObjects();
        List<PurchaseOrderItem> poItemList = new QueryBuilder<>(PurchaseOrderItem.class).select().from("db/PurchaseOrderItem.txt").getAsObjects();

        HashMap<String, Item> itemMap = new HashMap<>();
        for(Item item : itemList){
            itemMap.put(item.getItemID(), item);
        }

        for(PurchaseOrderItem poItem : poItemList) {
            Item item = itemMap.get(poItem.getItemID());
            poItemDTOs.add(poItem.getPOItemDTO(poItem, item));
        }

        return poItemDTOs;
    }
}
