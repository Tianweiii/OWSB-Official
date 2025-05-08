package models.Datas;

import models.DTO.InventoryUpdateRequestDTO;
import models.ModelInitializable;
import models.Users.User;

import java.util.HashMap;

public class InventoryUpdateRequest implements ModelInitializable {
    private String inventoryUpdateRequestID;
    private String itemID;
    private String userID;
    private int quantity;
    private String status;

    public InventoryUpdateRequest() {

    }

    public InventoryUpdateRequest(String inventoryUpdateRequestID, String itemID, String userID, int quantity, String status) {
        this.inventoryUpdateRequestID = inventoryUpdateRequestID;
        this.itemID = itemID;
        this.userID = userID;
        this.quantity = quantity;
        this.status = status;
    }

    public String getInventoryUpdateRequestID() {
        return inventoryUpdateRequestID;
    }

    public String getItemID() {
        return itemID;
    }

    public String getUserID() {
        return userID;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setInventoryUpdateRequestID(String inventoryUpdateRequestID) {
        this.inventoryUpdateRequestID = inventoryUpdateRequestID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void initialize(HashMap<String, String> data) {
        this.inventoryUpdateRequestID = data.get("inventoryUpdateRequestID");
        this.itemID = data.get("itemID");
        this.userID = data.get("userID");
        this.quantity = Integer.parseInt(data.get("quantity"));
        this.status = data.get("status");
    }

    public InventoryUpdateRequestDTO toURDTO(User user, Item item) {
        return new InventoryUpdateRequestDTO(
                this.inventoryUpdateRequestID,
                this.itemID,
                item != null ? item.getItemName() : "NA Item",
                this.quantity,
                this.userID,
                user != null ? user.getName() : "NA User",
                this.status
        );
    }

}
