package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.DTO.SalesItemDTO;
import models.Datas.Payment;
import models.Datas.Sales;

import java.net.URL;
import java.util.ResourceBundle;

public class ReportPaymentItemController implements Initializable {

    @FXML
    private Text POTitle;
    @FXML
    private Text moneyField;
    @FXML
    private Text username;

    @FXML
    private VBox imageContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setData(Payment i) {
        POTitle.setText(i.getPaymentID());
        moneyField.setText("- RM " + String.valueOf(i.getAmount()));
        username.setText(i.getUserID());
    }

    public void setData(SalesItemDTO i) {
        POTitle.setText(i.getItemName());
        moneyField.setText("+ RM " + String.valueOf(i.getAmount()));
        username.setText("x" + String.valueOf(i.getQuantity()));

        imageContainer.setStyle("-fx-background-color: #0ca813; -fx-background-radius: 100;");
        moneyField.setStyle("-fx-fill: #0ca813;");
    }
}
