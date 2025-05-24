package views.salesViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import views.View;

import java.io.IOException;
import java.net.URL;

public class DailyItemSalesView implements View {
	private final BorderPane dailyItemSalesPane;

	public DailyItemSalesView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/DailyItemSales.fxml"));
		this.dailyItemSalesPane = loader.load();
	}

	@Override
	public Parent getView() {
		return this.dailyItemSalesPane;
	}
}
