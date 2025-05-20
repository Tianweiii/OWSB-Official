package views.FinanceViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import views.View;

import java.io.IOException;

public class ViewAllSales implements View {

    private VBox viewAllSalesPane;

    public ViewAllSales() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/ViewAllSales.fxml"));
        viewAllSalesPane = loader.load();
    }


    @Override
    public Parent getView() {
        return viewAllSalesPane;
    }
}
