package controllers.salesController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import models.Datas.Item;
import models.Datas.Supplier;
import models.Utils.Helper;
import models.Utils.QueryBuilder;

import java.net.URL;
import java.util.*;

public class ItemListController implements Initializable {
	@FXML private Button addItemButton;
	@FXML private Button searchButton;
	@FXML private TextField sortByField;
	@FXML private TextField searchField;
	@FXML private TableView<HashMap<String, String>> itemTable;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
			ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList(qb
					.select(new String[]{"item_id", "item_name", "supplier_name", "created_at", "updated_at", "supplier_id"})
					.from("db/Item.txt")
					.joins(Supplier.class, "supplier_id")
					.get());

			List<String> columnNames = new ArrayList<>();
			columnNames.add("Item Id");
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

			itemTable.setItems(oListItems);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
