package models.Datas;

import models.ModelInitializable;
import models.Utils.QueryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class Supplier implements ModelInitializable {
    private String supplierID;
    private String supplierName;
    private String company;
    private String phoneNumber;
    private String address;

    public String getSupplierId() {
        return supplierID;
    }

    public String getCompanyName() {
        return this.company;
    }

    public String getSupplierName() {
        return this.supplierName;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getAddress() {
        return this.address;
    }

	public static ArrayList<HashMap<String, String>> getSupplierNameById(String supplierID) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
        ArrayList<HashMap<String, String>> suppliers = qb.select(new String[]{"supplierName"})
                .from("db/Supplier")
                .where("supplierID", "=", supplierID)
				.get();
		return suppliers;
}
    @Override
    public void initialize(HashMap<String, String> data) {
		this.supplierID = data.get("supplierID");
		this.supplierName = data.get("supplierName");
		this.company = data.get("company");
		this.phoneNumber = data.get("phoneNumber");
        this.address = data.get("address");
    }
}
