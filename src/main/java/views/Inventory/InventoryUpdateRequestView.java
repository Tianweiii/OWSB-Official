package views.Inventory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;

public class InventoryUpdateRequestView implements View {

    private AnchorPane salesPRPane;

    public InventoryUpdateRequestView() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InventoryFXML/InventoryUpdateRequest.fxml"));
        this.salesPRPane = loader.load();
    }

    @Override
    public Parent getView() {
        return this.salesPRPane;
    }
}
