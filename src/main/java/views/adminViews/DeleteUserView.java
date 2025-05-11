package views.adminViews;

import controllers.NotificationController;
import controllers.adminController.DeleteUserController;
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

public class DeleteUserView implements View {
	private Pane rootPane;
	private static UserListController controller;
	private static HashMap<String, String> data;

	public DeleteUserView(UserListController controller) throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/Admin/DeleteUserConfirmation.fxml"));
		this.rootPane = loader.load();
		DeleteUserView.controller = controller;
		DeleteUserController ctrl = loader.getController();
	}

	public static UserListController getController() {
		return DeleteUserView.controller;
	}

	public void show() {
		Layout layout = Layout.getInstance();
		layout.getRoot().getChildren().add(this.rootPane);
		Helper.adjustPanePosition(NotificationController.popUpPos.CENTER, layout.getRoot(), this.rootPane);

		controller.getRootPane().setDisable(true);
	}

	public static void setData(HashMap<String, String> data) {
		DeleteUserView.data = data;
	}

	public static HashMap<String, String> getData() {
		return DeleteUserView.data;
	}

	@Override
	public Parent getView() {
		return this.rootPane;
	}
}
