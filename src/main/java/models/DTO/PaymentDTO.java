package models.DTO;

public class PaymentDTO {
    private String PO_ID;
    private String itemName;
    private double amount;
    private String item;
    private int quantity;

    public PaymentDTO(String PO_ID, String itemName, double amount, String item, int quantity) {
        this.PO_ID = PO_ID;
        this.itemName = itemName;
        this.amount = amount;
        this.item = item;
        this.quantity = quantity;
    }

    public String getPO_ID() {
        return PO_ID;
    }

    public double getAmount() {
        return amount;
    }

    public String getItem() {
        return item;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setPO_ID(String PO_ID) {
        this.PO_ID = PO_ID;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setItem(String item) {
        this.item = item;
    }
}
