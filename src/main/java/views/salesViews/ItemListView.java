package views.salesViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;
import java.net.URL;

public class ItemListView implements View {
	private final AnchorPane itemListPane;

	public ItemListView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/ItemList.fxml"));
		this.itemListPane = loader.load();
	}

	@Override
	public Parent getView() {
		return this.itemListPane;
	}
}
