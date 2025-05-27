package models.Datas;

public class FinanceReportHeaderData {

    private String generatedBy;
    private String generatedDate;

    public FinanceReportHeaderData(String generatedDate, String generatedBy) {
        this.generatedDate = generatedDate;
        this.generatedBy = generatedBy;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public String getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public void setGeneratedDate(String generatedDate) {
        this.generatedDate = generatedDate;
    }
}
