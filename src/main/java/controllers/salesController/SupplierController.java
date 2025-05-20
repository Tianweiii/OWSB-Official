package controllers.salesController;

import controllers.NotificationController;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.Callback;
import models.Datas.Supplier;
import org.start.owsb.Layout;
import service.SupplierService;
import views.NotificationView;
import views.salesViews.DeleteConfirmationView;
import views.salesViews.DeleteSupplierView;
import views.salesViews.SupplierView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TableView<Supplier> table;
    @FXML private TableColumn<Supplier, String> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colCompany;
    @FXML private TableColumn<Supplier, Void> colActions;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbSort;

    @FXML private Button deleteButton;
    @FXML private Button cancelDeleteItemButton;

    @FXML private AnchorPane addFormPane;
    @FXML private TextField addNameField, addPhoneField, addCompanyField, addAddressField;
    @FXML private Button addSubmitBtn, addCancelBtn;

    @FXML private AnchorPane editFormPane;
    @FXML private TextField editNameField, editPhoneField, editCompanyField, editAddressField;
    @FXML private Button editSaveBtn, editCancelBtn;

    private final SupplierService svc = new SupplierService();
    private ObservableList<Supplier> masterList;
    private Supplier editingSupplier;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        colId.setStyle("-fx-alignment: CENTER;");
        colName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colName.setStyle("-fx-alignment: CENTER;");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colPhone.setStyle("-fx-alignment: CENTER;");
        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colCompany.setStyle("-fx-alignment: CENTER;");
        addActionsColumn();

        masterList = FXCollections.observableArrayList(svc.getAll());
        table.setItems(masterList);

        cmbSort.getItems().setAll("Alphabetical Ascending", "Alphabetical Descending");
        cmbSort.setOnAction(e -> applySort());

        addCancelBtn.setOnAction(e -> hideAddForm());
        addSubmitBtn.setOnAction(e -> {
            svc.add(
                    addNameField.getText().trim(),
                    addCompanyField.getText().trim(),
                    addPhoneField.getText().trim(),
                    addAddressField.getText().trim()
            );
            hideAddForm();
            refreshTable();
            try {
                NotificationView notificationView = new NotificationView("Supplier created successfully", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notificationView.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        editCancelBtn.setOnAction(e -> hideEditForm());
        editSaveBtn.setOnAction(e -> {
            svc.update(
                    editingSupplier.getSupplierId(),
                    editNameField.getText(),
                    editCompanyField.getText(),
                    editPhoneField.getText(),
                    editAddressField.getText()
            );
            hideEditForm();
            refreshTable();
            try {
                NotificationView notificationView = new NotificationView("Supplier updated", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notificationView.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /** Show the “add” panel with animation */
    @FXML private void showAddForm() {
        showPane(addFormPane);
        addNameField.clear();
        addPhoneField.clear();
        addCompanyField.clear();
        addAddressField.clear();
    }

    /** Hide the “add” panel with animation */
    @FXML private void hideAddForm() {
        hidePane(addFormPane);
    }

    /** Show the “edit” panel with animation */
    private void showEditForm(Supplier s) {
        editingSupplier = s;
        editNameField.setText(s.getSupplierName());
        editPhoneField.setText(s.getPhoneNumber());
        editCompanyField.setText(s.getCompanyName());
        editAddressField.setText(s.getAddress());
        showPane(editFormPane);
    }

    /** Hide the “edit” panel with animation */
    @FXML private void hideEditForm() {
        hidePane(editFormPane);
    }

    /** Search button */
    @FXML private void onSearch() {
        String txt = txtSearch.getText().toLowerCase().trim();
        table.setItems(
                txt.isEmpty()
                        ? masterList
                        : masterList.filtered(s -> s.getSupplierName().toLowerCase().contains(txt))
        );
    }

    /** Sort handler */
    private void applySort() {
        String choice = cmbSort.getValue();
        FXCollections.sort(table.getItems(), (a, b) -> {
            int cmp = a.getSupplierName().compareToIgnoreCase(b.getSupplierName());
            return "Alphabetical Descending".equals(choice) ? -cmp : cmp;
        });
    }

    /** Refresh table data */
    private void refreshTable() {
        masterList.setAll(svc.getAll());
        table.setItems(masterList);
        table.refresh();
    }

    /** Add "⋮" Actions column */
    private void addActionsColumn() {
        Callback<TableColumn<Supplier, Void>, TableCell<Supplier, Void>> cf = col -> new TableCell<>() {
            final HBox hBox = new HBox();
            final Button btn = new Button("⋮");
            {
                btn.getStyleClass().add("action-button");
                btn.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        Supplier s = getTableView().getItems().get(getIndex());
                        showContextMenu(s, btn);
                    }
                });
                setPrefHeight(Region.USE_COMPUTED_SIZE);
                hBox.getChildren().add(btn);
                setGraphic(hBox);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }

        };

        colActions.setCellFactory(cf);
        colActions.setText("Actions");
    }

    /** Popup menu for edit/delete with red delete button */
    private void showContextMenu(Supplier s, Button anchor) {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        MenuItem del = new MenuItem("Delete");
        del.setStyle("-fx-text-fill:red;");

        // Edit action
        edit.setOnAction(e -> showEditForm(s));

        del.setOnAction(e -> {
            // Delete confirmation
            try {
                DeleteSupplierView deleteSupplierView = new DeleteSupplierView(this);
                DeleteSupplierView.setSupplier(s);
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
}
