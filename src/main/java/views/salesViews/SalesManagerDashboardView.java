package views.salesViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;
import java.net.URL;

public class SalesManagerDashboardView implements View {
    private final AnchorPane salesManagerDashboardPane;

    public SalesManagerDashboardView() throws IOException {
        FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/SalesManagerDashboard.fxml"));
        this.salesManagerDashboardPane = loader.load();
    }

    @Override
    public Parent getView() {
        return this.salesManagerDashboardPane;
    }
}
