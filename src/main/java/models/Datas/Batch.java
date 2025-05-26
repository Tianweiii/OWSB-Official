package models.Datas;

import models.ModelInitializable;
import models.Utils.QueryBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public void createBatch() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        QueryBuilder<Batch> qb = new QueryBuilder<>(Batch.class);

        String[] values = new String[]{
                this.updatedDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                this.verified ? "Verified" : "Not verified"
        };
        qb.target("db/Batch").values(values).create(this.batchID);
    }

    @Override
    public void initialize(HashMap<String, String> data) {
        this.batchID = data.get("batchID");
        this.updatedDatetime = LocalDateTime.parse(data.get("updatedDatetime"));
        this.verified = data.get("verified").equals("Verified");
    }
}
