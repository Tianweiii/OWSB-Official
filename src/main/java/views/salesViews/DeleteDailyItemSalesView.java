package views.salesViews;

import controllers.NotificationController;
import controllers.SidebarController;
import controllers.salesController.DailyItemSalesController;
import controllers.salesController.DeleteDailyItemSalesController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.Datas.DailySalesHistory;
import models.Datas.Transaction;
import models.Utils.Helper;
import org.start.owsb.Layout;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * View class for delete confirmation dialogs.
 * Supports both single transaction deletion and full day sales deletion.
 */
public class DeleteDailyItemSalesView {
    private Pane deletePane;
    private Stage stage;
    private final DeleteDailyItemSalesController controller;
    private static DailyItemSalesController rootController;

    public DeleteDailyItemSalesView(DailyItemSalesController rootController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalesManager/DeleteDailyItemSales.fxml"));
        this.deletePane = loader.load();
        controller = loader.getController();

        this.rootController = rootController;
    }

    /**
     * Show confirmation dialog for deleting a single transaction.
     * @param transaction Transaction to delete
     * @param onSuccess Callback after deletion success
     */
    public void showSingleTransactionDeletionDialog(Transaction transaction, Consumer<Void> onSuccess) {
        controller.showSingleDelete(transaction, onSuccess);
        stage.showAndWait();
    }

    /**
     * Show confirmation dialog for deleting all sales in a day.
     * @param history Daily sales history to delete
     * @param onSuccess Callback after deletion success
     */
    public void showDailySalesDeletionDialog(DailySalesHistory history, Consumer<Void> onSuccess) {
        controller.showDailyDelete(history, onSuccess);
        stage.showAndWait();
    }

    public void show() {
        Layout layout = Layout.getInstance();
        BorderPane root = layout.getRoot();
        root.getChildren().add(this.deletePane);

        Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), this.deletePane);

        rootController.getRootPane().setDisable(true);
        SidebarController.getSidebar().setDisable(true);

    }

    public static DailyItemSalesController getRootController() {
        return rootController;
    }

    public DeleteDailyItemSalesController getDeleteDailyItemSalesController() {
        return this.controller;
    }

    public Pane getDeleteDialogPane() {
        return this.deletePane;
    }
}
