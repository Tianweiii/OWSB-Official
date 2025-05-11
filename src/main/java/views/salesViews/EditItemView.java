package views.salesViews;

import controllers.NotificationController;
import controllers.salesController.ItemListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import models.ModelInitializable;
import models.Utils.Helper;
import models.Utils.QueryBuilder;
import org.start.owsb.Layout;
import views.View;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class EditItemView implements View {
	private final Pane editItemPane;
	private static HashMap<String, String> data;
	private static ItemListController rootController;

	public EditItemView(ItemListController rootController) throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/SalesManager/Components/EditItem.fxml"));
		this.editItemPane = loader.load();
		EditItemView.rootController = rootController;
	}

	public static void setData(HashMap<String, String> data) {
		EditItemView.data = data;
	}

	public static HashMap<String, String> getData() {
		return EditItemView.data;
	}

	public void showEditItemPane() {
		Layout layout = Layout.getInstance();
		layout.getRoot().getChildren().add(editItemPane);
		Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), editItemPane);
	}

	public static ItemListController getRootController() {
		return EditItemView.rootController;
	}

	@Override
	public Parent getView() {
		return this.editItemPane;
	}
}
