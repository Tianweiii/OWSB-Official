package views.salesViews;

import controllers.salesController.DailyItemSalesController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import views.View;

import java.net.URL;

public class MissingDailyItemSalesView implements View {
	private Pane missingDailyItemSalesPane;

	public MissingDailyItemSalesView() throws Exception {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/Components/EmptyDailySales.fxml"));
		this.missingDailyItemSalesPane = loader.load();
	}

	@Override
	public Parent getView() {
		return this.missingDailyItemSalesPane;
	}
}
