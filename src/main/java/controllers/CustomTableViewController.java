package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import models.ModelInitializable;
import models.Utils.Helper;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;
import views.Command;
import views.CustomTableView;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CustomTableViewController implements Initializable {
	private static Command command;
	private Class<? extends ModelInitializable> model;
	private String[] columns;
	@FXML private AnchorPane rootPane;
	@FXML private ComboBox<String> filterComboBox;
	@FXML private TextField searchField;
	@FXML private Button btnSearch;
	@FXML private Button btnClear;
	@FXML private Button addItemButton;
	@FXML private Label titleLabel;
	@FXML private TableView<HashMap<String, String>> tableView;

	@FXML
	public void onAddItemButtonClicked() throws IOException {
		command.openModal();
	}

	@FXML
	public void onSearchButtonClicked() {
		String searchText = searchField.getText();
		if (searchText.isEmpty()) {
			ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
			oListItems.addAll(getLatestData());
			tableView.setItems(oListItems);
			return;
		}
		ArrayList<HashMap<String, String>> data = tableView
				.getItems()
				.stream()
				.filter(item ->
						Arrays.stream(columns)
								.anyMatch(column ->
										item.get(Helper.toAttrString(column, Helper.AttrFormat.snake_case))
												.toLowerCase()
												.contains(searchText.toLowerCase())
								)
				).collect(Collectors.toCollection(ArrayList::new));
		ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList(data);
		tableView.setItems(oListItems);

	}

	@FXML
	public void onClearButtonClicked() {
		ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
		oListItems.addAll(getLatestData());
		tableView.setItems(oListItems);
		searchField.setText("");
	}

	public void filterItems() {
		String[] filterList = {"All", "Alert Item"};
		filterComboBox.getItems().addAll(filterList);
		filterComboBox.setOnAction(event -> {
			String selectedFilter = filterComboBox.getSelectionModel().getSelectedItem();
			if (Objects.equals(selectedFilter, "Alert Item")) {
//				ObservableList<Item> alertItems = FXCollections.observableArrayList();
				System.out.println("filtering...");
//				tableView.setItems(null);
			} else {
//				tableView.setItems(null);
				System.out.println("normal");
			}
		});
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			this.model = CustomTableView.getModel();

			ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();

			oListItems.addAll(getLatestData());

			this.columns = CustomTableView.getColumns();
			List<String> columnNames = List.of(this.columns);

			this.tableView.setRowFactory(tv -> new TableRow<>());
			for (String columnName : columnNames) {
				TableColumn<HashMap<String, String>, String> column = new TableColumn<>(columnName);

				column.setCellValueFactory(cellData ->
						new SimpleStringProperty(cellData.getValue().get(Helper.toAttrString(columnName, Helper.AttrFormat.snake_case))));

				this.tableView.getColumns().add(column);
			}
			TableColumn<HashMap<String, String>, String> optionsColumns = this.getOptionsColumns();

			this.tableView.getColumns().add(optionsColumns);
			this.tableView.setItems(oListItems);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		SessionManager sessionManager = SessionManager.getInstance();
		String role_id = sessionManager.getUserData().get("role_id");
		this.addItemButton.setVisible(role_id.equals("1") || role_id.equals("2"));
	}

	public TableView<HashMap<String, String>> getTableView() { return this.tableView; }

	public static void setCommand(Command command) { CustomTableViewController.command = command; }

	public static Command getCommand() { return CustomTableViewController.command; }

	public CustomTableViewController getInstance() { return this; }

	private ObservableList<HashMap<String, String>> getLatestData() {
		ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
		try {
			QueryBuilder<?> qb = new QueryBuilder<>(model);
			String className = model.getSimpleName();
			if (!CustomTableView.getJoins().isEmpty()) {
				for (Class<? extends ModelInitializable> join: CustomTableView.getJoins()) {
					qb.joins(join, CustomTableView.getJoinKeys().get(CustomTableView.getJoins().indexOf(join)));
				}
			}
			oListItems.addAll(qb
					.select()
					.from("db/" + className + ".txt")
					.get());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return oListItems;
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
		CustomTableViewController.command.openEditModal(data);
	}

	private void handleDelete(HashMap<String, String> data) throws IOException {
		CustomTableViewController.command.openDeleteModal(data);
	}
}
