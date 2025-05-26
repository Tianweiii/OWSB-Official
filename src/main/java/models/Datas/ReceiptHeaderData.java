package models.Datas;

public class ReceiptHeaderData {

    // paid by
    private String paidTo;
    private String receiptNo;
    private String date;

    public ReceiptHeaderData(String paidTo, String receiptNo, String date) {
        this.paidTo = paidTo;
        this.receiptNo = receiptNo;
        this.date = date;
    }

    public String getPaidTo() {
        return paidTo;
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public String getDate() {
        return date;
    }
}
