package models.Datas;

import models.Initializable;
import models.Utils.QueryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class InventoryUpdateLog implements Initializable {

    private String logID;
    private int itemID;
    private int prevQuantity;
    private int newQuantity;
    private String userId;
    private String batchId;
    private String note;


    public InventoryUpdateLog() {}

    public InventoryUpdateLog(String logID, int itemID, int prevQuantity, int newQuantity, String userId, String batchId, String note) {
        this.logID = logID;
        this.itemID = itemID;
        this.prevQuantity = prevQuantity;
        this.newQuantity = newQuantity;
        this.userId = userId;
        this.batchId = batchId;
        this.note = note;
    }

    public String getLogId() {
        return logID;
    }

    public int getItemId() {
        return itemID;
    }

    public int getPrevQuantity() {
        return prevQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public String getUserId() {
        return userId;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getNote() {
        return note;
    }

    public void setPrevQuantity(int prevQuantity) {
        this.prevQuantity = prevQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static ArrayList<HashMap<String, String>> getInventoryUpdateLog() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        QueryBuilder<InventoryUpdateLog> qb = new QueryBuilder<>(InventoryUpdateLog.class);
        return qb.select().from("db/InventoryUpdateLog").get();
    }

    @Override
    public void initialize(HashMap<String, String> data) {

    }
}
