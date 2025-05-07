package controllers.salesController;

import controllers.CustomTableViewController;
import controllers.NotificationController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import models.Datas.Item;
import models.Utils.QueryBuilder;
import views.NotificationView;
import views.salesViews.DeleteConfirmationView;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DeleteConfirmationController extends CustomTableViewController implements Initializable {
	@FXML private Pane deleteItemPane;
	@FXML private Button deleteButton;
	@FXML private Button cancelDeleteItemButton;
	@FXML private Label itemToBeDeleted = new Label();

	@FXML
	public void onDeleteItemButtonClick() {
		HashMap<String, String> dataToDelete = DeleteConfirmationView.getData();

		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			boolean res = qb.target("db/Item.txt").delete(dataToDelete.get("itemID"));
			if (res) {

				Platform.runLater(() -> {
					DeleteConfirmationView.getRootController().getTableView().getItems().remove(dataToDelete);
					DeleteConfirmationView.getRootController().getTableView().refresh();
				});

				NotificationView notificationView = new NotificationView("Item deleted successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();

				CustomTableViewController.getCommand().closeDeleteModal();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@FXML
	public void onCancelDeleteItemButtonClick() {
		CustomTableViewController.getCommand().closeDeleteModal();
	}
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.itemToBeDeleted.setText(DeleteConfirmationView.getData().get("itemName"));
	}
}
