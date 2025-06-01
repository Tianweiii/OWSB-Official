package views.FinanceViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import views.View;

import java.io.IOException;

public class PaymentSuccessView implements View {
    private StackPane paymentSuccessPane;

    public PaymentSuccessView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/PaymentSuccess.fxml"));
        paymentSuccessPane = loader.load();
    }

    @Override
    public Parent getView() {
        return paymentSuccessPane;
    }
}
