package views.FinanceViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import views.View;

import java.io.IOException;

public class FinancePaymentsView implements View {

    private VBox financePaymentsPane;

    public FinancePaymentsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/FinancePayments.fxml"));
        financePaymentsPane = loader.load();
    }

    @Override
    public Parent getView() {
        return financePaymentsPane;
    }
}
