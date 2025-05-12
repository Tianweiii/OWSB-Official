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
import models.Datas.PurchaseOrder;
import models.Datas.PurchaseRequisition;
import models.Utils.FileIO;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FinancePRController implements Initializable {

    @FXML private TableView<PurchaseRequisition> PRTable;
    @FXML private TableColumn<PurchaseRequisition, String> PR_IDField;
    @FXML private TableColumn<PurchaseRequisition, String> userField;
    @FXML private TableColumn<PurchaseRequisition, String> createdDateField;
    @FXML private TableColumn<PurchaseRequisition, String> receiveDateField;
    @FXML private TableColumn<PurchaseRequisition, String> statusField;
    @FXML private TableColumn<PurchaseRequisition, Void> actionField;

    private FinanceMainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("table" + PRTable);
        initTable();
        try {
            QueryBuilder<PurchaseRequisition> qb = new QueryBuilder<>(PurchaseRequisition.class);
            ArrayList<PurchaseRequisition> PRs = qb.select().from("db/PurchaseRequisition").getAsObjects();
            fillTable(FXCollections.observableArrayList(PRs));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void initTable() {
        PR_IDField.setCellValueFactory(new PropertyValueFactory<>("prRequisitionID"));
        userField.setCellValueFactory(new PropertyValueFactory<>("userID"));
        createdDateField.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        receiveDateField.setCellValueFactory(new PropertyValueFactory<>("receivedByDate"));
        statusField.setCellValueFactory(new PropertyValueFactory<>("PRStatus"));

        actionField.setCellFactory(column -> {
            return new TableCell<PurchaseRequisition, Void>() {
                private final Button viewButton = new Button("View");

                {
                    // Button action
                    viewButton.setOnAction(event -> {
                        PurchaseRequisition pr = getTableView().getItems().get(getIndex());
                        SessionManager.setCurrentPR(pr);
                        mainController.onPressPRDetails();
                    });
                }

                // idk why this is needed, if removed then button wont show
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(viewButton);
                    }
                }
            };
        });
    }

    private void fillTable(ObservableList<PurchaseRequisition> list) {
        PRTable.setItems(list);
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }
}
