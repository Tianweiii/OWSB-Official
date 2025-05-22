package views.Inventory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;

public class StockManagementView implements View {
    private AnchorPane stockPane;

    public StockManagementView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InventoryFXML/StockManagement.fxml"));
        this.stockPane = loader.load();
    }

    public Parent getView() {
        return this.stockPane;
    }
}
