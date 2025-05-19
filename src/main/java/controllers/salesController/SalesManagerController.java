package controllers.salesController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import models.Datas.*;
import models.Utils.QueryBuilder;
import org.start.owsb.Layout;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * SalesManagerController
 * Controller for Sales Manager functionalities:
 * - Dashboard navigation
 * - Manage Items (CRUD)
 * - Manage Suppliers (CRUD)
 * - Record Daily Sales
 * - Create and View Purchase Requisitions
 * - View Purchase Orders
 */
public class SalesManagerController implements Initializable {

    private String[] sidebarItems;
    private String sidebarType;
    @FXML
    private GridPane sidebarGrid;
    @FXML private Button logoutButton;
    @FXML private Label usernameLabel;
    @FXML private Label positionLabel;

    // ============================ Shell & Navigation ============================
    @FXML private StackPane contentPane;

    // ============================ UI Components ============================
    // Item Management
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, Integer> colItemId;
    @FXML private TableColumn<Item, String> colItemName;
    @FXML private ComboBox<Supplier> cmbItemSupplier;
    @FXML private TextField txtItemName;

    // Supplier Management
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> colSupId;
    @FXML private TableColumn<Supplier, String> colSupName;
    @FXML private TextField txtSupName;
    @FXML private TextField txtSupCompany;

    // Daily Sales
    @FXML private TableView<Item> salesItemTable;
    @FXML private TableColumn<Item, Integer> colSalesItemId;
    @FXML private TableColumn<Item, String> colSalesItemName;
    @FXML private TextField txtSalesQty;

    // Purchase Requisition
    @FXML private ComboBox<Item> cmbPRItem;
    @FXML private TextField txtPRQty;
    @FXML private DatePicker dpPRDate;
    @FXML private TableView<PurchaseRequisition> prTable;
    @FXML private TableColumn<PurchaseRequisition, Integer> colPRId;
    @FXML private TableColumn<PurchaseRequisition, Integer> colPRQty;
    @FXML private TableColumn<PurchaseRequisition, String> colPRDate;

    // Purchase Order
    @FXML private TableView<PurchaseOrder> poTable;
    @FXML private TableColumn<PurchaseOrder, Integer> colPOId;
    @FXML private TableColumn<PurchaseOrder, Integer> colPOReqId;
    @FXML private TableColumn<PurchaseOrder, Integer> colPOStatusId;

    // ============================ Data Lists ============================
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    private final ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
    private final ObservableList<PurchaseRequisition> requisitions = FXCollections.observableArrayList();
    private final ObservableList<PurchaseOrder> purchaseOrders = FXCollections.observableArrayList();

