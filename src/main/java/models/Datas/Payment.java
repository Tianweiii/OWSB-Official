package models.Datas;

import models.ModelInitializable;
import models.Utils.Helper;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Payment implements ModelInitializable {

	public enum PaymentMethod {
		Bank, TnG
	};

	private String paymentID;
	private PaymentMethod paymentMethod;
	private double amount;
	private String createdAt;
	private String PO_ID;
	private String userID;
	private String paymentReference;

	public Payment(PaymentMethod method, double amount, String PO_ID, String userID) {
		this.paymentMethod = method;
		this.amount = amount;
		this.PO_ID = PO_ID;
		this.userID = userID;

		// TODO: get latest row count
		int count = 1;

		this.paymentID = MessageFormat.format("PY{0}", count); // TODO: change to fetch row count
		this.createdAt = LocalDateTime.now().toString();
		this.paymentReference = generatePaymentReference(count);
	}

	private String generatePaymentReference(int hashKey) {
		String prefix = "REF";
		String temp = String.valueOf(hashKey);
		String hashCode = Helper.MD5_Hashing(temp).substring(0, 10);

		return MessageFormat.format("{0}-{1}", prefix, hashCode);
	}

	public String getPaymentID() {
		return paymentID;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public double getAmount() {
		return amount;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getPO_ID() {
		return PO_ID;
	}

	public String getUserID() {
		return userID;
	}

	public String getPaymentReference() {
		return paymentReference;
	}

	@Override
	public void initialize(HashMap<String, String> data) {
		this.paymentID = data.get("payment_id");
		this.paymentMethod = PaymentMethod.valueOf(data.get("payment_method"));
		this.amount = Double.parseDouble(data.get("amount"));
		this.createdAt = data.get("created_at");
		this.PO_ID = data.get("po_id");
		this.userID = data.get("user_id");
		this.paymentReference = data.get("payment_reference");
	}
}
