package views.salesViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import views.View;

import java.io.IOException;
import java.net.URL;

public class SupplierView implements View {
    private final StackPane supplierPane;

    public SupplierView() throws IOException {
        FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/SupplierManagement.fxml"));
        this.supplierPane = loader.load();
    }
    @Override
    public Parent getView() {
        return this.supplierPane;
    }
}
