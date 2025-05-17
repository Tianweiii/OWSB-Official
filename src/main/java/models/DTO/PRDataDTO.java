package models.DTO;

import models.Datas.PurchaseOrder;
import models.Datas.PurchaseOrderItem;
import models.Datas.PurchaseRequisition;
import models.Datas.PurchaseRequisitionItem;
import models.Users.User;
import models.Utils.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PRDataDTO {
    private String userName;
    private String userID;
    private String prID;
    private String status;
    private String createdDate;
    private String receivedByDate;
    private List<PRItemDTO> prItemList;
    private int totalQuantity;

    public PRDataDTO(String userName,
                     String userID,
                     String prID,
                     String status,
                     String createdDate,
                     String receivedByDate,
                     List<PRItemDTO> prItemList,
                     int totalQuantity
    ){
        this.userName = userName;
        this.userID = userID;
        this.prID = prID;
        this.status = status;
        this.createdDate = createdDate;
        this.receivedByDate = receivedByDate;
        this.prItemList = prItemList;
        this.totalQuantity = totalQuantity;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPrID() {
        return prID;
    }

    public void setPrID(String prID) {
        this.prID = prID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getReceivedByDate() {
        return receivedByDate;
    }

    public void setReceivedByDate(String receivedByDate) {
        this.receivedByDate = receivedByDate;
    }

    public List<PRItemDTO> getPrItemList() {
        return prItemList;
    }

    public void setPrItemList(List<PRItemDTO> prItemList) {
        this.prItemList = prItemList;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public static List<PRDataDTO> getPRDataDTOs(String filter_status) throws  Exception{
        List<PRDataDTO> prDataDTOs = new ArrayList<>();

        List<PurchaseRequisition> prList;
        List<User> userList = new QueryBuilder<>(User.class).select().from("db/User.txt").getAsObjects();

        if(filter_status.equalsIgnoreCase("all")){
            prList = new QueryBuilder<>(PurchaseRequisition.class).select().from("db/PurchaseRequisition.txt").getAsObjects();
        } else{
            prList = new QueryBuilder<>(PurchaseRequisition.class).select().from("db/PurchaseRequisition.txt").where("PRStatus", "=", filter_status).getAsObjects();
        }

        List<PRItemDTO> prItemDTOList = PRItemDTO.getPRItemDTOs();

        HashMap<String, User> userMap = new HashMap<>();
        HashMap<String, List<PRItemDTO>> prItemDTOMap = new HashMap<>();
        HashMap<String, Integer> totalQuantityMap = new HashMap<>();

        for (User user : userList) {
            userMap.put(user.getId(), user);
        }

        for(PRItemDTO prItemDTO : prItemDTOList){
            String prID = prItemDTO.getPrID();
            if (prItemDTOMap.containsKey(prID)) {
                prItemDTOMap.get(prID).add(prItemDTO); // if already have a record, just add the PRItem to the existing list
                totalQuantityMap.put(prID, totalQuantityMap.get(prID) + prItemDTO.getItemQuantity()); // Add the quantity on top of the prev total qty
            } else {
                List<PRItemDTO> newList = new ArrayList<>();
                newList.add(prItemDTO); // add the first item
                prItemDTOMap.put(prID, newList); // put into the map
                totalQuantityMap.put(prID, prItemDTO.getItemQuantity());
            }
        }

        for(PurchaseRequisition pr : prList){
            User user = userMap.get(pr.getUserID());
            prDataDTOs.add(
                pr.getPRDataDTO(
                    user,
                    prItemDTOMap.get(pr.getPrRequisitionID()),
                    totalQuantityMap.get(pr.getPrRequisitionID())
                )
            );
        }

        return prDataDTOs;
    }
}
