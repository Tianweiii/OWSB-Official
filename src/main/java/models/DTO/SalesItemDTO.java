package models.DTO;

public class SalesItemDTO {
    private String itemName;
    private int quantity;
    private double amount;

    public SalesItemDTO(String itemName, int quantity, double amount) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.amount = amount;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }
}
