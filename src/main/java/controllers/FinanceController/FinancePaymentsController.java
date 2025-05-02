package controllers.FinanceController;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FinancePaymentsController implements Initializable, IdkWhatToNameThis {

    private FinanceMainController mainController;
    @FXML
    private VBox makePaymentButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressMakePayment() {
        mainController.onPressPayment();
    }
}
