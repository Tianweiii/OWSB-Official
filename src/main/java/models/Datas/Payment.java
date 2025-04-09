package models.Datas;

import models.Utils.Helper;

import java.text.MessageFormat;
import java.time.LocalDateTime;

public class Payment {

	public enum PaymentMethod {
		Bank, TnG
	};

	private final String paymentID;
	private final PaymentMethod paymentMethod;
	private double amount;
	private final String createdAt;
	private final String PO_ID;
	private final String userID;
	private final String paymentReference;

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


}
