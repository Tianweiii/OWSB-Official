package models.Datas;

public class PurchaseItem {
    private String purchaseItemID;
    private String purchaseReqID;
    private String itemID;
    private int quantity;

    public PurchaseItem(){

    }

    public PurchaseItem(String purchaseItemID, String purchaseReqID, String itemID, int quantity) {
        this.purchaseItemID = purchaseItemID;
        this.purchaseReqID = purchaseReqID;
        this.itemID = itemID;
        this.quantity = quantity;
    }

    public String getPurchaseItemID() {
        return purchaseItemID;
    }

    public void setPurchaseItemID(String purchaseItemID) {
        this.purchaseItemID = purchaseItemID;
    }

    public String getPurchaseReqID() {
        return purchaseReqID;
    }

    public void setPurchaseReqID(String purchaseReqID) {
        this.purchaseReqID = purchaseReqID;
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
}
