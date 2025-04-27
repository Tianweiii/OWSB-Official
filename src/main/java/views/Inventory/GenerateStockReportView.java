package views.Inventory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;

public class GenerateStockReportView implements View {
    private AnchorPane stockReportPane;

    public GenerateStockReportView() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InventoryFXML/StockReportGeneration.fxml"));
        this.stockReportPane = loader.load();
    }

    @Override
    public Parent getView() {
        return this.stockReportPane;
    }
}
