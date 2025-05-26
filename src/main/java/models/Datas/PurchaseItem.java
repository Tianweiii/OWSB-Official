package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseItem implements ModelInitializable {
    private String prItemID;
    private String prOrderID;
    private String itemID;
    private int quantity;

    public PurchaseItem(String prItemID, String prOrderID, String itemID, int quantity) {
        this.prItemID = prItemID;
        this.prOrderID = prOrderID;
        this.itemID = itemID;
        this.quantity = quantity;
    }


    @Override
    public void initialize(HashMap<String, String> data) {

    }
}
