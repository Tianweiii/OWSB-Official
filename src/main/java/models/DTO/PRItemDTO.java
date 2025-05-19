package models.DTO;

public class PRItemDTO {

    private String supplierID;
    private String itemTitle;
    private int quantity;
    private double unitPrice;
    private double total;

    public PRItemDTO(String supplierID, String itemTitle, int quantity, double unitPrice, double total) {
        this.supplierID = supplierID;
        this.itemTitle = itemTitle;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    public String getSupplierID() {
        return supplierID;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotal() {
        return total;
    }
}
