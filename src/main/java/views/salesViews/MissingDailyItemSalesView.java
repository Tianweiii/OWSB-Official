package views.salesViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import views.View;

import java.net.URL;

public class MissingDailyItemSalesView implements View {
	private final Pane missingDailyItemSalesPane;

	public MissingDailyItemSalesView() throws Exception {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/Components/EmptyDailySales.fxml"));
		this.missingDailyItemSalesPane = loader.load();
	}

	@Override
	public Parent getView() {
		return this.missingDailyItemSalesPane;
	}
}
