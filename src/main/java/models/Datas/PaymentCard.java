package models.Datas;

import models.ModelInitializable;

import java.util.HashMap;

public class PaymentCard implements ModelInitializable {

    private String cardID;
    private long cardNumber;
    private String cardName;
    private String expiryDate;
    private int cvv;
    private double cardBalance;

    public PaymentCard(long cardNumber, String cardName, String expiryDate, int cvv, double cardBalance) {
        this.cardNumber = cardNumber;
        this.cardName = cardName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardBalance = cardBalance;
    }

    public PaymentCard() {

    }

    @Override
    public void initialize(HashMap<String, String> data) {
        cardID = data.get("cardID");
//        cardNumber = Integer.parseInt(data.get("cardNumber"));
        cardNumber = Long.parseLong(data.get("cardNumber"));
        cardName = data.get("cardName");
        expiryDate = data.get("expiryDate");
        cvv = Integer.parseInt(data.get("cvv"));
        cardBalance = Double.parseDouble(data.get("cardBalance"));
    }

    public String getCardID() {
        return cardID;
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public int getCvv() {
        return cvv;
    }

    public double getCardBalance() {
        return cardBalance;
    }
}
