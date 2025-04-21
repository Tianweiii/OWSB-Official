package models.Datas;

import java.util.Date;

public class Batch {
    private int batchID;
    private Date updatedDatetime;
    private boolean verified;

    public Batch(int batchID, Date updatedDatetime, boolean verified) {
        this.batchID = batchID;
        this.updatedDatetime = updatedDatetime;
        this.verified = verified;
    }

    public int getBatchID() {
        return batchID;
    }

    public Date getUpdatedDatetime() {
        return updatedDatetime;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setBatchID(int batchID) {
        this.batchID = batchID;
    }

    public void setUpdatedDatetime(Date updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
