package models.Datas;

public class Test {
    private final String reportCreater;
    private final String reportCreatedDate;

    public Test(String creatorName, String reportCreatedDate) {
        this.reportCreater = creatorName;
        this.reportCreatedDate = reportCreatedDate;
    }

    public String getReportCreater() {
        return reportCreater;
    }

    public String getReportCreatedDate() {
        return reportCreatedDate;
    }
}
