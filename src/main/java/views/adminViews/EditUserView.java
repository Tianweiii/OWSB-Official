package views.adminViews;

import controllers.NotificationController;
import controllers.adminController.EditUserController;
import controllers.adminController.UserListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import models.Utils.Helper;
import org.start.owsb.Layout;
import views.View;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class EditUserView implements View {
	private static UserListController controller;
	private static HashMap<String, String> data;
	private final Pane editUserPane;

	public EditUserView(UserListController controller) throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/Admin/EditUser.fxml"));
		this.editUserPane = loader.load();
		EditUserView.controller = controller;

		EditUserController ctrl = loader.getController();
		ctrl.setComboBoxOptions();
	}

	public void show() {
		Layout layout = Layout.getInstance();
		layout.getRoot().getChildren().add(this.editUserPane);
		Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), this.editUserPane);

		controller.getRootPane().setDisable(true);
	}

	public static UserListController getController() {
		return EditUserView.controller;
	}

	public static void setData(HashMap<String, String> data) {
		EditUserView.data = data;
	}

	public static HashMap<String, String> getData() {
		return EditUserView.data;
	}

	@Override
	public Parent getView() {
		return this.editUserPane;
	}
}
