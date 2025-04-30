package controllers.InventoryController;

import controllers.NotificationController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.DTO.InventoryUpdateRequestDTO;
import models.Datas.InventoryUpdateLog;
import models.Datas.InventoryUpdateRequest;
import models.Datas.Item;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;
import views.NotificationView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InventoryUpdateRequestController implements Initializable {

    @FXML
    private TableColumn<InventoryUpdateRequestDTO, Void> actions;

    @FXML
    private TableColumn<InventoryUpdateRequestDTO, String> itemID;

    @FXML
    private TableColumn<InventoryUpdateRequestDTO, String> itemName;

    @FXML
    private TableColumn<InventoryUpdateRequestDTO, Integer> quantity;

    @FXML
    private TableView<InventoryUpdateRequestDTO> salesRequestTableView;

    @FXML
    private Label txtNumSalesRequest;

    @FXML
    private TableColumn<InventoryUpdateRequestDTO, String> userName;

    SessionManager session = SessionManager.getInstance();
    HashMap<String, String> userData = session.getUserData();
    int currentUserID = Integer.parseInt(userData.get("user_id"));

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        configureTableCols();
        try {
            populateTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void configureTableCols() {
        itemID.setCellValueFactory(new PropertyValueFactory<>("itemID"));
        itemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        userName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        addActionsButtons();
    }

    private void addActionsButtons() {
        actions.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button declineButton = new Button("Decline");
            private final HBox hbox = new HBox(10, updateButton, declineButton);

            {
                updateButton.setStyle("-fx-background-color: #A8E6A3; -fx-text-fill: black; -fx-background-radius: 5;");
                declineButton.setStyle("-fx-background-color: #F7A6A6; -fx-text-fill: black; -fx-background-radius: 5;");

                hbox.setAlignment(Pos.CENTER);
                hbox.setSpacing(10);

                updateButton.setOnAction(event -> {
                    InventoryUpdateRequestDTO request = getTableView().getItems().get(getIndex());
                    try {
                        handleUpdate(request);
                    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                declineButton.setOnAction(event -> {
                    InventoryUpdateRequestDTO request = getTableView().getItems().get(getIndex());
                    try {
                        handleDecline(request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    public void populateTable() {
        try {
            List<InventoryUpdateRequestDTO> inventoryURTable = InventoryUpdateRequestDTO.getInventoryUpdateRequestDTOs().stream()
                    .filter(d -> "Pending".equals(d.getStatus()))
                    .collect(Collectors.toList());
            ObservableList<InventoryUpdateRequestDTO> tableData = FXCollections.observableArrayList(inventoryURTable);
            salesRequestTableView.setItems(tableData);

            txtNumSalesRequest.setText(tableData.size() + " pending sales item request(s)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleUpdate(InventoryUpdateRequestDTO inventoryUpdateRequestDTO) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Optional<ButtonType> result = showConfirmationDialog(
                "Confirm Update",
                "Are you sure you want to approve this update?",
                "This action will approve the sales item request."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String itemID = inventoryUpdateRequestDTO.getItemID();
                int quantity = inventoryUpdateRequestDTO.getQuantity();
                int userID = inventoryUpdateRequestDTO.getUserID();

                QueryBuilder<Item> qbItem = new QueryBuilder<>(Item.class);
                Item item = qbItem.select().from("db/Item.txt").getAsObjects().stream()
                        .filter(i -> i.getItemID().equals(itemID))
                        .findFirst()
                        .orElseThrow(() -> new Exception("Item not found"));

                if (item.getQuantity() >= quantity && (item.getQuantity() - quantity) >= item.getAlertSetting()) {
                    int leftoverQty = item.getQuantity() - quantity;
                    InventoryUpdateLog.logItemUpdate(item.getItemID(), item.getQuantity(), leftoverQty, currentUserID, "Update Sales Request", true);

                    item.setQuantity(leftoverQty);

                    String[] updateValues = new String[]{
                            item.getItemName(),
                            item.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            String.valueOf(item.getAlertSetting()),
                            String.valueOf(item.getQuantity()),
                            String.valueOf(item.getUnitPrice()),
                            String.valueOf(item.getSupplierID()),
                    };

                    qbItem.update(item.getItemID(), updateValues);


                    QueryBuilder<InventoryUpdateRequest> qbRequest = new QueryBuilder<>(InventoryUpdateRequest.class);
                    List<InventoryUpdateRequest> requests = qbRequest.select().from("db/InventoryUpdateRequest.txt").getAsObjects();
                    InventoryUpdateRequest requestToUpdate = requests.stream()
                            .filter(req -> req.getInventoryUpdateRequestID().equals(inventoryUpdateRequestDTO.getInventoryUpdateRequestID()))
                            .findFirst()
                            .orElseThrow(() -> new Exception("Request not found"));

                    requestToUpdate.setStatus("Approved");

                    qbRequest.update(requestToUpdate.getInventoryUpdateRequestID(), new String[]{
                            requestToUpdate.getItemID(),
                            String.valueOf(requestToUpdate.getUserID()),
                            String.valueOf(requestToUpdate.getQuantity()),
                            requestToUpdate.getStatus(),
                    });

                    NotificationView notificationView = new NotificationView("Successful Updating", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                    notificationView.show();

                } else {
                    NotificationView notificationView = new NotificationView("Error Updating - Out of stock", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
                    notificationView.show();
                }

            } catch (Exception e) {
                NotificationView notificationView = new NotificationView("Error Updating" + e, NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
                notificationView.show();
                throw new RuntimeException(e);
            } finally {
                populateTable();
            }

        } else {
            NotificationView notificationView = new NotificationView("Update Cancelled", NotificationController.popUpType.info, NotificationController.popUpPos.TOP);
            notificationView.show();
        }
    }

    public void handleDecline(InventoryUpdateRequestDTO inventoryUpdateRequestDTO) throws IOException {
        try {

            Optional<ButtonType> result = showConfirmationDialog(
                    "Confirm Update",
                    "Are you sure you want to approve this update?",
                    "This action will decline the sales item request."
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                QueryBuilder<InventoryUpdateRequest> qbRequest = new QueryBuilder<>(InventoryUpdateRequest.class);
                List<InventoryUpdateRequest> requests = qbRequest.select().from("db/InventoryUpdateRequest.txt").getAsObjects();
                InventoryUpdateRequest requestToUpdate = requests.stream()
                        .filter(req -> req.getInventoryUpdateRequestID().equals(inventoryUpdateRequestDTO.getInventoryUpdateRequestID()))
                        .findFirst()
                        .orElseThrow(() -> new Exception("Request not found"));

                requestToUpdate.setStatus("Rejected");

                qbRequest.update(requestToUpdate.getInventoryUpdateRequestID(), new String[]{
                        requestToUpdate.getItemID(),
                        String.valueOf(requestToUpdate.getUserID()),
                        String.valueOf(requestToUpdate.getQuantity()),
                        requestToUpdate.getStatus(),
                });

                NotificationView notificationView = new NotificationView("The inventory request is rejected", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notificationView.show();
            } else {
                NotificationView notificationView = new NotificationView("The update has been cancelled", NotificationController.popUpType.info, NotificationController.popUpPos.TOP);
                notificationView.show();
            }
        } catch (Exception e) {
            NotificationView notificationView = new NotificationView("Error Updating" + e, NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
            notificationView.show();
            throw new RuntimeException(e);
        } finally {
            populateTable();
        }
    }

    private Optional<ButtonType> showConfirmationDialog(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }

}


