package views.salesViews;

import controllers.NotificationController;
import controllers.SidebarController;
import controllers.salesController.DailyItemSalesController;
import controllers.salesController.DeleteDailyItemSalesController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import models.Utils.Helper;
import org.start.owsb.Layout;

import java.io.IOException;

/**
 * View class for delete confirmation dialogs.
 * Supports both single transaction deletion and full day sales deletion.
 */
public class DeleteDailyItemSalesView {
    private final Pane deletePane;
    private final DeleteDailyItemSalesController controller;
    private static DailyItemSalesController rootController;

    public DeleteDailyItemSalesView(DailyItemSalesController rootController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalesManager/DeleteDailyItemSales.fxml"));
        this.deletePane = loader.load();
        controller = loader.getController();

        DeleteDailyItemSalesView.rootController = rootController;
    }

    public void show() {
        Layout layout = Layout.getInstance();
        BorderPane root = layout.getRoot();
        root.getChildren().add(this.deletePane);

        Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), this.deletePane);

        rootController.getRootPane().setDisable(true);
        SidebarController.getSidebar().setDisable(true);

    }

    public DeleteDailyItemSalesController getDeleteDailyItemSalesController() {
        return this.controller;
    }

    public Pane getDeleteDialogPane() {
        return this.deletePane;
    }
}
