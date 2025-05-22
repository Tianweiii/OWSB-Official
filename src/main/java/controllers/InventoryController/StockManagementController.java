package controllers.InventoryController;

import controllers.NotificationController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import models.Datas.Item;
import models.Datas.Supplier;
import views.NotificationView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static models.Datas.Item.stringifyDateTime;

public class StockManagementController implements Initializable {

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnExportData;

    @FXML
    private TableColumn<Item, Integer> quantity;

    @FXML
    private TableColumn<Item, LocalDateTime> createdAt;

    @FXML
    private ComboBox<String> filterSelection;

    @FXML
    private TableColumn<Item, Integer> itemID;

    @FXML
    private TableColumn<Item, String> itemName;

    @FXML
    private TableView<Item> itemTable;

    @FXML
    private Label itemCountLabel;

    @FXML
    private TableColumn<Item, LocalDateTime> lastUpdated;

    @FXML
    private TableColumn<Supplier, String> supplierName;

    @FXML
    private TextField txtSearchKeyword;

    @FXML
    private AnchorPane stockManagementPane;

    @FXML
    void onClearButtonClicked(ActionEvent event) {
        clearSearchResult();
    }

    @FXML
    void onSearchButtonClicked(ActionEvent event) {
        searchItems();
    }

    @FXML
    void onExportButtonClicked(ActionEvent event) {
        try {
            exportItemData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final HashMap<Item, String> supplierMap = new HashMap<>();

    ObservableList<Item> itemList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        quantity.setCellValueFactory(new PropertyValueFactory<Item, Integer>("quantity"));
        createdAt.setCellValueFactory(new PropertyValueFactory<Item, LocalDateTime>("createdAt"));
        createdAt.setCellFactory(cell -> new TableCell<Item, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(stringifyDateTime(item));
                }
            }
        });
        itemID.setCellValueFactory(new PropertyValueFactory<Item, Integer>("itemID"));
        itemName.setCellValueFactory(new PropertyValueFactory<Item, String>("itemName"));
        lastUpdated.setCellValueFactory(new PropertyValueFactory<Item, LocalDateTime>("updatedAt"));
        lastUpdated.setCellFactory(cell -> new TableCell<Item, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(stringifyDateTime(item));
                }
            }
        });
        supplierName.setCellValueFactory(cellData ->
                new SimpleStringProperty(supplierMap.get(cellData.getValue()))
        );

        itemTable.setRowFactory(tv -> new TableRow<Item>() {
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else if (item.getQuantity() <= item.getAlertSetting()) {
                    setStyle("-fx-background-color: #F6D4D4;");
                } else {
                    setStyle("");
                }
            }
        });

        itemTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !itemTable.getSelectionModel().isEmpty()) {
                Item selectedItem = itemTable.getSelectionModel().getSelectedItem();

                try {
                    loadUpdateDialog(selectedItem);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
//        Loading item list
            loadInventoryTable();

//        Filtering function
            filterItems();

//        Update item count label
            updateItemCountLabel();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateItemCountLabel() {
        int count = itemTable.getItems().size();
        itemCountLabel.setText("(" + count + " items)");
    }

    private void loadInventoryTable() throws Exception {
        //        ItemList data
        HashMap<Item, String> data = Item.getItems(true);
        supplierMap.putAll(data);
        itemList.addAll(data.keySet());
        itemTable.setItems(itemList);
        itemTable.getSortOrder().add(itemID);
        itemID.setSortType(TableColumn.SortType.ASCENDING);
        itemTable.sort();

        updateItemCountLabel();
    }

    public void filterItems() {
        String[] filterList = {"All", "Alert Item"};
        filterSelection.getItems().addAll(filterList);
        filterSelection.setOnAction(event -> {
            String selectedFilter = filterSelection.getSelectionModel().getSelectedItem().toString();
            if (Objects.equals(selectedFilter, "Alert Item")) {
                ObservableList<Item> alertItems = FXCollections.observableArrayList();

                for (Item item : itemList) {
                    if (item.getQuantity() < item.getAlertSetting()) {
                        alertItems.add(item);
                    }
                }

                itemTable.setItems(alertItems);
            } else {
                itemTable.setItems(itemList);
            }

            updateItemCountLabel();
        });
    }

    public void searchItems() {
        String searchKeyword = txtSearchKeyword.getText().toLowerCase();

        FilteredList<Item> searchData = new FilteredList<>(itemList, item -> {
            if (searchKeyword.isEmpty()) return true;

            return item.getItemID().toLowerCase().contains(searchKeyword) ||
                    item.getItemName().toLowerCase().contains(searchKeyword) ||
                    item.getCreatedAt().toString().toLowerCase().contains(searchKeyword) ||
                    item.getUpdatedAt().toString().toLowerCase().contains(searchKeyword) ||
                    (supplierMap.get(item) != null && supplierMap.get(item).toLowerCase().contains(searchKeyword));
        });

        SortedList<Item> findData = new SortedList<>(searchData);
        findData.comparatorProperty().bind(itemTable.comparatorProperty());
        itemTable.setItems(findData);

        updateItemCountLabel();
    }

    public void clearSearchResult() {
        itemTable.setItems(itemList);
        txtSearchKeyword.setText("");

        updateItemCountLabel();
    }

    private void exportItemData() throws IOException {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Inventory Data");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("inventory_export_" +
                    java.time.LocalDate.now().toString() + ".csv");

            File file = fileChooser.showSaveDialog(stockManagementPane.getScene().getWindow());

            if (file != null) {
                FileWriter writer = new FileWriter(file);

                writer.write("Item ID,Item Name,Supplier Name,Quantity,Created At,Last Updated\n");

                for (Item item : itemTable.getItems()) {
                    writer.write(String.format("%s,%s,%s,%d,%s,%s\n",
                            item.getItemID(),
                            item.getItemName().replace(",", ";"), // Avoid CSV format issues
                            supplierMap.get(item).replace(",", ";"),
                            item.getQuantity(),
                            stringifyDateTime(item.getCreatedAt()),
                            stringifyDateTime(item.getUpdatedAt())
                    ));
                }

                writer.close();

                NotificationView notificationView = new NotificationView(
                        "Data exported successfully",
                        NotificationController.popUpType.success,
                        NotificationController.popUpPos.TOP);
                notificationView.show();
            }
        } catch (IOException ex) {
            NotificationView notificationView = new NotificationView(
                    "Error exporting data: " + ex.getMessage(),
                    NotificationController.popUpType.error,
                    NotificationController.popUpPos.TOP);
            notificationView.show();
        }
    }

    public void loadUpdateDialog(Item selectedItem) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InventoryFXML/UpdateInventoryDialog.fxml"));

        Parent updatePane = loader.load();

        UpdateInventoryController controller = loader.getController();

        controller.setItemData(selectedItem);

        controller.setRefreshCallback(this::refreshTableAfterUpdate);

        AnchorPane dimmedBackground = new AnchorPane();
        dimmedBackground.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        dimmedBackground.setPrefSize(stockManagementPane.getWidth(), stockManagementPane.getHeight());

        // Center modal
        AnchorPane.setTopAnchor(updatePane, (stockManagementPane.getHeight() - updatePane.prefHeight(-1)) / 2);
        AnchorPane.setLeftAnchor(updatePane, (stockManagementPane.getWidth() - updatePane.prefWidth(-1)) / 2);

        dimmedBackground.getChildren().add(updatePane);
        dimmedBackground.setOnMouseClicked(e -> {
            if (!updatePane.contains(e.getX() - updatePane.getLayoutX(), e.getY() - updatePane.getLayoutY())) {
                if (stockManagementPane.getChildren().contains(dimmedBackground)) {
                    stockManagementPane.getChildren().remove(dimmedBackground);
                }
            }
        });

        stockManagementPane.getChildren().add(dimmedBackground);

    }

    public void refreshTableAfterUpdate(Item updatedItem, Boolean updateSuccess) {
        try {
            supplierMap.clear();
            itemList.clear();
            loadInventoryTable();
            if (updateSuccess) {
                NotificationView notificationView = new NotificationView("Successful Updating Inventory", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notificationView.show();
            } else {
                NotificationView notificationView = new NotificationView("Error Updating Inventory", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
                notificationView.show();
            }

            updateItemCountLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
