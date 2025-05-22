package models.Users;

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
}
