package controllers.salesController;

import controllers.NotificationController;
import controllers.SidebarController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import models.DTO.ItemListDTO;
import models.Datas.Item;
import models.Datas.Supplier;
import models.Utils.Helper;
import models.Utils.QueryBuilder;
import org.start.owsb.Layout;
import service.ItemService;
import service.SupplierService;
import views.NotificationView;
import views.salesViews.AddItemView;
import views.salesViews.DeleteConfirmationView;
import views.salesViews.EditItemView;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ItemListController implements Initializable {
	// Item List Page
	private String[] columns;
	private final ItemService itemListService = new ItemService();
	private final SupplierService supplierService = new SupplierService();
	@FXML private AnchorPane rootPane;
	@FXML private Button addItemButton;
	@FXML private Button searchButton;
	@FXML private ChoiceBox<String> filterByChoiceBox;
	@FXML private TextField searchField;
	@FXML private Button clearSearchButton;
	@FXML private TableView<ItemListDTO> itemTable = new TableView<>();

	// Add Item Popup
	@FXML
	Pane addItemPane;
	@FXML
	private TextField addItemNameField;
	@FXML private ChoiceBox<Supplier> supplierChoiceBox = new ChoiceBox<>();
	@FXML private TextField itemDescField;
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
	@FXML private TextField editDescField = new TextField();
	@FXML private Button saveEditItemButton;
	@FXML private Button cancelEditItemButton;

	// Initialize the item list filter box
	public void initFilterItems() {
		String[] filterList = {"All", "Alert Item"};
		if (filterByChoiceBox != null) {
			filterByChoiceBox.getItems().addAll(filterList);
			filterByChoiceBox.setOnAction(event -> {
				String selectedFilter = filterByChoiceBox.getSelectionModel().getSelectedItem();
				if (Objects.equals(selectedFilter, "Alert Item")) {
					ObservableList<ItemListDTO> alertItems = FXCollections.observableArrayList();

					for (ItemListDTO item : itemTable.getItems()) {
						if (item.getQuantity() < item.getAlertSetting()) {
							alertItems.add(item);
						}
					}

					itemTable.setItems(alertItems);
				} else {
					itemTable.setItems(getLatestData());
				}
			});
		}
	}

	// Clears the search field and refreshes results
	@FXML
	public void onClear() {
		this.searchField.clear();

		this.itemTable.setItems(getLatestData());
		this.itemTable.refresh();
	}

	// Searches for items with the contained keyword
	@FXML
	public void searchItems() {
		String searchKeyword = searchField.getText().toLowerCase();

		ObservableList<ItemListDTO> data = this.itemTable.getItems();

		ObservableList<ItemListDTO> filteredData = data.filtered(
				item -> {
					if (searchKeyword.isEmpty()) {
						return true;
					}
					if (searchKeyword.matches("[0-9]+")) {
						return item.getQuantity() == Integer.parseInt(searchKeyword) |
								item.getAlertSetting() == Integer.parseInt(searchKeyword);
					}

					return item.getItemName().toLowerCase().contains(searchKeyword) |
							item.getDescription().toLowerCase().contains(searchKeyword) |
							item.getSupplierName().toLowerCase().contains(searchKeyword);
				}
		);

		itemTable.setItems(filteredData);
	}

	@FXML
	public void onCancelEditItemButtonClick(){
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.editItemPane);

		ItemListController controller = EditItemView.getRootController();
		SidebarController.getSidebar().setDisable(false);
		controller.getRootPane().setDisable(false);

	}

	@FXML
	public void onSaveEditItemButtonClick() {
		NotificationView notificationView;
		String changedItemName = this.editItemNameField.getText();
		String changedItemDescription = this.editDescField.getText();
		String itemId = EditItemView.getData().get("itemID");
		String supplierId = EditItemView.getData().get("supplierID");

		HashMap<String, String> dataToUpdate = new HashMap<>();
		dataToUpdate.put("itemName", changedItemName);
		dataToUpdate.put("description", changedItemDescription);
		try {
			//here
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

			boolean res = itemListService.update(itemId, dataToUpdate);

			if (res) {
				ObservableList<ItemListDTO> oListItems = FXCollections.observableArrayList(getLatestData());

				Platform.runLater(() -> {
					Layout layout = Layout.getInstance();
					BorderPane root = layout.getRoot();
					root.getChildren().remove(this.editItemPane);
					ItemListController controller = EditItemView.getRootController();
					controller.getRootPane().setDisable(false);
					SidebarController.getSidebar().setDisable(false);

					TableView<ItemListDTO> itemTable = controller.getItemTable();
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
			boolean res = itemListService.delete(selectedId);
			NotificationView notificationView;
			if (res) {
				notificationView = new NotificationView("Item deleted successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
				ItemListController controller = DeleteConfirmationView.getRootController();
				SidebarController.getSidebar().setDisable(false);
				controller.getRootPane().setDisable(false);
				Platform.runLater(() -> {
					// Find the deleted item and remove it from the table
					controller.getItemTable().getItems().stream().filter(i ->
							i.getItemID().equals(selectedId)).findFirst().ifPresent(controller.getItemTable().getItems()::remove);
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
		SidebarController.getSidebar().setDisable(false);
		controller.getRootPane().setDisable(false);
	}

	@FXML public void onSaveAddItemButtonClick() throws IOException {
		String itemName = this.addItemNameField.getText();
		Supplier supplier = this.supplierChoiceBox.getValue();

		try {
			NotificationView notificationView;
			ItemListController controllerReference = AddItemView.getRootController();
			ObservableList<ItemListDTO> data = getLatestData();

			// Check for existing items with same name and supplier
			for (ItemListDTO item : data) {
				if (item.getItemName().equals(itemName) && item.getSupplierID().equals(String.valueOf(supplier.getSupplierId()))) {
					notificationView = new NotificationView("Item already exists", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
					notificationView.show();
					return;
				}
			}
			boolean res = itemListService.add(itemName, this.itemDescField.getText(), supplier.getSupplierId());

			if (res) {
				ObservableList<ItemListDTO> oListItems = getLatestData();

				Platform.runLater(() -> {
					TableView<ItemListDTO> itemTable = controllerReference.getItemTable();
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

			SidebarController.getSidebar().setDisable(false);
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
		SidebarController.getSidebar().setDisable(false);

	}

	@FXML
	public void handleAddItemButtonClick() throws IOException {
		SidebarController.getSidebar().setDisable(true);
		this.rootPane.setDisable(true);


		AddItemView addItemView = new AddItemView();
		addItemView.showAddItemView(this);

	}

	public TableView<ItemListDTO> getItemTable() {
		return this.itemTable;
	}

	public AnchorPane getRootPane() {
		return this.rootPane;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			ObservableList<ItemListDTO> oListItems = getLatestData();

			this.columns = new String[]{"Item ID", "Item Name", "Description", "Supplier Name", "Unit Price", "Quantity", "Created At", "Updated At"};
			List<String> columnNames = List.of(this.columns);

			// If quantity less than alert setting, color red
			itemTable.setRowFactory(new Callback<>() {
				@Override
				public TableRow<ItemListDTO> call(TableView<ItemListDTO> hashMapTableView) {
					return new TableRow<>() {
						@Override
						protected void updateItem(ItemListDTO item, boolean empty) {
							super.updateItem(item, empty);
							if (item != null && item.getQuantity() < item.getAlertSetting()) {
								setStyle("-fx-background-color: #ff0000;");
							} else {
								setStyle("");
							}
						}
					};
				}
			});

			for (String columnName : columnNames) {
				TableColumn<ItemListDTO, String> column = new TableColumn<>(columnName);

				column.setCellValueFactory(new PropertyValueFactory<>(Helper.toAttrString(columnName)));

				itemTable.getColumns().add(column);
			}

			TableColumn<ItemListDTO, String> optionsColumns = this.getOptionsColumns();

			itemTable.getColumns().add(optionsColumns);
			itemTable.setItems(oListItems);

			initFilterItems();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		try {
			ObservableList<Supplier> supplierList = FXCollections.observableArrayList(supplierService.getAll());
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

	private TableColumn<ItemListDTO, String> getOptionsColumns() {
		TableColumn<ItemListDTO, String> optionsColumns = new TableColumn<>("Actions");

		optionsColumns.setCellFactory(column -> new TableCell<>() {
			private final MenuButton actionMenu = new MenuButton("⋮");
			private final HBox hBox = new HBox(actionMenu);
			{
				hBox.setSpacing(5);
				hBox.setAlignment(Pos.CENTER);


				MenuItem editItem = new MenuItem("Edit");
				MenuItem deleteItem = new MenuItem("Delete");

				editItem.setOnAction(event -> {
					ItemListDTO data = this.getTableView().getItems().get(this.getIndex());
					try {
						handleEdit(data.toMap());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				deleteItem.setOnAction(event -> {
					ItemListDTO data = this.getTableView().getItems().get(this.getIndex());
					try {
						handleDelete(data.toMap());
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
		SidebarController.getSidebar().setDisable(true);

	}

	private void handleDelete(HashMap<String, String> data) throws IOException {
		DeleteConfirmationView deleteConfirmationView = new DeleteConfirmationView(this);
		this.itemToBeDeleted.setText(data.get("itemName"));

		DeleteConfirmationView.setData(data);

		deleteConfirmationView.showDeleteConfirmationView();
		this.rootPane.setDisable(true);
		SidebarController.getSidebar().setDisable(true);

	}

	private ObservableList<ItemListDTO> getLatestData() {
		return FXCollections.observableArrayList(itemListService.getAll());
	}
}
