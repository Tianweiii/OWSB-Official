package models.Datas;

import models.ModelInitializable;
import models.Utils.QueryBuilder;

import javax.management.Query;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

public class Batch implements ModelInitializable {
    private String batchID;
    private LocalDateTime updatedDatetime;
    private boolean verified;

    public Batch() {

    }

    public Batch(String batchID, LocalDateTime updatedDatetime, boolean verified) {
        this.batchID = "Batch-" + System.currentTimeMillis();
        this.updatedDatetime = updatedDatetime;
        this.verified = verified;
    }

    public String getBatchID() {
        return "Batch-" + System.currentTimeMillis();
    }

    public LocalDateTime getUpdatedDatetime() {
        return updatedDatetime;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public void setUpdatedDatetime(LocalDateTime updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void createBatch() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        QueryBuilder<Batch> qb = new QueryBuilder<Batch>(Batch.class);
        String[] values = new String[] {
                this.batchID,
                String.valueOf(this.updatedDatetime),
                String.valueOf(this.verified)
        };
        qb.target("db/Batch").values(values).create();
    }

    public static void logBatch(String batchID, LocalDateTime updatedDatetime, boolean verified) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Batch batch = new Batch(batchID, updatedDatetime, verified);
        batch.createBatch();
    }

    @Override
    public void initialize(HashMap<String, String> data) {

    }
}
