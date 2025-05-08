package models.DTO;

public class PaymentDTO {
    private String PO_ID;
    private double amount;
    private String items;

    public PaymentDTO(String PO_ID, double amount, String items) {
        this.PO_ID = PO_ID;
        this.amount = amount;
        this.items = items;
    }

    public String getPO_ID() {
        return PO_ID;
    }

    public double getAmount() {
        return amount;
    }

    public String getItems() {
        return items;
    }

    public void setPO_ID(String PO_ID) {
        this.PO_ID = PO_ID;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setItems(String items) {
        this.items = items;
    }
}
