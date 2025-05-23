package controllers.adminController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import models.DTO.ItemListDTO;
import models.Utils.Navigator;

import java.net.URL;
import java.util.ResourceBundle;

public class ItemInfoController implements Initializable {
	private ItemListDTO itemData;
	private AdminDashboardController rootController;

	@FXML private VBox itemInfoRoot;

	@FXML private Label itemNameLabel;
	@FXML private Label descriptionLabel;
	@FXML private Label quantityLabel;
	@FXML private Label alertSettingLabel;
	@FXML private Label supplierLabel;

	@FXML private Button closeButton;
	@FXML private Button navigateButton;

	@FXML
	public void onCloseButtonClick() {
		rootController.getItemInfoPane().getChildren().remove(this.itemInfoRoot);
	}

	@FXML
	public void onNavigateButtonClick() {
		Navigator navigator = Navigator.getInstance();
		navigator.navigate(navigator.getRouters("inventory").getRoute("stockManagement"));
	}

	public void setItemData(ItemListDTO item) {
		this.itemData = item;
	}

	public void setRootController(AdminDashboardController rootController) {
		this.rootController = rootController;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}

	public void initLabels() {
		this.itemNameLabel.setText(itemData.getItemName());
		this.descriptionLabel.setText(itemData.getDescription());
		this.quantityLabel.setText(String.valueOf(itemData.getQuantity()));
		this.alertSettingLabel.setText(String.valueOf(itemData.getAlertSetting()));
		this.supplierLabel.setText(itemData.getSupplierName());
	}

}
