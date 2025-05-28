package controllers.salesController;

import controllers.NotificationController;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.Callback;
import models.Datas.Supplier;
import models.Utils.SessionManager;
import service.SupplierService;
import views.NotificationView;
import views.salesViews.DeleteSupplierView;
import models.Utils.Validation;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {

    @FXML private Button addSupplierBtn;
    @FXML private StackPane rootPane;
    @FXML private TableView<Supplier> table;
    @FXML private TableColumn<Supplier, String> colId,colName,colPhone,colCompany;
    @FXML private TableColumn<Supplier, Void> colActions;
    @FXML private Label totalSupplierLabel;

    @FXML private TextField txtSearch;
    @FXML private Button clearSearchButton;
    @FXML private ComboBox<String> cmbSort;

    @FXML private Button deleteButton, cancelDeleteItemButton;

    @FXML private AnchorPane addFormPane;
    @FXML private TextField addNameField, addPhoneField, addCompanyField, addAddressField;
    @FXML private Button addSubmitBtn, addCancelBtn;

    @FXML private AnchorPane editFormPane;
    @FXML private TextField editNameField, editPhoneField, editCompanyField, editAddressField;
    @FXML private Button editSaveBtn, editCancelBtn;

    private final SupplierService svc = new SupplierService();
    private ObservableList<Supplier> masterList;
    private Supplier editingSupplier;
    private String user_role = SessionManager.getInstance().getUserData().get("roleID");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // If role = purchase manager
        if(user_role.equals("3")){
            addSupplierBtn.setVisible(false);
        } else {
            addActionsColumn();
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        colId.setStyle("-fx-alignment: CENTER;");
        colName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colName.setStyle("-fx-alignment: CENTER;");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colPhone.setStyle("-fx-alignment: CENTER;");
        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colCompany.setStyle("-fx-alignment: CENTER;");


        masterList = FXCollections.observableArrayList(svc.getAll());
        updateSupplierCount(masterList.size());
        table.setItems(masterList);

        setupFilters();
        setupFormValidation();
        setupListeners();

        // Add listener for masterList changes
        masterList.addListener((ListChangeListener<Supplier>) change -> {
            while (change.next()) {
                updateSupplierCount(masterList.size());
            }
        });
    }

    private void setupFilters() {
        if (cmbSort != null) {
            cmbSort.getItems().setAll("Name (A-Z)", "Name (Z-A)", "Company (A-Z)", "Company (Z-A)");
            cmbSort.getSelectionModel().selectFirst();
            cmbSort.setOnAction(e -> applySort());
        }
    }

    private void setupListeners() {
        if (addCancelBtn != null) {
            addCancelBtn.setOnAction(e -> hideAddForm());
        }
        if (editCancelBtn != null) {
            editCancelBtn.setOnAction(e -> hideEditForm());
        }
        if (clearSearchButton != null) {
            clearSearchButton.setOnAction(e -> onClear());
        }
    }

    /** Show the "add" panel with animation */
    @FXML private void showAddForm() {
        showPane(addFormPane);
        addNameField.clear();
        addPhoneField.clear();
        addCompanyField.clear();
        addAddressField.clear();
    }

    @FXML
    public void onClear() {
        this.txtSearch.clear();
        this.table.setItems(this.masterList);
        this.table.refresh();
    }

    /** Hide the "add" panel with animation */
    @FXML private void hideAddForm() {  
        hidePane(addFormPane);
    }

    /** Show the "edit" panel with animation */
    private void showEditForm(Supplier s) {
        editingSupplier = s;
        editNameField.setText(s.getSupplierName());
        editPhoneField.setText(s.getPhoneNumber());
        editCompanyField.setText(s.getCompanyName());
        editAddressField.setText(s.getAddress());
        showPane(editFormPane);
    }

    /** Hide the "edit" panel with animation */
    @FXML private void hideEditForm() {
        hidePane(editFormPane);
    }

    /** Search button */
    @FXML private void onSearch() {
        String txt = txtSearch.getText().toLowerCase().trim();
        table.setItems(
                txt.isEmpty()
                        ? masterList
                        : masterList.filtered(s ->
                            s.getSupplierName().toLowerCase().contains(txt) ||
                            s.getCompanyName().toLowerCase().contains(txt) ||
                            s.getPhoneNumber().contains(txt))
        );
        updateSupplierCount(table.getItems().size());
    }

    /** Sort handler */
    private void applySort() {
        String choice = cmbSort.getValue();
        if (choice == null) return;

        FXCollections.sort(table.getItems(), (a, b) -> switch (choice) {
	        case "Name (A-Z)" -> a.getSupplierName().compareToIgnoreCase(b.getSupplierName());
	        case "Name (Z-A)" -> b.getSupplierName().compareToIgnoreCase(a.getSupplierName());
	        case "Company (A-Z)" -> a.getCompanyName().compareToIgnoreCase(b.getCompanyName());
	        case "Company (Z-A)" -> b.getCompanyName().compareToIgnoreCase(a.getCompanyName());
	        default -> 0;
        });
        table.refresh();
    }

    /** Refresh table data */
    private void refreshTable() {
        Platform.runLater(() -> {
            masterList.setAll(svc.getAll());
            table.setItems(masterList);
            table.refresh();
            updateSupplierCount(masterList.size());
        });
    }

    /** Add "⋮" Actions column */
    private void addActionsColumn() {
        Callback<TableColumn<Supplier, Void>, TableCell<Supplier, Void>> cf = col -> new TableCell<>() {
            private final Button actionButton = new Button("⋮");
            {
                actionButton.getStyleClass().addAll("action-button-table");
                actionButton.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: #092165; -fx-background-radius: 4px; -fx-padding: 2px 10px;");
                actionButton.setCursor(javafx.scene.Cursor.HAND);

                actionButton.setOnAction(e -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Supplier s = getTableView().getItems().get(getIndex());
                        showContextMenu(s, actionButton);
                    }
                });

                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionButton);
                }
            }
        };

        colActions.setCellFactory(cf);
        colActions.setText("Actions");
    }

    /** Popup menu for edit/delete with red delete button */
    private void showContextMenu(Supplier s, Node anchor) {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        MenuItem del = new MenuItem("Delete");
        del.setStyle("-fx-text-fill: #ba0202;");

        // Add icons to menu items
        ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
        editIcon.setFitWidth(16);
        editIcon.setFitHeight(16);
        edit.setGraphic(editIcon);

        ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
        deleteIcon.setFitWidth(16);
        deleteIcon.setFitHeight(16);
        del.setGraphic(deleteIcon);

        // Edit action
        edit.setOnAction(e -> showEditForm(s));

        del.setOnAction(e -> {
            // Delete confirmation
            try {
                DeleteSupplierView.setSupplier(s);
                DeleteSupplierView deleteSupplierView = new DeleteSupplierView(this);
                deleteSupplierView.show();
                this.rootPane.setDisable(true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        menu.getItems().addAll(edit, del);
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    /** Slide in */
    private void showPane(AnchorPane pane) {
        pane.setVisible(true);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), pane);
        tt.setFromX(pane.getWidth());
        tt.setToX(0);
        tt.play();
    }

    /** Slide out */
    private void hidePane(AnchorPane pane) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), pane);
        tt.setFromX(0);
        tt.setToX(pane.getWidth());
        tt.setOnFinished(e -> pane.setVisible(false));
        tt.play();
    }

    public StackPane getRootPane() {
        return rootPane;
    }

    public TableView<Supplier> getTable() {
        return table;
    }

    public void updateSupplierCount(int count) {
        if (totalSupplierLabel != null) {
            Platform.runLater(() -> totalSupplierLabel.setText(String.format("Total Suppliers: %d", count)));
        }
    }

    private void setupFormValidation() {
        // Add Form Validation
        addNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                addNameField.setStyle("");
                validateAddForm();
                return;
            }

            if (!Validation.isValidName(newValue)) {
                addNameField.setStyle("-fx-border-color: red;");
                addSubmitBtn.setDisable(true);
                showNotification("Invalid name format", NotificationController.popUpType.error);
            } else {
                addNameField.setStyle("");
                validateAddForm();
            }
        });

        addPhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                addPhoneField.setStyle("");
                validateAddForm();
                return;
            }

            if (!Validation.isValidPhone(newValue)) {
                addPhoneField.setStyle("-fx-border-color: red;");
                addSubmitBtn.setDisable(true);
                showNotification("Invalid phone number format", NotificationController.popUpType.error);
            } else {
                addPhoneField.setStyle("");
                validateAddForm();
            }
        });

        addCompanyField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                addCompanyField.setStyle("");
                validateAddForm();
                return;
            }

            if (!Validation.isValidName(newValue)) {
                addCompanyField.setStyle("-fx-border-color: red;");
                addSubmitBtn.setDisable(true);
                showNotification("Company name cannot be empty", NotificationController.popUpType.error);
            } else {
                addCompanyField.setStyle("");
                validateAddForm();
            }
        });

        addAddressField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                addAddressField.setStyle("");
                validateAddForm();
                return;
            }

            if (!Validation.isValidName(newValue)) {
                addAddressField.setStyle("-fx-border-color: red;");
                addSubmitBtn.setDisable(true);
                showNotification("Invalid address format", NotificationController.popUpType.error);
            }
            addAddressField.setStyle("");
            validateAddForm();
        });

        // Edit Form Validation
        editNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Validation.isValidName(newValue)) {
                editNameField.setStyle("-fx-border-color: red;");
                editSaveBtn.setDisable(true);
                showNotification("Invalid name format", NotificationController.popUpType.error);
            } else {
                editNameField.setStyle("");
                validateEditForm();
            }
        });

        editPhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Validation.isValidPhone(newValue)) {
                editPhoneField.setStyle("-fx-border-color: red;");
                editSaveBtn.setDisable(true);
                showNotification("Invalid phone number format", NotificationController.popUpType.error);
            } else {
                editPhoneField.setStyle("");
                validateEditForm();
            }
        });

        editCompanyField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                editCompanyField.setStyle("-fx-border-color: red;");
                editSaveBtn.setDisable(true);
                showNotification("Company name cannot be empty", NotificationController.popUpType.error);
            } else {
                editCompanyField.setStyle("");
                validateEditForm();
            }
        });

        editAddressField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Validation.isValidName(newValue)) {
                editAddressField.setStyle("-fx-border-color: red;");
                editSaveBtn.setDisable(true);
                showNotification("Invalid address format", NotificationController.popUpType.error);
            } else {
                editAddressField.setStyle("");
                validateEditForm();
            }
        });
    }

    private void validateAddForm() {
        boolean isValid = Validation.isValidName(addNameField.getText()) &&
                         Validation.isValidPhone(addPhoneField.getText()) &&
                         !addCompanyField.getText().trim().isEmpty() && !addAddressField.getText().trim().isEmpty();
        addSubmitBtn.setDisable(!isValid);
    }

    private void validateEditForm() {
        boolean isValid = Validation.isValidName(editNameField.getText()) &&
                         Validation.isValidPhone(editPhoneField.getText()) &&
                         !editCompanyField.getText().trim().isEmpty() && !editAddressField.getText().trim().isEmpty();
        editSaveBtn.setDisable(!isValid);
    }

    @FXML public void onSaveAddSupplierButtonClick() {
        try {
            // Validate form fields
            if (addNameField.getText().trim().isEmpty()) {
                showNotification("Supplier name cannot be empty", NotificationController.popUpType.error);
                return;
            }
            if (addPhoneField.getText().trim().isEmpty()) {
                showNotification("Phone number cannot be empty", NotificationController.popUpType.error);
                return;
            }
            if (addCompanyField.getText().trim().isEmpty()) {
                showNotification("Company name cannot be empty", NotificationController.popUpType.error);
                return;
            }

            // Check for duplicate supplier
            if (isDuplicateSupplier(addNameField.getText(), addCompanyField.getText())) {
                showNotification("Supplier with same name and company already exists", NotificationController.popUpType.error);
                return;
            }

            boolean success = svc.add(
                addNameField.getText(),
                addCompanyField.getText(),
                addPhoneField.getText(),
                addAddressField.getText()
            );
            if (success) {
                refreshTable();
                hideAddForm();
                showNotification("Supplier added successfully", NotificationController.popUpType.success);
            } else {
                showNotification("Failed to add supplier", NotificationController.popUpType.error);
            }
        } catch (Exception e) {
            showNotification("Error: " + e.getMessage(), NotificationController.popUpType.error);
        }
    }

    @FXML public void onSaveEditButtonClick() {
        try {
            // Validate form fields
            if (editNameField.getText().trim().isEmpty()) {
                showNotification("Supplier name cannot be empty", NotificationController.popUpType.error);
                return;
            }
            if (editPhoneField.getText().trim().isEmpty()) {
                showNotification("Phone number cannot be empty", NotificationController.popUpType.error);
                return;
            }
            if (editCompanyField.getText().trim().isEmpty()) {
                showNotification("Company name cannot be empty", NotificationController.popUpType.error);
                return;
            }

            // Check for duplicate supplier excluding current one
            if (isDuplicateSupplierExcludingCurrent(editingSupplier.getSupplierId(),
                editNameField.getText(), editCompanyField.getText())) {
                showNotification("Supplier with same name and company already exists", NotificationController.popUpType.error);
                return;
            }

            boolean success = svc.update(
                editingSupplier.getSupplierId(),
                editNameField.getText(),
                editCompanyField.getText(),
                editPhoneField.getText(),
                editAddressField.getText()
            );
            if (success) {
                refreshTable();
                hideEditForm();
                showNotification("Supplier updated successfully", NotificationController.popUpType.success);
            } else {
                showNotification("Failed to update supplier", NotificationController.popUpType.error);
            }
        } catch (Exception e) {
            showNotification("Error: " + e.getMessage(), NotificationController.popUpType.error);
        }
    }

    private void showNotification(String message, NotificationController.popUpType type) {
        try {
            NotificationView notificationView = new NotificationView(message, type, NotificationController.popUpPos.TOP);
            notificationView.show();
        } catch (IOException ex) {
            System.err.println("Failed to show notification: " + ex.getMessage());
        }
    }

    private boolean isDuplicateSupplier(String name, String company) {
        return masterList.stream().anyMatch(s ->
            s.getSupplierName().equalsIgnoreCase(name) &&
            s.getCompanyName().equalsIgnoreCase(company));
    }

    private boolean isDuplicateSupplierExcludingCurrent(String currentId, String name, String company) {
        return masterList.stream().anyMatch(s ->
            !s.getSupplierId().equals(currentId) &&
            s.getSupplierName().equalsIgnoreCase(name) &&
            s.getCompanyName().equalsIgnoreCase(company));
    }
}
