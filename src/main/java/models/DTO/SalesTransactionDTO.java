package models.DTO;

public class SalesTransactionDTO {

    private String transactionID;
    private String userID;
    private String itemName;
    private double salesAmount;
    private String createdDate;

    public SalesTransactionDTO(String transactionID, String itemName,  double salesAmount, String createdDate, String userID) {
        this.transactionID = transactionID;
        this.userID = userID;
        this.itemName = itemName;
        this.salesAmount = salesAmount;
        this.createdDate = createdDate;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getUserID() {
        return userID;
    }

    public String getItemName() {
        return itemName;
    }

    public double getSalesAmount() {
        return salesAmount;
    }

    public String getCreatedDate() {
        return createdDate;
    }
}
