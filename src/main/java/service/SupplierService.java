package service;

import models.Datas.Supplier;
import models.Utils.QueryBuilder;
import java.util.Collections;
import java.util.List;

/**
 * Business logic for managing suppliers.
 */
public class SupplierService extends Service<Supplier> {
    private static final String DB_PATH = "db/supplier";

    @Override
    public List<Supplier> getAll() {
        try {
            QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
            return qb.select().from(DB_PATH).getAsObjects();
        } catch (Exception e) {
            System.out.println(e.getMessage());;
            return Collections.emptyList();
        }
    }

    /**
     * Add a new supplier. Returns true if creation succeeds.
     */
    public boolean add(String name, String company, String phone, String address) {
        try {
            QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
            return qb.target(DB_PATH).values(new String[]{name, company, phone, address}).create();
        } catch (Exception e) {
            System.out.println(e.getMessage());;
            return false;
        }
    }

    /**
     * Update an existing supplier by ID. Returns true on success.
     */
    public boolean update(String id, String name, String company, String phone, String address) {
        try {
            QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
            return qb.update(id, new String[]{name, company, phone, address}
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());;
            return false;
        }
    }

    /**
     * Delete a supplier by ID. Returns true on success.
     */
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