package models.Datas;

import models.ModelInitializable;
import models.Utils.IDGenerator;
import models.Utils.QueryBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class InventoryUpdateLog implements ModelInitializable {

    private String logID;
    private String itemID;
    private int prevQuantity;
    private int newQuantity;
    private int userId;
    private String batchId;
    private String note;


    public InventoryUpdateLog() {
    }

    public InventoryUpdateLog(String logID, String itemID, int prevQuantity, int newQuantity, int userId, String batchId, String note) {
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

    public String getItemId() {
        return itemID;
    }

    public int getPrevQuantity() {
        return prevQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public int getUserId() {
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
        this.logID = data.get("logID");
        this.itemID = data.get("itemID");
        this.prevQuantity = Integer.parseInt(data.get("prevQuantity"));
        this.newQuantity = Integer.parseInt(data.get("newQuantity"));
        this.userId = Integer.parseInt(data.get("userId"));
        this.batchId = data.get("batchId");
        this.note = data.get("note");
    }

    public void saveLog() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        QueryBuilder<InventoryUpdateLog> qb = new QueryBuilder<>(InventoryUpdateLog.class);
        String[] values = new String[]{
//                this.logID,
                this.itemID,
                String.valueOf(this.prevQuantity),
                String.valueOf(this.newQuantity),
                String.valueOf(this.userId),
                String.valueOf(this.batchId),
                this.note
        };
        qb.target("db/InventoryUpdateLog").values(values).create(this.logID);
    }

    public static void logItemUpdate(String itemId, int prevQty, int newQty, int userId, String note, boolean verified) throws Exception {
        String batchID = IDGenerator.generateBatchID();
        Batch batch = new Batch(batchID, LocalDateTime.now(), verified);
        batch.createBatch();
        InventoryUpdateLog log = new InventoryUpdateLog(IDGenerator.generateLogID(), itemId, prevQty, newQty, userId, batchID, note);
        log.saveLog();
    }

}