    // ============================ Initialization ============================
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        try {
            // Inject sidebar and default module into the root layout
            Layout.getInstance().initSidebar("Sales Manager",
                    new String[]{"Dashboard","Items","Suppliers","Sales","Requisitions","Orders","Analytics"});
            setupTables();
            loadAllData();
            showDashboard(null);
        } catch (Exception e) {
            showError("Initialization Error: " + e.getMessage());
        }
    }

    // ============================ Navigation Handlers ============================
    @FXML private void showDashboard(ActionEvent event) { loadModule("/SalesManager/SalesManagerDashboard.fxml"); }
    @FXML private void showItemsView(ActionEvent event)   { loadModule("/Components/ItemsView.fxml"); }
    @FXML private void showSuppliersView(ActionEvent event){ loadModule("/Components/SuppliersView.fxml"); }
    @FXML private void showSalesView(ActionEvent event)   { loadModule("/Components/SalesEntryView.fxml"); }
    @FXML private void showPRView(ActionEvent event)      { loadModule("/Components/PurchaseRequisitionView.fxml"); }
    @FXML private void showPOView(ActionEvent event)      { loadModule("/Components/PurchaseOrderView.fxml"); }
    @FXML private void showAnalyticsView(ActionEvent event){ loadModule("/Components/AnalyticsView.fxml"); }

    /** Load a child FXML into the center pane */
    private void loadModule(String fxmlPath) {
        try {
            Parent module = FXMLLoader.load(getClass().getResource(fxmlPath));
            Layout.getInstance().setView(module);
        } catch (IOException e) {
            showError("Cannot load module: " + e.getMessage());
        }
    }

    // ============================ Table Setup & Data Loading ============================
    private void setupTables() {
        // Items
        colItemId.setCellValueFactory(new PropertyValueFactory<>("item_id"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("item_name"));
        itemTable.setItems(items);
        cmbItemSupplier.setItems(suppliers);

        // Suppliers
        colSupId.setCellValueFactory(new PropertyValueFactory<>("supplier_id"));
        colSupName.setCellValueFactory(new PropertyValueFactory<>("name"));
        supplierTable.setItems(suppliers);

        // Sales Entry
        colSalesItemId.setCellValueFactory(new PropertyValueFactory<>("item_id"));
        colSalesItemName.setCellValueFactory(new PropertyValueFactory<>("item_name"));
        salesItemTable.setItems(items);

        // Purchase Requisitions
        colPRId.setCellValueFactory(new PropertyValueFactory<>("pr_requisition_id"));
        colPRQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPRDate.setCellValueFactory(new PropertyValueFactory<>("received_by_date"));
        prTable.setItems(requisitions);
        cmbPRItem.setItems(items);

        // Purchase Orders
        colPOId.setCellValueFactory(new PropertyValueFactory<>("pr_order_id"));
        colPOReqId.setCellValueFactory(new PropertyValueFactory<>("pr_requisition_id"));
        colPOStatusId.setCellValueFactory(new PropertyValueFactory<>("pr_order_status_id"));
        poTable.setItems(purchaseOrders);
    }

    private void loadAllData() throws Exception {
        items.setAll(new QueryBuilder<>(Item.class).select().from("db/Item.txt").getAsObjects());
        suppliers.setAll(new QueryBuilder<>(Supplier.class).select().from("db/Supplier.txt").getAsObjects());
        requisitions.setAll(new QueryBuilder<>(PurchaseRequisition.class).select().from("db/PurchaseRequisition.txt").getAsObjects());
        purchaseOrders.setAll(new QueryBuilder<>(PurchaseOrder.class).select().from("db/PurchaseOrder.txt").getAsObjects());
    }

    // ============================ Item CRUD ============================
    @FXML private void handleAddItem() {
        String name = txtItemName.getText().trim();
        Supplier supplier = cmbItemSupplier.getValue();
        if (name.isEmpty() || supplier == null) {
            showError("Please provide a valid item name and select a supplier.");
            return;
        }
        try {
            new QueryBuilder<>(Item.class)
                    .target("db/Item.txt")
                    .values(new String[]{name, String.valueOf(supplier.getSupplierId())})
                    .create();
            loadAllData();
        } catch (Exception e) {
            showError("Failed to add item: " + e.getMessage());
        }
    }

    @FXML private void handleDeleteItem() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select an item to delete."); return; }
        try {
            new QueryBuilder<>(Item.class)
                    .target("db/Item.txt")
                    .delete(String.valueOf(selected.getItemID()));
            loadAllData();
        } catch (Exception e) {
            showError("Failed to delete item: " + e.getMessage());
        }
    }

    // ============================ Supplier CRUD ============================
    @FXML private void handleAddSupplier() {
        String name = txtSupName.getText().trim();
        String comp = txtSupCompany.getText().trim();
        if (name.isEmpty() || comp.isEmpty()) {
            showError("Supplier name and company must not be empty.");
            return;
        }
        try {
            new QueryBuilder<>(Supplier.class)
                    .target("db/Supplier.txt")
                    .values(new String[]{name, comp, "", ""})
                    .create();
            loadAllData();
        } catch (Exception e) {
            showError("Failed to add supplier: " + e.getMessage());
        }
    }

    @FXML private void handleDeleteSupplier() {
        Supplier sel = supplierTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Please select a supplier to delete."); return; }
        try {
            new QueryBuilder<>(Supplier.class)
                    .target("db/Supplier.txt")
                    .delete(String.valueOf(sel.getSupplierId()));
            loadAllData();
        } catch (Exception e) {
            showError("Failed to delete supplier: " + e.getMessage());
        }
    }

    // ============================ Daily Sales Entry ============================
    @FXML private void handleRecordSales() {
        Item sel = salesItemTable.getSelectionModel().getSelectedItem();
        String qtyText = txtSalesQty.getText().trim();
        if (sel == null || !isPositiveInteger(qtyText)) {
            showError("Please select an item and enter a valid quantity.");
            return;
        }
        try {
            new QueryBuilder<>(Transaction.class)
                    .target("db/Transaction.txt")
                    .values(new String[]{LocalDate.now().toString(), LocalDate.now().toString(), qtyText, String.valueOf(sel.getItemID()), ""})
                    .create();
            loadAllData();
        } catch (Exception e) {
            showError("Failed to record sale: " + e.getMessage());
        }
    }

    // ============================ Purchase Requisition ============================
    @FXML private void handleCreatePR() {
        Item sel = cmbPRItem.getValue();
        String qtyText = txtPRQty.getText().trim();
        LocalDate date = dpPRDate.getValue();
        if (sel == null || !isPositiveInteger(qtyText) || date == null) {
            showError("Please select an item, enter a valid quantity, and pick a date.");
            return;
        }
        try {
            new QueryBuilder<>(PurchaseRequisition.class)
                    .target("db/PurchaseRequisition.txt")
                    .values(new String[]{date.toString(), "", qtyText, String.valueOf(sel.getItemID())})
                    .create();
            loadAllData();
        } catch (Exception e) {
            showError("Failed to create purchase requisition: " + e.getMessage());
        }
    }

    // ============================ Utilities ============================
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.showAndWait();
    }
    private boolean isPositiveInteger(String s) {
        try { return Integer.parseInt(s) > 0; } catch (NumberFormatException e) { return false; }
    }
}
