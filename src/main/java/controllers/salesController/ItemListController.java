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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import models.DTO.ItemListDTO;
import models.Datas.Supplier;
import models.Utils.Helper;
import models.Utils.SessionManager;
import models.Utils.Validation;
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
	private String[] columns;
	private final ItemService itemListService = new ItemService();
	private final SupplierService supplierService = new SupplierService();
	private SessionManager session = SessionManager.getInstance();
	private String user_role = session.getUserData().get("roleID");
	@FXML
	private AnchorPane rootPane;
	@FXML
	private Button addItemButton, searchButton, clearSearchButton, deleteButton, cancelDeleteItemButton, saveEditItemButton, cancelEditItemButton, saveAddItemButton, cancelAddItemButton;
	@FXML
	private ChoiceBox<String> filterByChoiceBox;
	@FXML
	private TextField searchField;
	@FXML
	private TableView<ItemListDTO> itemTable = new TableView<>();
	@FXML
	private ComboBox<String> filterComboBox;
	@FXML
	private Label totalItemsLabel;

	@FXML
	Pane addItemPane, deleteItemPane;

	@FXML
	private TextField addItemNameField;
	@FXML
	private TextField addItemPriceField, itemDescField;
	@FXML
	private ComboBox<Supplier> supplierComboBox = new ComboBox<>();


	@FXML
	private VBox deletePane;
	@FXML
	private Label itemToBeDeleted = new Label();

	@FXML
	private Pane editItemPane;
	@FXML
	private TextField editItemNameField = new TextField();
	@FXML
	private TextField editItemDescField = new TextField();
	@FXML
	private TextField editItemPriceField = new TextField();
	@FXML
	private ComboBox<Supplier> editSupplierComboBox = new ComboBox<>();

	// Clears the search field and refreshes results
	public void onClear() {
		this.searchField.clear();
		ObservableList<ItemListDTO> latestData = getLatestData();
		this.itemTable.setItems(latestData);
		this.itemTable.refresh();
		updateItemCount(latestData.size());
	}

	// Searches for items with the contained keyword
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
		updateItemCount(filteredData.size());
	}

	@FXML
	public void onCancelEditItemButtonClick() {
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
		String changedItemDescription = this.editItemDescField.getText();
		String changedItemPrice = this.editItemPriceField.getText();
		Supplier selectedSupplier = this.editSupplierComboBox.getValue();

		// Validation
		if (changedItemName == null || changedItemName.trim().isEmpty()) {
			try {
				notificationView = new NotificationView("Item name cannot be empty", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			return;
		}

		if (selectedSupplier == null) {
			try {
				notificationView = new NotificationView("Please select a supplier", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			return;
		}

		String changedSupplierID = selectedSupplier.getSupplierId();

		// Validate price is a valid number
		try {
			double price = Double.parseDouble(changedItemPrice);
			if (price <= 0) {
				notificationView = new NotificationView("Price must be greater than zero", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
				return;
			}
		} catch (NumberFormatException e) {
			try {
				notificationView = new NotificationView("Price must be a valid number", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
			return;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}

		String itemId = EditItemView.getData().get("itemID");

		HashMap<String, String> dataToUpdate = new HashMap<>();
		dataToUpdate.put("itemName", changedItemName);
		dataToUpdate.put("description", changedItemDescription);
		dataToUpdate.put("unitPrice", changedItemPrice);
		dataToUpdate.put("supplierID", changedSupplierID);
		try {
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
			} else {
				notificationView = new NotificationView("Item update failed", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			}
			notificationView.show();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@FXML
	public void onDeleteItemButtonClick() throws IOException {
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
					controller.updateItemCount(controller.getItemTable().getItems().size());
				});
			} else {
				notificationView = new NotificationView("Item deletion failed", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			}
			notificationView.show();

			Layout layout = Layout.getInstance();
			BorderPane root = layout.getRoot();
			root.getChildren().remove(this.deleteItemPane);

		} catch (Exception e) {
			NotificationView notificationView = new NotificationView(e.getMessage(), NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			notificationView.show();
		}

	}

	@FXML
	public void onCancelDeleteItemButtonClick() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.deleteItemPane);

		ItemListController controller = DeleteConfirmationView.getRootController();
		SidebarController.getSidebar().setDisable(false);
		controller.getRootPane().setDisable(false);
	}

	@FXML
	public void onSaveAddItemButtonClick() throws IOException {
		String itemName = this.addItemNameField.getText();
		Supplier supplier = this.supplierComboBox.getValue();
		String itemDescription = this.itemDescField.getText();
		String itemPrice = this.addItemPriceField.getText();

		try {
			NotificationView notificationView;

			// Validation
			if (itemName == null || itemName.trim().isEmpty()) {
				notificationView = new NotificationView("Item name cannot be empty", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
				return;
			}

			if (supplier == null) {
				notificationView = new NotificationView("Please select a supplier", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
				return;
			}

			if (itemPrice == null || itemPrice.trim().isEmpty()) {
				notificationView = new NotificationView("Item price cannot be empty", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
				return;
			}

			// Validate price is a valid number
			try {
				double price = Double.parseDouble(itemPrice);
				if (price <= 0) {
					notificationView = new NotificationView("Price must be greater than zero", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
					notificationView.show();
					return;
				}
			} catch (NumberFormatException e) {
				notificationView = new NotificationView("Price must be a valid number", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
				return;
			}

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

			boolean res = itemListService.add(itemName, itemDescription, "0", itemPrice, supplier.getSupplierId());

			if (res) {
				ObservableList<ItemListDTO> oListItems = getLatestData();

				Platform.runLater(() -> {
					TableView<ItemListDTO> itemTable = controllerReference.getItemTable();
					itemTable.getItems().clear();
					itemTable.setItems(oListItems);
					itemTable.refresh();
					controllerReference.updateItemCount(oListItems.size());
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
		} catch (Exception e) {
			NotificationView notificationView = new NotificationView(e.getMessage(), NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
			notificationView.show();
		}
	}

	@FXML
	public void onCancelAddItemButtonClick() {
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
			setupFilters();
			loadItems();
			setupListeners();
			updateItemCount(oListItems.size());

			this.columns = new String[]{"Item ID", "Item Name", "Description", "Quantity", "Supplier Name", "Unit Price", "Created At", "Updated At"};
			List<String> columnNames = List.of(this.columns);

			for (String columnName : columnNames) {
				TableColumn<ItemListDTO, String> column = new TableColumn<>(columnName);
				column.setCellValueFactory(new PropertyValueFactory<>(Helper.toAttrString(columnName)));

				// Set max width for specific columns
				if (columnName.equals("Unit Price") || columnName.equals("Quantity") || columnName.equals("Item ID")) {
					column.setMinWidth(120);
					column.setPrefWidth(120);
					column.setMaxWidth(120);
				}

				itemTable.getColumns().add(column);
			}

			if (user_role.equals("1") || user_role.equals("2")) {
				TableColumn<ItemListDTO, String> optionsColumns = this.getOptionsColumns();
				itemTable.getColumns().add(optionsColumns);
			} else if (user_role.equals("3")) {
				addItemButton.setVisible(false);
			}
			itemTable.setItems(oListItems);
		} catch (Exception e) {
			System.out.println("Search error: " + e.getMessage());
		}
	}

	private void setupFilters() {
		if (filterComboBox != null) {
			filterComboBox.getItems().addAll(
					"Name (A-Z)",
					"Name (Z-A)",
					"Price (Low-High)",
					"Price (High-Low)",
					"Quantity (Low-High)",
					"Quantity (High-Low)"
			);
			filterComboBox.getSelectionModel().selectFirst();
		}
	}

	private void setupListeners() {
		if (searchButton != null) {
			searchButton.setOnAction(e -> searchItems());
		}

		if (filterComboBox != null) {
			// Add sorting functionality
			filterComboBox.setOnAction(e -> applySorting());
		}

		if (clearSearchButton != null) {
			clearSearchButton.setOnAction(e -> onClear());
		}
	}

	private void applySorting() {
		String sortOption = filterComboBox.getValue();
		if (sortOption == null) return;

		ObservableList<ItemListDTO> items = itemTable.getItems();

		switch (sortOption) {
			case "Name (A-Z)":
				FXCollections.sort(items, Comparator.comparing(ItemListDTO::getItemName));
				break;
			case "Name (Z-A)":
				FXCollections.sort(items, Comparator.comparing(ItemListDTO::getItemName, Comparator.reverseOrder()));
				break;
			case "Price (Low-High)":
				FXCollections.sort(items, Comparator.comparing(ItemListDTO::getUnitPrice));
				break;
			case "Price (High-Low)":
				FXCollections.sort(items, Comparator.comparing(ItemListDTO::getUnitPrice, Comparator.reverseOrder()));
				break;
			case "Quantity (Low-High)":
				FXCollections.sort(items, Comparator.comparing(i -> {
					try {
						return i.getQuantity();
					} catch (Exception e) {
						return 0;
					}
				}));
				break;
			case "Quantity (High-Low)":
				FXCollections.sort(items, Comparator.comparing(i -> {
					try {
						return -i.getQuantity();
					} catch (Exception e) {
						return 0;
					}
				}));
				break;
		}

		itemTable.refresh();
	}

	private void loadItems() {
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
			this.supplierComboBox.setConverter(supplierConverter);
			this.editSupplierComboBox.setConverter(supplierConverter);
			this.supplierComboBox.getItems().addAll(supplierList);
			this.editSupplierComboBox.getItems().addAll(supplierList);

			if (!supplierComboBox.getItems().isEmpty()) {
				supplierComboBox.setValue(supplierComboBox.getItems().get(0));
			}
			if (!editSupplierComboBox.getItems().isEmpty()) {
				editSupplierComboBox.setValue(editSupplierComboBox.getItems().get(0));
			}
		} catch (Exception e) {
			System.out.println("Error loading items: " + e.getMessage());
		}
	}

	private void updateItemCount(int count) {
		if (totalItemsLabel != null) {
			totalItemsLabel.setText(String.format("Total Items: %d", count));
		}
	}

	private TableColumn<ItemListDTO, String> getOptionsColumns() {
		TableColumn<ItemListDTO, String> optionsColumns = new TableColumn<>("Actions");

		optionsColumns.setCellFactory(column -> new TableCell<>() {
			private final MenuButton actionButton = new MenuButton("⋮");

			{
				actionButton.getStyleClass().addAll("action-button-table");
				actionButton.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: #092165; -fx-background-radius: 4px; -fx-padding: 2px 10px;");
				actionButton.setCursor(javafx.scene.Cursor.HAND);

				MenuItem editItem = new MenuItem("Edit");
				MenuItem deleteItem = new MenuItem("Delete");
				deleteItem.setStyle("-fx-text-fill: #ba0202;");

				// Add icons to menu items
				ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
				editIcon.setFitWidth(16);
				editIcon.setFitHeight(16);
				editItem.setGraphic(editIcon);

				ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
				deleteIcon.setFitWidth(16);
				deleteIcon.setFitHeight(16);
				deleteItem.setGraphic(deleteIcon);

				actionButton.getItems().addAll(editItem, deleteItem);

				actionButton.setOnShowing(event -> {
					ItemListDTO data = this.getTableView().getItems().get(this.getIndex());

					editItem.setOnAction(e -> {
						try {
							handleEdit(data.toMap());
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					});

					deleteItem.setOnAction(e -> {
						try {
							handleDelete(data.toMap());
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					});

				});
				setAlignment(Pos.CENTER);
			}

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : actionButton);
			}
		});
		return optionsColumns;
	}

	public void prefillEditFields(HashMap<String, String> data) {
		this.editItemNameField.setText(data.get("itemName"));
		this.editItemDescField.setText(data.get("description"));
		this.editItemPriceField.setText(data.get("unitPrice"));

		String supplierId = data.get("supplierID");
		if (supplierId != null) {
			for (Supplier supplier : this.editSupplierComboBox.getItems()) {
				if (supplier.getSupplierId().equals(supplierId)) {
					this.editSupplierComboBox.setValue(supplier);
					break;
				}
			}
		}
	}

	private void handleEdit(HashMap<String, String> data) throws IOException {
		EditItemView editItemView = new EditItemView(this);


		EditItemView.setData(data);
		editItemView.showEditItemPane();

		this.rootPane.setDisable(true);
		SidebarController.getSidebar().setDisable(true);
	}

	public void setItemToBeDeleted(String name) {
		this.itemToBeDeleted.setText("Are you sure you want to delete \"" + name + "\"?");
	}

	private void handleDelete(HashMap<String, String> data) throws IOException {
		DeleteConfirmationView.setData(data);
		DeleteConfirmationView deleteConfirmationView = new DeleteConfirmationView(this);

		deleteConfirmationView.showDeleteConfirmationView();

		this.rootPane.setDisable(true);
		SidebarController.getSidebar().setDisable(true);

	}

	private ObservableList<ItemListDTO> getLatestData() {
		return FXCollections.observableArrayList(itemListService.getAll());
	}

	public void setupFormValidation(String type) {
		// Add Form Validation
		if (type.equals("add")) {
			addItemNameField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null || newValue.trim().isEmpty()) {
					addItemNameField.setStyle("");
					validateAddForm();
					return;
				}

				if (!Validation.isValidItemName(newValue)) {
					addItemNameField.setStyle("-fx-border-color: red;");
					saveAddItemButton.setDisable(true);
					showNotification("Item name must be 2-100 characters with valid characters only", NotificationController.popUpType.error);
				} else {
					addItemNameField.setStyle("");
					validateAddForm();
				}
			});

			addItemPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null || newValue.trim().isEmpty()) {
					addItemPriceField.setStyle("");
					validateAddForm();
					return;
				}

				if (!Validation.isValidPrice(newValue)) {
					addItemPriceField.setStyle("-fx-border-color: red;");
					saveAddItemButton.setDisable(true);
					showNotification("Price must be a valid number greater than 0", NotificationController.popUpType.error);
				} else {
					addItemPriceField.setStyle("");
					validateAddForm();
				}
			});

			itemDescField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null || newValue.trim().isEmpty()) {
					itemDescField.setStyle("");
					validateAddForm();
					return;
				}

				if (!Validation.isValidDescription(newValue)) {
					itemDescField.setStyle("-fx-border-color: red;");
					saveAddItemButton.setDisable(true);
					showNotification("Description must be less than 500 characters with valid characters only", NotificationController.popUpType.error);
				} else {
					itemDescField.setStyle("");
					validateAddForm();
				}
			});

			supplierComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateAddForm());

		} else if (type.equals("edit")) {
			// Edit Form Validation
			editItemNameField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null || newValue.trim().isEmpty()) {
					editItemNameField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
					showNotification("Item name cannot be empty", NotificationController.popUpType.error);
				} else if (!Validation.isValidItemName(newValue)) {
					editItemNameField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
					showNotification("Item name must be 2-100 characters with valid characters only", NotificationController.popUpType.error);
				} else {
					editItemNameField.setStyle("");
					validateEditForm();
				}
			});

			editItemPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null || newValue.trim().isEmpty()) {
					editItemPriceField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
					showNotification("Price cannot be empty", NotificationController.popUpType.error);
				} else if (!Validation.isValidPrice(newValue)) {
					editItemPriceField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
					showNotification("Price must be a valid number greater than 0", NotificationController.popUpType.error);
				} else {
					editItemPriceField.setStyle("");
					validateEditForm();
				}
			});

			editItemDescField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null || newValue.trim().isEmpty()) {
					editItemDescField.setStyle("");
					validateEditForm();
					return;
				}

				if (!Validation.isValidDescription(newValue)) {
					editItemDescField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
					showNotification("Description must be less than 500 characters with valid characters only", NotificationController.popUpType.error);
				} else {
					editItemDescField.setStyle("");
					validateEditForm();
				}
			});

			editSupplierComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateEditForm());
		}
	}

	private void validateAddForm() {
		boolean isValid = Validation.isNotEmpty(addItemNameField.getText()) &&
				Validation.isValidItemName(addItemNameField.getText()) &&
				Validation.isNotEmpty(addItemPriceField.getText()) &&
				Validation.isValidPrice(addItemPriceField.getText()) &&
				Validation.isValidDescription(itemDescField.getText()) &&
				supplierComboBox.getValue() != null;
		saveAddItemButton.setDisable(!isValid);
	}

	private void validateEditForm() {
		boolean isValid = Validation.isNotEmpty(editItemNameField.getText()) &&
				Validation.isValidItemName(editItemNameField.getText()) &&
				Validation.isNotEmpty(editItemPriceField.getText()) &&
				Validation.isValidPrice(editItemPriceField.getText()) &&
				Validation.isValidDescription(editItemDescField.getText()) &&
				editSupplierComboBox.getValue() != null;
		saveEditItemButton.setDisable(!isValid);
	}

	private void showNotification(String message, NotificationController.popUpType type) {
		try {
			NotificationView notificationView = new NotificationView(message, type, NotificationController.popUpPos.TOP);
			notificationView.show();
		} catch (Exception ex) {
			System.err.println("Failed to show notification: " + ex.getMessage());
		}
	}
}