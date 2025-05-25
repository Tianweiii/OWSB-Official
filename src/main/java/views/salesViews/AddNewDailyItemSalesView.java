package views.salesViews;

import controllers.NotificationController;
import controllers.salesController.AddNewDailyItemSalesController;
import controllers.salesController.DailyItemSalesController;
import controllers.salesController.MissingDailySalesController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import models.Utils.Helper;
import org.start.owsb.Layout;
import views.View;

import java.net.URL;
import java.time.LocalDate;

public class AddNewDailyItemSalesView implements View {
	private final Pane addNewDailyItemSalesPane;
	private static MissingDailySalesController missingController;
	private static DailyItemSalesController rootController;
	private final AddNewDailyItemSalesController addNewDailyItemSalesController;
	private final Mode mode;

    public AddNewDailyItemSalesController getAddNewDailyItemSalesController() {
        return addNewDailyItemSalesController;
    }

    public enum Mode {
		FIRST,
		NEW,
		UPDATE
	}

	public AddNewDailyItemSalesView(MissingDailySalesController rootController, Mode mode) throws Exception {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/AddNewDailyItemSales.fxml"));
		this.addNewDailyItemSalesPane = loader.load();
		AddNewDailyItemSalesView.missingController = rootController;

		AddNewDailyItemSalesController controller = loader.getController();
		controller.initMode(mode, null, LocalDate.now());
		this.addNewDailyItemSalesController = controller;
		this.mode = mode;
	}

	public AddNewDailyItemSalesView(DailyItemSalesController rootController, Mode mode) throws Exception {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/AddNewDailyItemSales.fxml"));
		this.addNewDailyItemSalesPane = loader.load();
		AddNewDailyItemSalesView.rootController = rootController;

		AddNewDailyItemSalesController controller = loader.getController();
		controller.initMode(mode, null, LocalDate.now());
		this.addNewDailyItemSalesController = controller;
		this.mode = mode;
	}

	public void show() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().add(addNewDailyItemSalesPane);
		Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, root, addNewDailyItemSalesPane);

		if (missingController != null && mode == Mode.FIRST) {
			missingController.getRootPane().setDisable(true);
		} else {
			rootController.getRootPane().setDisable(true);
		}
	}

	public static MissingDailySalesController getMissingController() {
		return AddNewDailyItemSalesView.missingController;
	}

	public static DailyItemSalesController getRootController() {
		return AddNewDailyItemSalesView.rootController;
	}

	@Override
	public Parent getView() {
		return null;
	}
}
