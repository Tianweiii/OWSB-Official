package views.salesViews;

import controllers.CustomTableViewController;
import controllers.NotificationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import models.Utils.Helper;
import org.start.owsb.Layout;
import views.View;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class DeleteConfirmationView implements View {
	private final Pane deletePane;
	private static HashMap<String, String> data;
	private static CustomTableViewController rootController;

	public DeleteConfirmationView(CustomTableViewController rootController) throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/Components/DeleteConfirmationWindow.fxml"));
		this.deletePane = loader.load();
		DeleteConfirmationView.rootController = rootController;
	}

	public static CustomTableViewController getRootController() {
		return rootController;
	}

	public void showDeleteConfirmationView() {
		Layout layout = Layout.getInstance();
		layout.getRoot().getChildren().add(deletePane);
		Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), deletePane);

		DeleteConfirmationView.rootController.getTableView().setDisable(true);
	}

	public void hideDeleteConfirmationView() {
		Layout layout = Layout.getInstance();
		layout.getRoot().getChildren().remove(deletePane);

		DeleteConfirmationView.rootController.getTableView().setDisable(false);
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
