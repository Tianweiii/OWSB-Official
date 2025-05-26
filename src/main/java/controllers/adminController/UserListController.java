package controllers.adminController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import models.Datas.Role;
import models.Users.User;
import models.Utils.Helper;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import views.adminViews.DeleteUserView;
import views.adminViews.EditUserView;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class UserListController implements Initializable {
	private String[] columns;
	@FXML private AnchorPane rootPane;
	@FXML private ComboBox<String> filterComboBox;
	@FXML private TextField searchField;
	@FXML private Button btnSearch;
	@FXML private Button btnClear;
	@FXML private Button addUserButton;
	@FXML private TableView<HashMap<String, String>> tableView;

	@FXML
	public void onAddUserButtonClicked() {
		Navigator navigator = Navigator.getInstance();
		navigator.navigate(navigator.getRouters("admin").getRoute("register"));
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
										item.get(Helper.toAttrString(column))
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
		tableView.refresh();
		searchField.setText("");
	}

	public void setupFilter() {
		String[] filterList = {"All", "Admin", "Sales Manager", "Inventory Manager", "Finance Manager", "Purchase Manager" };
		filterComboBox.getItems().addAll(filterList);
		filterComboBox.setOnAction(event -> {
			String selectedFilter = filterComboBox.getSelectionModel().getSelectedItem();
			ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
			if (selectedFilter.equals("All")) {
				oListItems.addAll(getLatestData());
				tableView.setItems(oListItems);
				tableView.refresh();
			} else {
				switch (selectedFilter) {
					case "Admin":
						oListItems.addAll(getLatestData().stream().filter(item -> item.get("roleID").equals("1")).toList());
						tableView.setItems(oListItems);
						tableView.refresh();
						break;
					case "Sales Manager":
						oListItems.addAll(getLatestData().stream().filter(item -> item.get("roleID").equals("2")).toList());
						tableView.setItems(oListItems);
						tableView.refresh();
						break;
					case "Purchase Manager":
						oListItems.addAll(getLatestData().stream().filter(item -> item.get("roleID").equals("3")).toList());
						tableView.setItems(oListItems);
						tableView.refresh();
						break;
					case "Inventory Manager":
						oListItems.addAll(getLatestData().stream().filter(item -> item.get("roleID").equals("4")).toList());
						tableView.setItems(oListItems);
						tableView.refresh();
						break;
					case "Finance Manager":
						oListItems.addAll(getLatestData().stream().filter(item -> item.get("roleID").equals("5")).toList());
						tableView.setItems(oListItems);
						tableView.refresh();
						break;
				}
			}
		});
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();

			oListItems.addAll(getLatestData());

			this.columns = new String[]{"User ID", "Username", "Email", "Position", "Age"};
			List<String> columnNames = List.of(this.columns);

			this.tableView.setRowFactory(tv -> new TableRow<>());
			for (String columnName : columnNames) {
				TableColumn<HashMap<String, String>, String> column = new TableColumn<>(columnName);

				column.setCellValueFactory(cellData ->
						new SimpleStringProperty(cellData.getValue().get(Helper.toAttrString(columnName))));

				this.tableView.getColumns().add(column);
			}
			TableColumn<HashMap<String, String>, String> optionsColumns = this.getOptionsColumns();

			this.tableView.getColumns().add(optionsColumns);
			this.tableView.setItems(oListItems);

			this.setupFilter();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public TableView<HashMap<String, String>> getTableView() { return this.tableView; }

	private ObservableList<HashMap<String, String>> getLatestData() {
		ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList();
		try {
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			String className = qb.getClassName();
			oListItems.addAll(qb
					.select()
					.from("db/" + className + ".txt")
					.joins(Role.class, "roleID")
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
		EditUserView.setData(data);

		EditUserView editUserView = new EditUserView(this);
		editUserView.show();
	}

	private void handleDelete(HashMap<String, String> data) throws IOException {
		DeleteUserView.setData(data);

		DeleteUserView deleteUserView = new DeleteUserView(this);
		deleteUserView.show();
	}

	public AnchorPane getRootPane() {
		return this.rootPane;
	}
}
