package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PurchaseOrderItem implements ModelInitializable {

    private String POI_ID;
    private String PO_ID;
    private String itemID;
    private int quantity;

    @Override
    public void initialize(HashMap<String, String> data) {
        POI_ID = data.get("POI_ID");
        PO_ID = data.get("PO_ID");
        itemID = data.get("itemID");
        quantity = Integer.parseInt(data.get("quantity"));
    }

    public PurchaseOrderItem(String POI_ID, String PO_ID, String itemID, int quantity) {
        this.POI_ID = POI_ID;
        this.PO_ID = PO_ID;
        this.itemID = itemID;
        this.quantity = quantity;
    }

    public PurchaseOrderItem() {}


}
