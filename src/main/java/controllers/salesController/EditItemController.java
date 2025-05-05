package controllers.salesController;

import controllers.CustomTableViewController;
import controllers.NotificationController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import models.Datas.Item;
import models.Datas.Supplier;
import models.Utils.QueryBuilder;
import views.NotificationView;
import views.salesViews.EditItemView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class EditItemController extends CustomTableViewController implements Initializable {
	@FXML
	private Pane editItemPane;
	@FXML private TextField editItemNameField = new TextField();
	@FXML private Button saveEditItemButton;
	@FXML private Button cancelEditItemButton;

	@FXML
	public void onSaveEditItemButtonClick() {
		HashMap<String, String> data = EditItemView.getData();
		HashMap<String, String> dataToUpdate = new HashMap<>();

		dataToUpdate.put("item_name", editItemNameField.getText());
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			boolean res = qb.target("db/Item.txt").update(data.get("item_id"), dataToUpdate);

			if (res) {
				ArrayList<HashMap<String, String>> newData = qb
						.select(new String[]{"item_id", "item_name", "supplier_name", "created_at", "updated_at", "supplier_id"})
						.from("db/Item.txt")
						.joins(Supplier.class, "supplier_id")
						.get();

				Platform.runLater(() -> {
					EditItemView.getRootController().getTableView().getItems().setAll(newData);
					EditItemView.getRootController().getTableView().refresh();
					CustomTableViewController.getCommand().closeEditModal();
				});

				NotificationView notificationView = new NotificationView("Item updated successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
				CustomTableViewController.getCommand().closeEditModal();
			} else {
				NotificationView notificationView = new NotificationView("Failed to update item", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
			}
		} catch (Exception e) {
			System.out.println("not working" + e.getMessage());
		}
	}

	@FXML
	public void onCancelEditItemButtonClick() {
		CustomTableViewController.getCommand().closeEditModal();
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}
}
