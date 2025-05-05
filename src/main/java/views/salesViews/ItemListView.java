package views.salesViews;

import models.Datas.Item;
import models.Datas.Supplier;
import models.Utils.Helper;
import views.Command;
import views.CustomTableView;
import views.View;

import java.io.IOException;
import java.util.HashMap;

public class ItemListView extends CustomTableView implements View, Command {
	private AddItemView addItemView;
	private EditItemView editItemView;
	private DeleteConfirmationView deleteConfirmationView;

	public ItemListView() throws IOException {
		super(new String[]{"Item Id", "Item Name", "Supplier Name", "Created At", "Updated At"}, Item.class, Helper.createClassArray(Supplier.class), new String[]{"supplier_id"});
		CustomTableView.setCommand(this);
	}

	public void openModal() throws IOException {
		AddItemView addItemView = new AddItemView();
		addItemView.showAddItemView(CustomTableView.controller);

		this.addItemView = addItemView;
	}

	public void closeModal() {
		this.addItemView.hideAddItemView();
	}

	public void openEditModal(HashMap<String, String> data) throws IOException {
		EditItemView editItemView = new EditItemView(CustomTableView.controller);
		EditItemView.setData(data);

		editItemView.showEditItemPane();

		this.editItemView = editItemView;
	}

	public void closeEditModal() {
		this.editItemView.hideEditItemPane();
	}

	public void openDeleteModal(HashMap<String, String> data) throws IOException {
		DeleteConfirmationView.setData(data);
		DeleteConfirmationView deleteConfirmationView = new DeleteConfirmationView(CustomTableView.controller);

		deleteConfirmationView.showDeleteConfirmationView();

		this.deleteConfirmationView = deleteConfirmationView;
	}

	public void closeDeleteModal() {
		this.deleteConfirmationView.hideDeleteConfirmationView();
	}
}
