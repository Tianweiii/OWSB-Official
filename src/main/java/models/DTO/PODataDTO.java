package models.DTO;

import models.Datas.PurchaseOrder;
import models.Datas.PurchaseOrderItem;
import models.Datas.PurchaseRequisition;
import models.Users.User;
import models.Utils.QueryBuilder;

import javax.management.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PODataDTO {
    private String userName;
    private String userID;
    private String prID;
    private String poID;
    private String status;
    private String title;
    private String createdDate;
    private String receivedByDate;
    private List<POItemDTO> poItemList;
    private int totalQuantity;
    private double payableAmount;

    public PODataDTO(String userName,
                     String userID,
                     String prID,
                     String poID,
                     String status,
                     String title,
                     String createdDate,
                     String receivedByDate,
                     List<POItemDTO> poItemList,
                     int totalQuantity,
                     double payableAmount
    ) {
        this.userName = userName;
        this.userID = userID;
        this.prID = prID;
        this.poID = poID;
        this.status = status;
        this.title = title;
        this.createdDate = createdDate;
        this.receivedByDate = receivedByDate;
        this.poItemList = poItemList;
        this.totalQuantity = totalQuantity;
        this.payableAmount = payableAmount;
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

    public String getPoID() {
        return poID;
    }

    public void setPoID(String poID) {
        this.poID = poID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

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

    public List<POItemDTO> getPoItemList() {
        return poItemList;
    }

    public void setPoItemList(List<POItemDTO> poItemList) {
        this.poItemList = poItemList;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(double payableAmount) {
        this.payableAmount = payableAmount;
    }

    public static List<PODataDTO> getPODataDTOs(String filter_status) throws  Exception{
        List<PODataDTO> poDataDTOs = new ArrayList<>();

        List<PurchaseOrder> poList;
        List<PurchaseRequisition> prList = new QueryBuilder<>(PurchaseRequisition.class).select().from("db/PurchaseRequisition.txt").where("PRStatus", "=", "approved").getAsObjects();
        List<User> userList = new QueryBuilder<>(User.class).select().from("db/User.txt").getAsObjects();
        List<POItemDTO> poItemDTOList = POItemDTO.getPOItemDTOs();

        if(filter_status.equals("all")){
            poList = new QueryBuilder<>(PurchaseOrder.class).select().from("db/PurchaseOrder.txt").getAsObjects();
        } else{
            poList = new QueryBuilder<>(PurchaseOrder.class).select().from("db/PurchaseOrder.txt").where("POStatus", "=", filter_status).getAsObjects();
        }

        HashMap<String, PurchaseRequisition> prMap = new HashMap<>();
        HashMap<String, User> userMap = new HashMap<>();
        HashMap<String, List<POItemDTO>> poItemDTOMap = new HashMap<>();
        HashMap<String, Integer> totalQuantityMap = new HashMap<>();

        for (PurchaseRequisition pr : prList){
            prMap.put(pr.getPrRequisitionID(), pr);
        }
        for (User user : userList){
            userMap.put(user.getId(), user);
        }

        for(POItemDTO poItemDTO : poItemDTOList){
            String poID = poItemDTO.getPoID();
            if (poItemDTOMap.containsKey(poID)) {
                poItemDTOMap.get(poID).add(poItemDTO); // if already have a record, just add the POItem to the existing list
                totalQuantityMap.put(poID, totalQuantityMap.get(poID) + poItemDTO.getItemQuantity()); // Add the quantity on top of the prev total qty
            } else {
                List<POItemDTO> newList = new ArrayList<>();
                newList.add(poItemDTO); // add the first item
                poItemDTOMap.put(poID, newList); // put into the map
                totalQuantityMap.put(poID, poItemDTO.getItemQuantity());
            }
        }

        for(PurchaseOrder po : poList){
            User user = userMap.get(po.getUserID());
            PurchaseRequisition pr = prMap.get(po.getPrRequisitionID());
            poDataDTOs.add(
                po.getPODataDTO(
                    user,
                    pr,
                    poItemDTOMap.get(po.getpoID()),
                    totalQuantityMap.get(po.getpoID()))
                );
        }

        return poDataDTOs;
    }
}
