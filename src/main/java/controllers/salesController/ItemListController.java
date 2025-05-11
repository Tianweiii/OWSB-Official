package controllers.salesController;

import controllers.NotificationController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import models.Datas.Item;
import models.Datas.Supplier;
import models.Utils.Helper;
import models.Utils.QueryBuilder;
import org.start.owsb.Layout;
import views.NotificationView;
import views.salesViews.AddItemView;
import views.salesViews.DeleteConfirmationView;
import views.salesViews.EditItemView;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ItemListController implements Initializable {
	// Item List Page
	private ItemListController instance = this;
	@FXML private AnchorPane rootPane;
	@FXML private Button addItemButton;
	@FXML private Button searchButton;
	@FXML private TextField sortByField;
	@FXML private TextField searchField;
	@FXML private TableView<HashMap<String, String>> itemTable = new TableView<>();

	// Add Item Popup
	@FXML
	Pane addItemPane;
	@FXML
	private TextField addItemNameField;
	@FXML private ChoiceBox<Supplier> supplierChoiceBox = new ChoiceBox<>();
	@FXML private Button saveAddItemButton;
	@FXML private Button cancelAddItemButton;

	// Delete Item Pane
	@FXML private Pane deleteItemPane;
	@FXML private Button deleteButton;
	@FXML private Button cancelDeleteItemButton;
	@FXML private Label itemToBeDeleted = new Label();

	// Edit Item Pane
	@FXML private Pane editItemPane;
	@FXML private TextField editItemNameField = new TextField();
	@FXML private Button saveEditItemButton;
	@FXML private Button cancelEditItemButton;

	@FXML
	public void onCancelEditItemButtonClick() throws IOException {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.editItemPane);

		ItemListController controller = EditItemView.getRootController();
		controller.getRootPane().setDisable(false);

	}

	@FXML
	public void onSaveEditItemButtonClick() {
		NotificationView notificationView;
		String changedItemName = this.editItemNameField.getText();
		String itemId = EditItemView.getData().get("itemID");
		String supplierId = EditItemView.getData().get("supplierID");

		HashMap<String, String> dataToUpdate = new HashMap<>();
		dataToUpdate.put("itemName", changedItemName);
		try {
			QueryBuilder<Item> checkerQb = new QueryBuilder<>(Item.class);
			ArrayList<HashMap<String, String>> existingData = checkerQb
					.select(new String[]{"itemName"})
					.from("db/Item.txt")
					.where("itemName", "=", changedItemName)
					.and("supplierID", "=", supplierId)
					.get();

			if (!existingData.isEmpty()) {
				notificationView = new NotificationView("Item already exists", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
				notificationView.show();
				return;
			}

			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			boolean res = qb.target("db/Item.txt").update(itemId, dataToUpdate);

			if (res) {
				ArrayList<HashMap<String, String>> newData = qb
						.select(new String[]{"itemID", "itemName", "supplierName", "createdAt", "updatedAt", "supplierID"})
						.from("db/Item.txt")
						.joins(Supplier.class, "supplierID")
						.get();
				ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
				oListItems.addAll(newData);

				Platform.runLater(() -> {
					Layout layout = Layout.getInstance();
					BorderPane root = layout.getRoot();
					root.getChildren().remove(this.editItemPane);
					ItemListController controller = EditItemView.getRootController();
					controller.getRootPane().setDisable(false);

					TableView<HashMap<String, String>> itemTable = controller.getItemTable();
					itemTable.getItems().clear();
					itemTable.setItems(oListItems);
					itemTable.refresh();

				});
				notificationView = new NotificationView("Item has been successfully changed", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
			}else {
				notificationView = new NotificationView("Item deletion failed", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			}
			notificationView.show();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@FXML public void onDeleteItemButtonClick() throws IOException {
		HashMap<String, String> oldData = DeleteConfirmationView.getData();
		String selectedId = oldData.get("itemID");
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			boolean res = qb.target("db/Item.txt").delete(selectedId);
			NotificationView notificationView;
			if (res) {
				notificationView = new NotificationView("Item deleted successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
				ItemListController controller = DeleteConfirmationView.getRootController();
				controller.getRootPane().setDisable(false);
				Platform.runLater(() -> {
					controller.getItemTable().getItems().remove(oldData);
					controller.getItemTable().refresh();
				});
			}else {
				notificationView = new NotificationView("Item deletion failed", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			}
			notificationView.show();

			Layout layout = Layout.getInstance();
			BorderPane root = layout.getRoot();
			root.getChildren().remove(this.deleteItemPane);

		}catch (Exception e) {
			NotificationView notificationView = new NotificationView(e.getMessage(), NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			notificationView.show();
		}

	}

	@FXML public void onCancelDeleteItemButtonClick() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.deleteItemPane);

		ItemListController controller = DeleteConfirmationView.getRootController();
		controller.getRootPane().setDisable(false);
	}

	@FXML public void onSaveAddItemButtonClick() throws IOException {
		String itemName = this.addItemNameField.getText();
		Supplier supplier = this.supplierChoiceBox.getValue();

		try {
			NotificationView notificationView;
			ItemListController controllerReference = AddItemView.getRootController();
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			ArrayList<HashMap<String, String>> data = qb.select(new String[]{"itemName", "supplierID"}).from("db/Item.txt").get();

			for (HashMap<String, String> item: data) {
				if (item.get("itemName").equals(itemName) && item.get("supplierID").equals(String.valueOf(supplier.getSupplierId()))) {
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
						.select(new String[]{"itemID", "itemName", "supplierName", "createdAt", "updatedAt", "supplierID"})
						.from("db/Item.txt")
						.joins(Supplier.class, "supplierID")
						.get();
				ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
				oListItems.addAll(newData);

				Platform.runLater(() -> {
					TableView<HashMap<String, String>> itemTable = controllerReference.getItemTable();
					itemTable.getItems().clear();
					itemTable.setItems(oListItems);
					itemTable.refresh();

				});
				notificationView = new NotificationView("Item added successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
			} else {
				notificationView = new NotificationView("Item already exists", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			}
			notificationView.show();

			Layout layout = Layout.getInstance();
			BorderPane root = layout.getRoot();
			root.getChildren().remove(this.addItemPane);

			controllerReference.getRootPane().setDisable(false);
		}catch (Exception e) {
			NotificationView notificationView = new NotificationView(e.getMessage(), NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			notificationView.show();
		}
	}

	@FXML public void onCancelAddItemButtonClick() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.addItemPane);

		ItemListController controller = AddItemView.getRootController();
		controller.getRootPane().setDisable(false);
	}

	@FXML
	public void handleAddItemButtonClick() throws IOException {
		AddItemView addItemView = new AddItemView();
		addItemView.showAddItemView(this);

		this.rootPane.setDisable(true);
	}

	public TableView<HashMap<String, String>> getItemTable() {
		return this.itemTable;
	}

	public AnchorPane getRootPane() {
		return this.rootPane;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList(qb
					.select(new String[]{"itemID", "itemName", "supplierName", "createdAt", "updatedAt", "supplierID"})
					.from("db/Item.txt")
					.joins(Supplier.class, "supplierID")
					.get());

			List<String> columnNames = new ArrayList<>();
			columnNames.add("Item ID");
			columnNames.add("Item Name");
			columnNames.add("Supplier Name");
			columnNames.add("Created At");
			columnNames.add("Updated At");

			itemTable.setRowFactory(tv -> new TableRow<>());
			for (String columnName : columnNames) {
				TableColumn<HashMap<String, String>, String> column = new TableColumn<>(columnName);

				column.setCellValueFactory(cellData ->
						new SimpleStringProperty(cellData.getValue().get(Helper.toAttrString(columnName))));

				itemTable.getColumns().add(column);
			}

			TableColumn<HashMap<String, String>, String> optionsColumns = this.getOptionsColumns();

			itemTable.getColumns().add(optionsColumns);
			itemTable.setItems(oListItems);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

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

	private TableColumn<HashMap<String, String>, String> getOptionsColumns() {
		TableColumn<HashMap<String, String>, String> optionsColumns = new TableColumn<>("Actions");

		optionsColumns.setCellFactory(column -> new TableCell<>() {
			private final MenuButton actionMenu = new MenuButton("⋮");
			private final HBox hBox = new HBox(actionMenu);
			{
				hBox.setSpacing(5);
				hBox.setAlignment(Pos.CENTER);


				MenuItem editItem = new MenuItem("Edit");
				MenuItem deleteItem = new MenuItem("Delete");

				editItem.setOnAction(event -> {
					HashMap<String, String> data = this.getTableView().getItems().get(this.getIndex());
					try {
						handleEdit(data);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				deleteItem.setOnAction(event -> {
					HashMap<String, String> data = this.getTableView().getItems().get(this.getIndex());
					try {
						handleDelete(data);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});

				actionMenu.getItems().addAll(editItem, deleteItem);
			}
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : hBox);
			}

		});
		return optionsColumns;
	}

	private void handleEdit(HashMap<String, String> data) throws IOException {
		EditItemView editItemView = new EditItemView(this);
		this.editItemNameField.setPromptText(data.get("itemName"));

		EditItemView.setData(data);

		editItemView.showEditItemPane();
		this.rootPane.setDisable(true);
	}

	private void handleDelete(HashMap<String, String> data) throws IOException {
		DeleteConfirmationView deleteConfirmationView = new DeleteConfirmationView(this);
		this.itemToBeDeleted.setText(data.get("itemName"));

		DeleteConfirmationView.setData(data);

		deleteConfirmationView.showDeleteConfirmationView();
		this.rootPane.setDisable(true);
	}

}
