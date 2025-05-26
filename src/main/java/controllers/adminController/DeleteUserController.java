package controllers.adminController;

import controllers.NotificationController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import models.Datas.Role;
import models.Users.User;
import models.Utils.Helper;
import models.Utils.QueryBuilder;
import org.start.owsb.Layout;
import views.NotificationView;
import views.adminViews.DeleteUserView;
import views.adminViews.EditUserView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DeleteUserController implements Initializable {
	private HashMap<String, String> data;
	@FXML private Pane deleteUserPane;
	@FXML private Label userLabel;

	@FXML private Button confirmDeleteButton;
	@FXML private Button cancelDeleteButton;

	@FXML
	public void onDeleteUserButtonClick() {
		HashMap<String, String> data = DeleteUserView.getData();

		try {
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			boolean res = qb.target("db/User.txt").delete(data.get("userID"));
			if (res) {
				Layout layout = Layout.getInstance();
				BorderPane root = layout.getRoot();
				root.getChildren().remove(this.deleteUserPane);
				UserListController controller = DeleteUserView.getController();
				controller.getRootPane().setDisable(false);

				Platform.runLater(()-> {
					try {
						QueryBuilder<User> checkerQb = new QueryBuilder<>(User.class);
						ArrayList<HashMap<String, String>> latestData = checkerQb
								.select()
								.from("db/User.txt")
								.joins(Role.class, "roleID")
								.get();
						ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList(latestData);
						controller.getTableView().setItems(oListItems);
						controller.getTableView().refresh();

						NotificationView notificationView = new NotificationView("User deleted successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
						notificationView.show();
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				});
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@FXML
	public void onCancelDeleteUserButtonClick() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.deleteUserPane);

		UserListController controller = DeleteUserView.getController();
		controller.getRootPane().setDisable(false);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		HashMap<String, String> data = DeleteUserView.getData();
		this.userLabel.setText(data.get("username"));
	}
}
