package controllers.FinanceController;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class PaymentSuccessController implements Initializable {

    private FinanceMainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressBackToHome() {
        mainController.goHome();
    }

    public void pressPrintReceipt() {
        // send email to current login user email
        // send jasperreport pdf
    }
}
