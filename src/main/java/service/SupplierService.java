package service;

import models.Datas.Supplier;
import models.Utils.QueryBuilder;
import java.util.Collections;
import java.util.List;

public class SupplierService extends Service<Supplier> {
    private static final String DB_PATH = "db/supplier";

    @Override
    public List<Supplier> getAll() {
        try {
            QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
            return qb.select().from(DB_PATH).getAsObjects();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean add(String name, String company, String phone, String address) {
        try {
            String modifiedAddress = address.replace(",", "|");
            QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
            return qb.target(DB_PATH).values(new String[]{name, company, phone, modifiedAddress}).create();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean update(String id, String name, String company, String phone, String address) {
        try {
            String modifiedAddress = address.replace(",", "|");
            QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
            return qb.update(id, new String[]{name, company, phone, modifiedAddress});
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        try {
            QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
            return qb.target(DB_PATH).delete(id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}