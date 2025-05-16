package models.Datas;

import models.DTO.PODataDTO;
import models.DTO.POItemDTO;
import models.ModelInitializable;
import models.Users.User;

import java.util.HashMap;
import java.util.List;

public class PurchaseOrderItem implements ModelInitializable {
    private String poItemID;
    private String poID;
    private String itemID;
    private int quantity;

    public PurchaseOrderItem(){

    }

    public PurchaseOrderItem(String poItemID, String poID, String itemID, int quantity) {
        this.poItemID = poItemID;
        this.poID = poID;
        this.itemID = itemID;
        this.quantity = quantity;
    }

    public String getPoItemID() {
        return poItemID;
    }

    public void setPoItemID(String poItemID) {
        this.poItemID = poItemID;
    }

    public String getPoID() {
        return poID;
    }

    public void setPoRequisitionID(String poID) {
        this.poID = poID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public POItemDTO getPOItemDTO(PurchaseOrderItem poItem, Item item){
        return new POItemDTO(
                poItem.getItemID(),
                poItem.getPoID(),
                item.getItemName(),
                poItem.getQuantity(),
                item.getUnitPrice()
        );
    }

    @Override
    public void initialize(HashMap<String, String> data) {
        this.poItemID = data.get("poItemID");
        this.poID = data.get("poID");
        this.itemID = data.get("itemID");
        this.quantity = Integer.parseInt(data.get("quantity"));
    }
}