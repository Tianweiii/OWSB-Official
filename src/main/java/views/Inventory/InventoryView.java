package views.Inventory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import views.View;

import java.io.IOException;

public class InventoryView implements View {

    private StackPane homePane;

    public InventoryView() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InventoryFXML/InventoryView.fxml"));
        this.homePane = loader.load();
    }

    @Override
    public Parent getView() {
        return this.homePane;
    }
}
