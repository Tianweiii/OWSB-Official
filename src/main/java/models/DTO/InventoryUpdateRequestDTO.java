package models.DTO;

import models.Datas.InventoryUpdateRequest;
import models.Datas.Item;
import models.Users.User;
import models.Utils.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryUpdateRequestDTO {
    private String inventoryUpdateRequestID;
    private String itemID;
    private String itemName;
    private int quantity;
    private String userID;
    private String userName;
    private String status;

    public InventoryUpdateRequestDTO(String inventoryUpdateRequestID, String itemID, String itemName,
                                     int quantity, String userID, String userName, String status) {
        this.inventoryUpdateRequestID = inventoryUpdateRequestID;
        this.itemID = itemID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.userID = userID;
        this.userName = userName;
        this.status = status;
    }

    public String getInventoryUpdateRequestID() {
        return inventoryUpdateRequestID;
    }

    public String getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
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

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static List<InventoryUpdateRequestDTO> getInventoryUpdateRequestDTOs() throws Exception {
        List<InventoryUpdateRequestDTO> dtoURList = new ArrayList<>();

        QueryBuilder<InventoryUpdateRequest> qbRequest = new QueryBuilder<>(InventoryUpdateRequest.class);
        List<InventoryUpdateRequest> requests = qbRequest.select().from("db/InventoryUpdateRequest.txt").getAsObjects();

        QueryBuilder<User> qbUser = new QueryBuilder<>(User.class);
        List<User> users = qbUser.select().from("db/User.txt").getAsObjects();

        QueryBuilder<Item> qbItem = new QueryBuilder<>(Item.class);
        List<Item> items = qbItem.select().from("db/Item.txt").getAsObjects();

        HashMap<String, User> userMapping = new HashMap<>();
        for (User user : users) {
            userMapping.put(user.getId(), user);
        }

        HashMap<String, Item> itemMapping = new HashMap<>();
        for (Item item : items) {
            itemMapping.put(item.getItemID(), item);
        }

        for (InventoryUpdateRequest request : requests) {
            User user = userMapping.get(request.getUserID());
            Item item = itemMapping.get(request.getItemID());

            InventoryUpdateRequestDTO dto = request.toURDTO(user, item);

            dtoURList.add(dto);
        }

        return dtoURList;
    }

}
