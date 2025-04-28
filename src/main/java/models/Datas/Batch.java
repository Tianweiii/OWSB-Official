package models.Datas;

import models.ModelInitializable;
import models.Utils.QueryBuilder;

import javax.management.Query;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Batch implements ModelInitializable {
    private String batchID;
    private LocalDateTime updatedDatetime;
    private boolean verified;

    public Batch() {

    }

    public Batch(String batchID, LocalDateTime updatedDatetime, boolean verified) {
        this.batchID = batchID;
        this.updatedDatetime = updatedDatetime;
        this.verified = verified;
    }

    public String getBatchID() {
        return batchID;
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
        QueryBuilder<Batch> qb = new QueryBuilder<>(Batch.class);

        System.out.println("Batch Attributes: ");
        System.out.println("batchID: " + this.batchID);
        System.out.println("updatedDatetime: " + this.updatedDatetime);
        System.out.println("verified: " + (this.verified ? "Verified" : "Not verified"));

        String[] values = new String[]{
                this.batchID,
                this.updatedDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                this.verified ? "Verified" : "Not verified"
        };
        System.out.println(Arrays.toString(values));

        for (String value : values) {
            System.out.println(value);
        }
        qb.target("db/Batch").values(values).create();
    }

    @Override
    public void initialize(HashMap<String, String> data) {

    }
}
