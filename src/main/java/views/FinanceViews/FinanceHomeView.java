package views.FinanceViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import views.View;

import java.io.IOException;

public class FinanceHomeView implements View {

    private HBox financeHomePane;

    public FinanceHomeView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/FinanceHome.fxml"));
        this.financeHomePane = loader.load();
    }

    @Override
    public Parent getView() {
        return this.financeHomePane;
    }
}
