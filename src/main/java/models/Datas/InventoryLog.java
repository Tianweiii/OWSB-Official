package models.Datas;

import java.util.Date;

public class InventoryLog {

    private final String logId;
    private final String itemId;
    private int prevQuantity;
    private int newQuantity;
    private Date updatedAt;
    private final String userId;
    private final String batchId;
    private boolean verified;
    private String note;

    public InventoryLog(String logId, String itemId, int prevQuantity, int newQuantity, Date updatedAt, String userId, String batchId, boolean verified, String note) {
        this.logId = logId;
        this.itemId = itemId;
        this.prevQuantity = prevQuantity;
        this.newQuantity = newQuantity;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.batchId = batchId;
        this.verified = verified;
        this.note = note;
    }

    public String getLogId() {
        return logId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getPrevQuantity() {
        return prevQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getBatchId() {
        return batchId;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getNote() {
        return note;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setPrevQuantity(int prevQuantity) {
        this.prevQuantity = prevQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
