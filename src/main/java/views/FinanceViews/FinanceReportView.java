package views.FinanceViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import views.View;

import java.io.IOException;

public class FinanceReportView implements View {

    private VBox financeReportPane;

    public FinanceReportView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/FinanceReport.fxml"));
        financeReportPane = loader.load();
    }

    @Override
    public Parent getView() {
        return financeReportPane;
    }
}
