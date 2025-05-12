package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Datas.Payment;
import models.Utils.FileIO;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FinanceReportController implements Initializable {

    @FXML
    private VBox paymentsContainer;
    @FXML
    private VBox salesContainer;
    @FXML
    private Text totalCostField;
    @FXML
    private Text totalPendingField;
    @FXML
    private Text totalSalesField;
    @FXML
    private Text totalTransactionsField;

    private FinanceMainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            populatePaymentsContainer();
        } catch (IOException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void viewAllPayments() {
        mainController.viewAllPayments();
    }

    public void populatePaymentsContainer() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ArrayList<Payment> list = FileIO.getObjectsFromXLines(Payment.class, "Payment", 4);
        for (Payment i : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/ReportPaymentItem.fxml"));
                Parent card = loader.load();

                ReportPaymentItemController controller = loader.getController();
                controller.setData(i);

                paymentsContainer.getChildren().add(card);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void populateSalesContainer() {

    }
}
