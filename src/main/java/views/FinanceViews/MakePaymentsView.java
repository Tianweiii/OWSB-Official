package views.FinanceViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import views.View;

import java.io.IOException;

public class MakePaymentsView implements View {

    private StackPane makePaymentsPane;

    public MakePaymentsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/MakePayment.fxml"));
        makePaymentsPane = loader.load();
    }

    @Override
    public Parent getView() {
        return makePaymentsPane;
    }
}
