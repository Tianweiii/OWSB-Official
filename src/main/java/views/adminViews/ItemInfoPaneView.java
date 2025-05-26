package views.adminViews;

import controllers.adminController.AdminDashboardController;
import controllers.adminController.ItemInfoController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import models.DTO.ItemListDTO;
import views.View;

import java.net.URL;

public class ItemInfoPaneView implements View {
	private VBox itemInfoPane;

	public ItemInfoPaneView(ItemListDTO data, AdminDashboardController rootController) {
		try {
			FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/Admin/ItemInfoPane.fxml"));
			this.itemInfoPane = loader.load();
			ItemInfoController ctrl = loader.getController();
			ctrl.setItemData(data);
			ctrl.setRootController(rootController);
			ctrl.initLabels();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Parent getView() {
		return this.itemInfoPane;
	}
}
