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
import javafx.util.Callback;
import javafx.util.StringConverter;
import models.DTO.ItemListDTO;
import models.Datas.Supplier;
import models.Utils.Helper;
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
	@FXML private AnchorPane rootPane;
	@FXML private Button addItemButton,searchButton, clearSearchButton, deleteButton, cancelDeleteItemButton, saveEditItemButton, cancelEditItemButton, saveAddItemButton, cancelAddItemButton;
	@FXML private ChoiceBox<String> filterByChoiceBox;
	@FXML private TextField searchField;
	@FXML private TableView<ItemListDTO> itemTable = new TableView<>();
	@FXML private ComboBox<String> filterComboBox;
	@FXML private Label totalItemsLabel;

	@FXML
	Pane addItemPane, deleteItemPane;

	@FXML
	private TextField addItemNameField;
	@FXML private TextField addItemPriceField, addItemQuantityField, itemDescField;
	@FXML private ComboBox<Supplier> supplierComboBox = new ComboBox<>();


	@FXML private VBox deletePane;
	@FXML private Label itemToBeDeleted = new Label();

	@FXML private Pane editItemPane;
	@FXML private TextField editItemNameField = new TextField();
	@FXML private TextField editItemDescField = new TextField();
	@FXML private TextField editItemQuantityField = new TextField();
	@FXML private TextField editItemPriceField = new TextField();
	@FXML private ComboBox<Supplier> editSupplierComboBox = new ComboBox<>();

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
		String changedItemDescription = this.editItemDescField.getText();
		String changedItemQuantity = this.editItemQuantityField.getText();
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
		
		// Validate quantity is a valid number
		try {
			int quantity = Integer.parseInt(changedItemQuantity);
			if (quantity < 0) {
				notificationView = new NotificationView("Quantity cannot be negative", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
				return;
			}
		} catch (NumberFormatException e) {
			try {
				notificationView = new NotificationView("Quantity must be a valid number", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
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
		dataToUpdate.put("quantity", changedItemQuantity);
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
			}else {
				notificationView = new NotificationView("Item update failed", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
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
					controller.updateItemCount(controller.getItemTable().getItems().size());
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
		Supplier supplier = this.supplierComboBox.getValue();
		String itemDescription = this.itemDescField.getText();
		String itemPrice = this.addItemPriceField.getText();
		String itemQuantity = this.addItemQuantityField.getText();

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
			
			// Validate quantity is a valid number
			try {
				int quantity = Integer.parseInt(itemQuantity);
				if (quantity < 0) {
					notificationView = new NotificationView("Quantity cannot be negative", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
					notificationView.show();
					return;
				}
			} catch (NumberFormatException e) {
				notificationView = new NotificationView("Quantity must be a valid number", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
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
			
			boolean res = itemListService.add(itemName, itemDescription, itemQuantity, itemPrice, supplier.getSupplierId());

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
			setupFilters();
			loadItems();
			setupListeners();
			updateItemCount(oListItems.size());

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

		switch(sortOption) {
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
			private final Button actionButton = new Button("⋮");
			{
				actionButton.getStyleClass().addAll("action-button-table");
				actionButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #f0f0f0; -fx-text-fill: #092165; -fx-background-radius: 4px; -fx-padding: 2px 10px;");
				actionButton.setCursor(javafx.scene.Cursor.HAND);
				
				actionButton.setOnAction(event -> {
					ItemListDTO data = this.getTableView().getItems().get(this.getIndex());
					
					ContextMenu contextMenu = new ContextMenu();
					MenuItem editItem = new MenuItem("Edit");
					MenuItem deleteItem = new MenuItem("Delete");
					deleteItem.setStyle("-fx-text-fill:red;");
					
					// Add icons to menu items
					ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
					editIcon.setFitWidth(16);
					editIcon.setFitHeight(16);
					editItem.setGraphic(editIcon);
					
					ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
					deleteIcon.setFitWidth(16);
					deleteIcon.setFitHeight(16);
					deleteItem.setGraphic(deleteIcon);

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

					contextMenu.getItems().addAll(editItem, deleteItem);
					contextMenu.show(actionButton, javafx.geometry.Side.BOTTOM, 0, 0);
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
		this.editItemQuantityField.setText(data.get("quantity"));
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
				if (!Validation.isValidName(newValue)) {
					addItemNameField.setStyle("-fx-border-color: red;");
					saveAddItemButton.setDisable(true);
				} else {
					addItemNameField.setStyle("");
					validateAddForm();
				}
			});

			addItemPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!Validation.isValidCurrency(newValue)) {
					addItemPriceField.setStyle("-fx-border-color: red;");
					saveAddItemButton.setDisable(true);
				} else {
					addItemPriceField.setStyle("");
					validateAddForm();
				}
			});

			addItemQuantityField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!Validation.isValidQuantity(newValue)) {
					addItemQuantityField.setStyle("-fx-border-color: red;");
					saveAddItemButton.setDisable(true);
				} else {
					addItemQuantityField.setStyle("");
					validateAddForm();
				}
			});

		} else {
			// Edit Form Validation
			editItemNameField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!Validation.isValidName(newValue)) {
					editItemNameField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
				} else {
					editItemNameField.setStyle("");
					validateEditForm();
				}
			});

			editItemPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!Validation.isValidCurrency(newValue)) {
					editItemPriceField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
				} else {
					editItemPriceField.setStyle("");
					validateEditForm();
				}
			});

			editItemQuantityField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!Validation.isValidQuantity(newValue)) {
					editItemQuantityField.setStyle("-fx-border-color: red;");
					saveEditItemButton.setDisable(true);
				} else {
					editItemQuantityField.setStyle("");
					validateEditForm();
				}
			});
		}

	}

	private void validateAddForm() {
		boolean isValid = Validation.isValidName(addItemNameField.getText()) &&
						 Validation.isValidCurrency(addItemPriceField.getText()) &&
						 Validation.isValidQuantity(addItemQuantityField.getText()) &&
						 supplierComboBox.getValue() != null;
		saveAddItemButton.setDisable(!isValid);
	}

	private void validateEditForm() {
		boolean isValid = Validation.isValidName(editItemNameField.getText()) &&
						 Validation.isValidCurrency(editItemPriceField.getText()) &&
						 Validation.isValidQuantity(editItemQuantityField.getText()) &&
						 editSupplierComboBox.getValue() != null;
		saveEditItemButton.setDisable(!isValid);
	}
}
