package views.salesViews;

import controllers.NotificationController;
import controllers.salesController.SupplierController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import models.Datas.Supplier;
import models.Utils.Helper;
import org.start.owsb.Layout;
import views.View;

import java.io.IOException;
import java.net.URL;

public class DeleteSupplierView implements View {
    private final Pane deletePane;
    private static SupplierController controller;
    private static Supplier supplier;

    public DeleteSupplierView(SupplierController controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/DeleteSupplier.fxml"));
        this.deletePane = loader.load();
        DeleteSupplierView.controller = controller;
    }

    public static SupplierController getRootController() {
        return DeleteSupplierView.controller;
    }

    public void show() {
        Layout layout = Layout.getInstance();
        BorderPane root = layout.getRoot();
        root.getChildren().add(this.deletePane);

        Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), this.deletePane);
    }



    @Override
    public Parent getView() {
        return this.deletePane;
    }

    public static Supplier getSupplier() {
        return DeleteSupplierView.supplier;
    }

    public static void setSupplier(Supplier supplier) {
        DeleteSupplierView.supplier = supplier;
    }
}


