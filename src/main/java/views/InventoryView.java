package views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class InventoryView implements View{

    private AnchorPane stockPane;

    public InventoryView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InventoryFXML/StockManagement.fxml"));
        this.stockPane = loader.load();
    }

    @Override
    public Parent getView() {
        return this.stockPane;
    }
}
