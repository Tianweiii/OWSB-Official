package controllers.salesController;

import controllers.CustomTableViewController;
import controllers.NotificationController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import models.Datas.Item;
import models.Datas.Supplier;
import models.Utils.QueryBuilder;
import views.NotificationView;
import views.salesViews.AddItemView;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddItemController extends CustomTableViewController implements Initializable {
	@FXML private ChoiceBox<Supplier> supplierChoiceBox;
	@FXML private Button saveAddItemButton;
	@FXML private Button cancelAddItemButton;
	@FXML private TextField addItemNameField;

	@FXML
	public void onCancelAddItemButtonClicked(){
		CustomTableViewController.getCommand().closeModal();
	}

	@FXML
	public void onSaveAddItemButtonClicked() throws IOException {
		String itemName = this.addItemNameField.getText();
		Supplier supplier = this.supplierChoiceBox.getValue();

		try {
			NotificationView notificationView;
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			ArrayList<HashMap<String, String>> data = qb.select(new String[]{"item_name", "supplier_id"}).from("db/Item.txt").get();

			for (HashMap<String, String> item: data) {
				if (item.get("item_name").equals(itemName) && item.get("supplier_id").equals(String.valueOf(supplier.getSupplierId()))) {
					notificationView = new NotificationView("Item already exists", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
					notificationView.show();
					return;
				}
			}
			boolean res = qb.target("db/Item.txt").values(new String[]{
					itemName,
					LocalDate.now().format(DateTimeFormatter.ISO_DATE),
					LocalDate.now().format(DateTimeFormatter.ISO_DATE),
					"0",
					"100",
					String.valueOf(supplier.getSupplierId())
			}).create();

			if (res) {
				ArrayList<HashMap<String, String>> newData = qb
						.select(new String[]{"item_id", "item_name", "supplier_name", "created_at", "updated_at", "supplier_id"})
						.from("db/Item.txt")
						.joins(Supplier.class, "supplier_id")
						.get();
				ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
				oListItems.addAll(newData);

				Platform.runLater(() -> {
					TableView<HashMap<String, String>> itemTable = AddItemView.getRootController().getTableView();
					itemTable.getItems().clear();
					itemTable.setItems(oListItems);
					itemTable.refresh();

				});
				notificationView = new NotificationView("Item added successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
			} else {
				notificationView = new NotificationView("Item already exists", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			}
			notificationView.show();
			CustomTableViewController.getCommand().closeModal();

		}catch (Exception e) {
			NotificationView notificationView = new NotificationView(e.getMessage(), NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			notificationView.show();
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
			ArrayList<Supplier> suppliers = qb.select().from("db/Supplier.txt").getAsObjects();
			ObservableList<Supplier> supplierList = FXCollections.observableArrayList(suppliers);

			StringConverter<Supplier> supplierConverter = new StringConverter<>() {
				@Override
				public String toString(Supplier supplier) {
					if (supplier == null) {
						return "None";
					}
					return supplier.getSupplierName();
				}

				@Override
				public Supplier fromString(String s) {
					return null;
				}
			};
			this.supplierChoiceBox.setConverter(supplierConverter);
			this.supplierChoiceBox.getItems().addAll(supplierList);

			if (!supplierChoiceBox.getItems().isEmpty()) {
				supplierChoiceBox.setValue(supplierChoiceBox.getItems().get(0));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
