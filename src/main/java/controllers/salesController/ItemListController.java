//package controllers.salesController;
//
//import controllers.NotificationController;
//import javafx.application.Platform;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.EventTarget;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.geometry.Pos;
//import javafx.scene.Parent;
//import javafx.scene.control.*;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Pane;
//import javafx.util.StringConverter;
//import models.Datas.Item;
//import models.Datas.Supplier;
//import models.Utils.Helper;
//import models.Utils.QueryBuilder;
//import org.start.owsb.Layout;
//import views.NotificationView;
//import views.salesViews.AddItemView;
//import views.salesViews.DeleteConfirmationView;
//
//import java.io.IOException;
//import java.net.URL;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//public class ItemListController implements Initializable {
//	private ItemListController instance = this;
//	@FXML private AnchorPane rootPane;
//	@FXML private Button addItemButton;
//	@FXML private Button searchButton;
//	@FXML private TextField sortByField;
//	@FXML private TextField searchField;
//	@FXML private TableView<HashMap<String, String>> itemTable = new TableView<>();
//
//	// Add Item Popup
//	@FXML
//	Pane addItemPane;
//	@FXML
//	private TextField addItemNameField;
//	@FXML private ChoiceBox<Supplier> supplierChoiceBox = new ChoiceBox<>();
//	@FXML private Button saveAddItemButton;
//	@FXML private Button cancelAddItemButton;
//
//	@FXML private Pane deleteItemPane;
//	@FXML private Button deleteButton;
//	@FXML private Button cancelDeleteItemButton;
//	@FXML private Label itemToBeDeleted = new Label();
//
//	@FXML public void onDeleteItemButtonClick() throws IOException {
//		HashMap<String, String> oldData = DeleteConfirmationView.getData();
//		String selectedId = oldData.get("item_id");
//		try {
//			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
//			boolean res = qb.target("db/Item.txt").delete(selectedId);
//			NotificationView notificationView;
//			if (res) {
//				notificationView = new NotificationView("Item deleted successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
//				ItemListController controller = DeleteConfirmationView.getRootController();
//				controller.getRootPane().setDisable(false);
//				Platform.runLater(() -> {
//					controller.getItemTable().getItems().remove(oldData);
//					controller.getItemTable().refresh();
//				});
//			}else {
//				notificationView = new NotificationView("Item deletion failed", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
//			}
//			notificationView.show();
//
//			Layout layout = Layout.getInstance();
//			BorderPane root = layout.getRoot();
//			root.getChildren().remove(this.deleteItemPane);
//
//		}catch (Exception e) {
//			NotificationView notificationView = new NotificationView(e.getMessage(), NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
//			notificationView.show();
//		}
//
//	}
//
//	@FXML public void onCancelDeleteItemButtonClick() {
//		Layout layout = Layout.getInstance();
//		BorderPane root = layout.getRoot();
//		root.getChildren().remove(this.deleteItemPane);
//
//		ItemListController controller = DeleteConfirmationView.getRootController();
//		controller.getRootPane().setDisable(false);
//	}
//
//	@FXML public void onSaveAddItemButtonClick() throws IOException {
//		String itemName = this.addItemNameField.getText();
//		Supplier supplier = this.supplierChoiceBox.getValue();
//
//		try {
//			NotificationView notificationView;
//			ItemListController controllerReference = AddItemView.getRootController();
//			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
//			ArrayList<HashMap<String, String>> data = qb.select(new String[]{"item_name", "supplier_id"}).from("db/Item.txt").get();
//
//			for (HashMap<String, String> item: data) {
//				if (item.get("item_name").equals(itemName) && item.get("supplier_id").equals(String.valueOf(supplier.getSupplierId()))) {
//					notificationView = new NotificationView("Item already exists", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
//					notificationView.show();
//					return;
//				}
//			}
//			boolean res = qb.target("db/Item.txt").values(new String[]{
//					itemName,
//					LocalDate.now().format(DateTimeFormatter.ISO_DATE),
//					LocalDate.now().format(DateTimeFormatter.ISO_DATE),
//					"0",
//					"100",
//					String.valueOf(supplier.getSupplierId())
//			}).create();
//
//			if (res) {
//				ArrayList<HashMap<String, String>> newData = qb
//						.select(new String[]{"item_id", "item_name", "supplier_name", "created_at", "updated_at", "supplier_id"})
//						.from("db/Item.txt")
//						.joins(Supplier.class, "supplier_id")
//						.get();
//				ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
//				oListItems.addAll(newData);
//
//				Platform.runLater(() -> {
//					TableView<HashMap<String, String>> itemTable = controllerReference.getItemTable();
//					itemTable.getItems().clear();
//					itemTable.setItems(oListItems);
//					itemTable.refresh();
//
//				});
//				notificationView = new NotificationView("Item added successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
//			} else {
//				notificationView = new NotificationView("Item already exists", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
//			}
//			notificationView.show();
//
//			Layout layout = Layout.getInstance();
//			BorderPane root = layout.getRoot();
//			root.getChildren().remove(this.addItemPane);
//
//			controllerReference.getRootPane().setDisable(false);
//		}catch (Exception e) {
//			NotificationView notificationView = new NotificationView(e.getMessage(), NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
//			notificationView.show();
//		}
//	}
//
//	@FXML public void onCancelAddItemButtonClick() {
//		Layout layout = Layout.getInstance();
//		BorderPane root = layout.getRoot();
//		root.getChildren().remove(this.addItemPane);
//
//		ItemListController controller = AddItemView.getRootController();
//		controller.getRootPane().setDisable(false);
//	}
//
//	@FXML
//	public void handleAddItemButtonClick() throws IOException {
//		AddItemView addItemView = new AddItemView();
//		addItemView.showAddItemView(this);
//
//		this.rootPane.setDisable(true);
//	}
//
//	public TableView<HashMap<String, String>> getItemTable() {
//		return this.itemTable;
//	}
//
//	public AnchorPane getRootPane() {
//		return this.rootPane;
//	}
//
//	@Override
//	public void initialize(URL url, ResourceBundle resourceBundle) {
//		try {
//			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
//			ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList(qb
//					.select(new String[]{"item_id", "item_name", "supplier_name", "created_at", "updated_at", "supplier_id"})
//					.from("db/Item.txt")
//					.joins(Supplier.class, "supplier_id")
//					.get());
//
//			List<String> columnNames = new ArrayList<>();
//			columnNames.add("Item Id");
//			columnNames.add("Item Name");
//			columnNames.add("Supplier Name");
//			columnNames.add("Created At");
//			columnNames.add("Updated At");
//
//			itemTable.setRowFactory(tv -> new TableRow<>());
//			for (String columnName : columnNames) {
//				TableColumn<HashMap<String, String>, String> column = new TableColumn<>(columnName);
//
//				column.setCellValueFactory(cellData ->
//						new SimpleStringProperty(cellData.getValue().get(Helper.toAttrString(columnName))));
//
//				itemTable.getColumns().add(column);
//			}
//
//			TableColumn<HashMap<String, String>, String> optionsColumns = this.getOptionsColumns();
//
//			itemTable.getColumns().add(optionsColumns);
//			itemTable.setItems(oListItems);
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//		}
//
//		try {
//			QueryBuilder<Supplier> qb = new QueryBuilder<>(Supplier.class);
//			ArrayList<Supplier> suppliers = qb.select().from("db/Supplier.txt").getAsObjects();
//			ObservableList<Supplier> supplierList = FXCollections.observableArrayList(suppliers);
//
//			StringConverter<Supplier> supplierConverter = new StringConverter<>() {
//				@Override
//				public String toString(Supplier supplier) {
//					if (supplier == null) {
//						return "None";
//					}
//					return supplier.getSupplierName();
//				}
//
//				@Override
//				public Supplier fromString(String s) {
//					return null;
//				}
//			};
//			this.supplierChoiceBox.setConverter(supplierConverter);
//			this.supplierChoiceBox.getItems().addAll(supplierList);
//
//			if (!supplierChoiceBox.getItems().isEmpty()) {
//				supplierChoiceBox.setValue(supplierChoiceBox.getItems().get(0));
//			}
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//		}
//
//	}
//
//	private TableColumn<HashMap<String, String>, String> getOptionsColumns() {
//		TableColumn<HashMap<String, String>, String> optionsColumns = new TableColumn<>("Actions");
//
//		optionsColumns.setCellFactory(column -> new TableCell<>() {
//			private final MenuButton actionMenu = new MenuButton("⋮");
//			private final HBox hBox = new HBox(actionMenu);
//			{
//				hBox.setSpacing(5);
//				hBox.setAlignment(Pos.CENTER);
//
//
//				MenuItem editItem = new MenuItem("Edit");
//				MenuItem deleteItem = new MenuItem("Delete");
//
//				editItem.setOnAction(event -> handleEdit());
//				deleteItem.setOnAction(event -> {
//					try {
//						HashMap<String, String> data = this.getTableView().getItems().get(this.getIndex());
//						handleDelete(data);
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//				});
//
//				actionMenu.getItems().addAll(editItem, deleteItem);
//			}
//			@Override
//			protected void updateItem(String item, boolean empty) {
//				super.updateItem(item, empty);
//				setGraphic(empty ? null : hBox);
//			}
//
//		});
//		return optionsColumns;
//	}
//
//	private void handleEdit() {}
//
//	private void handleDelete(HashMap<String, String> data) throws IOException {
//		DeleteConfirmationView deleteConfirmationView = new DeleteConfirmationView(this);
//		this.itemToBeDeleted.setText(data.get("item_name"));
//		System.out.println(data.get("item_name"));
//		DeleteConfirmationView.setData(data);
//
//		deleteConfirmationView.showDeleteConfirmationView();
//		this.rootPane.setDisable(true);
//	}
//
//	public ItemListController getInstance() {
//		return instance;
//	}
//
//	public void setInstance(ItemListController instance) {
//		this.instance = instance;
//	}
//}
