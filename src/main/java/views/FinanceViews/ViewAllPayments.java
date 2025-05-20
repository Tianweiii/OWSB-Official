package views.FinanceViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import views.View;

import java.io.IOException;

public class ViewAllPayments implements View {

    private VBox viewAllPaymentsPane;

    public ViewAllPayments() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/ViewAllPayments.fxml"));
        viewAllPaymentsPane = loader.load();
    }


    @Override
    public Parent getView() {
        return viewAllPaymentsPane;
    }
}
