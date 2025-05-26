package controllers.adminController;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
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
		this.scaleOut(rootController.getItemInfoPane(), this.itemInfoRoot, () -> {});
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

	public void scaleOut(BorderPane root, VBox paneToRemove, Runnable onComplete) {
		Scale scaleTransform = new Scale();
		scaleTransform.setPivotX(0);
		scaleTransform.setPivotY(paneToRemove.getBoundsInLocal().getHeight() / 2);
		paneToRemove.getTransforms().add(scaleTransform);

		Timeline scaleOut = new Timeline(
				new KeyFrame(Duration.millis(150),
						new KeyValue(scaleTransform.xProperty(), 0.0, Interpolator.EASE_IN),
						new KeyValue(scaleTransform.yProperty(), 0.0, Interpolator.EASE_IN)
				)
		);

		scaleOut.setOnFinished(e -> {
			paneToRemove.getTransforms().remove(scaleTransform);  // Clean up
			root.getChildren().remove(paneToRemove);
			if (onComplete != null) onComplete.run();
		});

		scaleOut.play();
	}
}
