package models.Users;

import java.util.ArrayList;
import java.util.List;
import models.Datas.Item;

public class InventoryManager extends User{
    private String managerCode;
    private String assignedWarehouse;

    public InventoryManager() {
        super();
    }

    public InventoryManager(String username, int age, String managerCode, String assignedWarehouse) {
        super(username, age);
        this.managerCode = managerCode;
        this.assignedWarehouse = assignedWarehouse;
    }

    public String getManagerCode() {
        return managerCode;
    }

    public String getAssignedWarehouse() {
        return assignedWarehouse;
    }

    public void setManagerCode(String managerCode) {
        this.managerCode = managerCode;
    }

    public void setAssignedWarehouse(String assignedWarehouse) {
        this.assignedWarehouse = assignedWarehouse;
    }

    public void viewItemList() {
//        View Item
    }

    public void generateStockReport() {
//        Gen stock report
    }

    @Override
    public String toString() {
        return "InventoryManager{" +
                "username='" + getName() + '\'' +
                "managerCode='" + managerCode + '\'' +
                ", assignedWarehouse='" + assignedWarehouse + '\'' +
                '}';
    }
}
