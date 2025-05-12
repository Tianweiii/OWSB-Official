package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import models.Datas.Payment;

import java.net.URL;
import java.util.ResourceBundle;

public class ReportPaymentItemController implements Initializable {

    @FXML
    private Text POTitle;
    @FXML
    private Text moneyField;
    @FXML
    private Text username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setData(Payment i) {
        POTitle.setText(i.getPaymentID());
        moneyField.setText("+ RM " + String.valueOf(i.getAmount()));
        username.setText(i.getUserID());
    }
}
