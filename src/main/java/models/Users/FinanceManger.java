package models.Users;

public class FinanceManger extends User {
    private int FinanceId;

    public FinanceManger(String username, int age, int financeId) {
        super(username, age);
        FinanceId = financeId;
    }
}
