package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import models.DTO.PRItemDTO;
import models.Datas.PurchaseRequisitionItem;

import java.net.URL;
import java.util.ResourceBundle;

public class PRItemController implements Initializable {

    @FXML private Text titleField;
    @FXML private Text supplierField;
    @FXML private Text quantityField;
    @FXML private Text unitPriceField;
    @FXML private Text amountField;

    private PRDetailsController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setMainController(PRDetailsController controller) {
        mainController = controller;
    }

    public void setData(PRItemDTO item) {
        titleField.setText(item.getItemName());
        supplierField.setText("S2");
        quantityField.setText(String.valueOf(item.getQuantity()));
        unitPriceField.setText(String.valueOf(item.getUnitPrice()));
        amountField.setText(String.valueOf(item.getTotal()));
    }
}
