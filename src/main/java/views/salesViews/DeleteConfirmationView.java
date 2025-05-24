package views.salesViews;

import controllers.NotificationController;
import controllers.salesController.ItemListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.start.owsb.Layout;
import views.View;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class DeleteConfirmationView implements View {
	private final Pane deletePane;
	private static HashMap<String, String> data;
	private static ItemListController rootController;
	private final ItemListController itemListController;

	public DeleteConfirmationView(ItemListController rootController) throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/Components/DeleteConfirmationWindow.fxml"));
		this.deletePane = loader.load();
		this.itemListController = loader.getController();

		DeleteConfirmationView.rootController = rootController;
		rootController.setItemToBeDeleted(data.get("itemName"));
	}

	public static ItemListController getRootController() {
		return rootController;
	}

	public void showDeleteConfirmationView() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();

		itemListController.setItemToBeDeleted(data.get("itemName"));

		root.getChildren().add(deletePane);
		models.Utils.Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, root, deletePane);
	}

	@Override
	public Parent getView() {
		return deletePane;
	}

	public static void setData(HashMap<String, String> data) {
		DeleteConfirmationView.data = data;
	}

	public static HashMap<String, String> getData() {
		return DeleteConfirmationView.data;
	}
}
