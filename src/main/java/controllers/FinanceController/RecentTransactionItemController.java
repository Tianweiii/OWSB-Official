package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class RecentTransactionItemController implements Initializable {

    @FXML
    private Text idField;
    @FXML
    private Text nameField;
    @FXML
    private Text amountField;
    @FXML
    private VBox leftContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setData(String paymentID, String paymentDate, double amount) {
        nameField.setText(paymentID);
        idField.setText(paymentDate);
        amountField.setText("RM " + String.valueOf(amount));
    }

    public void setColor(String colorCode) {
        leftContainer.setStyle(
                "-fx-background-color: " + colorCode + ";" +
                        "-fx-background-radius: 20;"
        );
    }
}
