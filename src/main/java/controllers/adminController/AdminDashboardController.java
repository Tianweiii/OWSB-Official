package controllers.adminController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import models.DTO.ItemListDTO;
import models.Datas.Item;
import models.Datas.Role;
import models.Datas.Supplier;
import models.Users.User;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import views.adminViews.ItemInfoPaneView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {
	@FXML private VBox totalUserCard;
	@FXML private Label userCount;

	@FXML private VBox recentUsersCard;
	@FXML private ListView<User> recentUserList;

	@FXML private VBox lowStockItemCard;
	@FXML private ListView<Item> lowStockItemList;

	@FXML private BorderPane itemInfoPane;
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.setUpListViews();
		this.setUpCards();
		
		try {
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			ArrayList<HashMap<String, String>> userData = qb
					.select()
					.from("db/User.txt")
					.joins(Role.class, "roleID")
					.get();

			List<User> recentUsers = qb
					.select()
					.from("db/User.txt")
					.getAsObjects()
					.subList(userData.size()-3, userData.size());

			this.userCount.setText(String.valueOf(userData.size()));
			this.recentUserList.getItems().addAll(recentUsers);

			QueryBuilder<Item> itemQb = new QueryBuilder<>(Item.class);

			ArrayList<Item> items = itemQb
					.select()
					.from("db/Item.txt")
					.getAsObjects()
					.stream()
					.filter(item -> item.getQuantity() < item.getAlertSetting())
					.collect(Collectors.toCollection(ArrayList::new));

			this.lowStockItemList.getItems().addAll(items);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void setUpListViews() {
		this.recentUserList.setCellFactory(user -> new ListCell<>() {
			@Override
			protected void updateItem(User user, boolean empty) {
				super.updateItem(user, empty);

				if (empty || user == null || user.getName() == null) {
					setText(null);
				} else {
					setText(user.getName());
				}
			}
		});

		this.lowStockItemList.setCellFactory(item -> new ListCell<>() {
			@Override
			protected void updateItem(Item item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null || item.getItemName() == null) {
					setText(null);
				} else {
					setText(item.getItemName());
				}
			}
		});

		this.lowStockItemList.setOnMouseClicked(mouseEvent -> {
			Item selectedItem = this.lowStockItemList.getFocusModel().getFocusedItem();
			ItemInfoPaneView itemInfoPaneView = new ItemInfoPaneView(this.getItemDTO(selectedItem.getItemID()), this);
			this.itemInfoPane.setCenter(itemInfoPaneView.getView());
		});
	}

	private void setUpCards() {
		Navigator navigator = Navigator.getInstance();
		this.totalUserCard.setOnMouseClicked(e -> navigator.navigate(navigator.getRouters("admin").getRoute("user-list")));
		this.recentUsersCard.setOnMouseClicked(e -> navigator.navigate(navigator.getRouters("admin").getRoute("user-list")));

		this.lowStockItemCard.setOnMouseClicked(e -> navigator.navigate(navigator.getRouters("inventory").getRoute("stockManagement")));
	}

	private ItemListDTO getItemDTO(String id) {
		try {
			QueryBuilder<Item> itemQb = new QueryBuilder<>(Item.class);
			ArrayList<HashMap<String, String>> item = itemQb
					.select()
					.from("db/Item.txt")
					.where("itemID", "=", id)
					.joins(Supplier.class, "supplierID")
					.get();
			if (item.isEmpty()) {
				return null;
			}
			HashMap<String, String> selectedItemInfo = item.get(0);
			return new ItemListDTO(
					selectedItemInfo.get("itemID"),
					selectedItemInfo.get("itemName"),
					selectedItemInfo.get("description"),
					Item.formatDateTime(selectedItemInfo.get("createdAt")),
					Item.formatDateTime(selectedItemInfo.get("updatedAt")),
					Integer.parseInt(selectedItemInfo.get("alertSetting")),
					Integer.parseInt(selectedItemInfo.get("quantity")),
					Double.parseDouble(selectedItemInfo.get("unitPrice")),
					selectedItemInfo.get("supplierID"),
					selectedItemInfo.get("supplierName")
					);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public BorderPane getItemInfoPane() {
		return itemInfoPane;
	}
}
