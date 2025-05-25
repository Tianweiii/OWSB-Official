package views.salesViews;

import controllers.NotificationController;
import controllers.salesController.ItemListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import models.Utils.Helper;
import org.start.owsb.Layout;
import views.View;

import java.io.IOException;
import java.net.URL;

public class AddItemView implements View {
	private final Pane addItemPane;
	private static ItemListController rootController;
	private final ItemListController itemListController;

	public AddItemView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/Components/AddItem.fxml"));
		this.addItemPane = loader.load();
		this.itemListController = loader.getController();
	}

	public void showAddItemView(ItemListController rootController){
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().add(addItemPane);
		Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, root, addItemPane);

		this.itemListController.setupFormValidation("add");
		AddItemView.rootController = rootController;

	}

	public static ItemListController getRootController() {
		return rootController;
	}

	@Override
	public Parent getView() {
		return this.addItemPane;
	}
}
