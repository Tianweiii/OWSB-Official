package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.DTO.PRItemDTO;
import models.Datas.PurchaseRequisition;
import models.Datas.PurchaseRequisitionItem;
import models.Utils.Helper;
import models.Utils.SessionManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PRDetailsController implements Initializable {

    @FXML private Text PR_IDField;
    @FXML private Text statusField;
    @FXML private Text userField;
    @FXML private Text createdDateField;
    @FXML private Text receiveDateField;
    @FXML private VBox itemsContainer;

    private final PurchaseRequisition currentPR = SessionManager.getPurchaseRequisition();
    private FinanceMainController mainController;
    private List<PRItemDTO> prItems;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            prItems = currentPR.getPurchaseItemList();

            PR_IDField.setText(currentPR.getPrRequisitionID());
            statusField.setText(Helper.capitalize(currentPR.getPRStatus()));
            statusField.setStyle("-fx-text-fill: " + Helper.setPRStatusText(currentPR.getPRStatus()) + ";");
            userField.setText(currentPR.getUserID());
            createdDateField.setText(currentPR.getCreatedDate());
            receiveDateField.setText(currentPR.getReceivedByDate());
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        for (PRItemDTO i : prItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/PRItem.fxml"));
                Parent card = loader.load();

                PRItemController controller = loader.getController();
                controller.setMainController(this);
                controller.setData(i);

                itemsContainer.getChildren().add(card);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressBack() {
        mainController.goBack();
    }

}
