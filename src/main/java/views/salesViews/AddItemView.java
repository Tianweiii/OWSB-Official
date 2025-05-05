package views.salesViews;

import controllers.CustomTableViewController;
import controllers.NotificationController;
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
	private static CustomTableViewController rootController;

	public AddItemView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/Components/AddItem.fxml"));
		this.addItemPane = loader.load();
	}

	public void showAddItemView(CustomTableViewController rootController){
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().add(addItemPane);
		Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, root, addItemPane);

		AddItemView.rootController = rootController;
		rootController.getTableView().setDisable(true);

	}

	public void hideAddItemView() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.addItemPane);
		AddItemView.rootController.getTableView().setDisable(false);
	}

	public static CustomTableViewController getRootController() {
		return rootController;
	}

	@Override
	public Parent getView() {
		return this.addItemPane;
	}
}
