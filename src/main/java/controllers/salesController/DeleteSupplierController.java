package controllers.salesController;

import controllers.SidebarController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import models.Datas.Supplier;
import models.Utils.QueryBuilder;
import org.start.owsb.Layout;
import service.SupplierService;
import views.NotificationView;
import views.salesViews.DeleteSupplierView;

import java.net.URL;
import java.util.ResourceBundle;

public class DeleteSupplierController implements Initializable {
    @FXML private Pane rootPane;
    @FXML private Pane deleteSupplierPane;
    @FXML private Button deleteButton;
    @FXML private Button cancelDeleteButton;
    @FXML private Label deleteLabel;


    private final ObservableList<Supplier> masterList = FXCollections.observableArrayList();
    private final SupplierService svc = new SupplierService();
    @FXML
    public void onDeleteSupplierButtonClick() {
        Supplier supplier = DeleteSupplierView.getSupplier();
        svc.delete(supplier.getSupplierId());
        refreshTable();

        Layout layout = Layout.getInstance();
        BorderPane root = layout.getRoot();
        root.getChildren().remove(this.rootPane);

        SupplierController controller = DeleteSupplierView.getRootController();
        controller.getRootPane().setDisable(false);
        SidebarController.getSidebar().setDisable(false);
    }

    @FXML
    public void onCancelDeleteSupplierButtonClick() {
        Layout layout = Layout.getInstance();
        BorderPane root = layout.getRoot();
        root.getChildren().remove(this.rootPane);

        SupplierController controller = DeleteSupplierView.getRootController();
        controller.getRootPane().setDisable(false);
        SidebarController.getSidebar().setDisable(false);
    }

    private void refreshTable() {
        ObservableList<Supplier> newData = FXCollections.observableArrayList(svc.getAll());
        masterList.setAll(newData);
        DeleteSupplierView.getRootController().getTable().setItems(masterList);
        DeleteSupplierView.getRootController().getTable().refresh();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setDeleteLabel(String label) {
        deleteLabel.setText(label);
    }
}
