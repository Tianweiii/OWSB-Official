package controllers.FinanceController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Datas.PurchaseOrder;
import models.Datas.PurchaseOrderItem;
import models.Utils.FileIO;
import models.Utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FinancePaymentsController implements Initializable, IdkWhatToNameThis {

    private FinanceMainController mainController;
    @FXML
    private VBox makePaymentButton;

    // table ids
    @FXML private TableView<PurchaseOrder> POTable;
    @FXML private TableColumn<PurchaseOrder, String> PO_IDField;
    @FXML private TableColumn<PurchaseOrder, String> titleField;
    @FXML private TableColumn<PurchaseOrder, Double> amountField;
    @FXML private TableColumn<PurchaseOrder, String> openedByField;
    @FXML private TableColumn<PurchaseOrder, String> statusField;
    @FXML private TableColumn<PurchaseOrder, Void> actionField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initTable();
            ObservableList<PurchaseOrder> POs = this.getAllVerifiedPO();
            fillTable(POs);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressMakePayment() {
        mainController.onPressPayment();
    }

    ObservableList<PurchaseOrder> getAllVerifiedPO() throws IOException, ReflectiveOperationException {
        ArrayList<PurchaseOrder> POs = FileIO.getIDsAsObjects(PurchaseOrder.class, "PurchaseOrder", "verified", 5);
        System.out.println(POs);
        return FXCollections.observableArrayList(POs);
    }

    private void initTable() {
        PO_IDField.setCellValueFactory(new PropertyValueFactory<>("PO_ID"));
        titleField.setCellValueFactory(new PropertyValueFactory<>("title"));
        amountField.setCellValueFactory(new PropertyValueFactory<>("payableAmount"));
        openedByField.setCellValueFactory(new PropertyValueFactory<>("userID"));
        statusField.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionField.setCellFactory(column -> {
            return new TableCell<PurchaseOrder, Void>() {
                private final Button payButton = new Button("Pay");

                {
//                    payButton.set
                    // Button action
                    payButton.setOnAction(event -> {
                        PurchaseOrder po = getTableView().getItems().get(getIndex());
                        SessionManager.setCurrentPaymentPO(po);
                        mainController.onPressPayment();
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(payButton);
                    }
                }
            };
        });
    }

    private void fillTable(ObservableList<PurchaseOrder> list) {
        POTable.setItems(list);
        System.out.println("Table is filled");
    }

    public void onPressRow(MouseEvent e) {
        if (e.getClickCount() == 1) {
            PurchaseOrder selection = POTable.getSelectionModel().getSelectedItem();
            System.out.println(selection.toString());
        }
    }
}
