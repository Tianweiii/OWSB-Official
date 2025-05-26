package models.Utils;

import models.Datas.PurchaseOrder;
import models.Datas.PurchaseRequisition;
import models.Users.FinanceManager;

import java.util.HashMap;

public class SessionManager {
	private static SessionManager instance;
	private HashMap<String, String> userData;

	// create session instance on login, all values binded to that instance
	// clear instance on logout
	// all temp static, for static, will change to non after binding
	private  FinanceManager financeManagerData;
	private static PurchaseOrder paymentPurchaseOrder;
	private static PurchaseRequisition purchaseRequisition;

	public static SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}

	public HashMap<String, String> getUserData() {
		return this.userData;
	}

	public void setUserData(HashMap<String, String> userData) {
		this.userData = userData;
	}

	public void setFinanceManagerData(FinanceManager fm) {
		financeManagerData = fm;
	}

	public FinanceManager getFinanceManagerData() { return financeManagerData; }

	public static void setCurrentPaymentPO(PurchaseOrder PO) {
		paymentPurchaseOrder = PO;
		System.out.println(PO.toString());
	}

	public static PurchaseOrder getCurrentPaymentPO() { return paymentPurchaseOrder; }

	public static void setCurrentPR(PurchaseRequisition pr) {
		purchaseRequisition = pr;
	}

	public static PurchaseRequisition getPurchaseRequisition() { return purchaseRequisition; }
}
