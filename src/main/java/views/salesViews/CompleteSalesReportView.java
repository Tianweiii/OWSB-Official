package views.salesViews;

import controllers.NotificationController;
import controllers.SidebarController;
import controllers.salesController.CompleteSalesReportController;
import controllers.salesController.DailyItemSalesController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import models.Utils.Helper;
import org.start.owsb.Layout;

import java.time.LocalDate;

public class CompleteSalesReportView {
    private final Pane completeSalesReportPane;
    private final CompleteSalesReportController completeSalesReportController;
    private final DailyItemSalesController parentController;

    public CompleteSalesReportView(DailyItemSalesController parentController) throws Exception {
        this.parentController = parentController;
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalesManager/CompleteSalesReport.fxml"));
        completeSalesReportPane = loader.load();
        completeSalesReportController = loader.getController();
    }

    public void show() {
        Layout layout = Layout.getInstance();
        layout.getRoot().getChildren().add(completeSalesReportPane);

        Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), completeSalesReportPane);
        
        // Disable parent panels
        parentController.getRootPane().setDisable(true);
        SidebarController.getSidebar().setDisable(true);
    }

    public void setSelectedDate(LocalDate date) {
        completeSalesReportController.setSelectedDate(date);
    }

    public CompleteSalesReportController getCompleteSalesReportController() {
        return completeSalesReportController;
    }

    public Pane getCompleteSalesReportPane() {
        return completeSalesReportPane;
    }
} 