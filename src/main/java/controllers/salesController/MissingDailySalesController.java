package controllers.salesController;

import controllers.SidebarController;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import views.salesViews.AddNewDailyItemSalesView;
import controllers.NotificationController;
import views.NotificationView;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MissingDailySalesController implements Initializable {

	public VBox container;
	@FXML private AnchorPane missingDailySalesPane;
	@FXML private Button createNewSalesEntryButton;
	private LocalDate salesDate;

	private Runnable onCreateCallback;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		playIntroAnimation();
	}

	private void playIntroAnimation() {
		TranslateTransition slide = new TranslateTransition(Duration.millis(400), missingDailySalesPane);
		slide.setFromY(20);
		slide.setToY(0);

		FadeTransition fade = new FadeTransition(Duration.millis(400), missingDailySalesPane);
		fade.setFromValue(0);
		fade.setToValue(1);

		slide.play();
		fade.play();
	}

	@FXML
	public void onCreateNewSalesEntryButtonClick() {
		try {
			AddNewDailyItemSalesView view = new AddNewDailyItemSalesView(this, AddNewDailyItemSalesView.Mode.FIRST);
			view.getAddNewDailyItemSalesController().initMode(AddNewDailyItemSalesView.Mode.FIRST, null, this.salesDate);
			view.show();
			missingDailySalesPane.setDisable(true);
			SidebarController.getSidebar().setDisable(true);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				new NotificationView(
						"Unable to open entry form",
						NotificationController.popUpType.error,
						NotificationController.popUpPos.BOTTOM_RIGHT
				).show();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}


	public void notifyEntryCreated() {
		missingDailySalesPane.setDisable(false);
		if (onCreateCallback != null) {
			onCreateCallback.run();
		}
	}

	public void setOnCreateCallback(Runnable callback) {
		this.onCreateCallback = callback;
	}

	public Pane getRootPane() {
		return missingDailySalesPane;
	}

	public Button getCreateButton() {
		return createNewSalesEntryButton;
	}

	public void reload() {

	}

	public void setSalesDate(LocalDate salesDate) {
		this.salesDate = salesDate;
	}
}
