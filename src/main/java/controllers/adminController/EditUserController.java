package controllers.adminController;

import controllers.NotificationController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import models.Datas.Role;
import models.Users.User;
import models.Utils.Helper;
import models.Utils.QueryBuilder;
import models.Utils.Validation;
import org.start.owsb.Layout;
import views.NotificationView;
import views.adminViews.EditUserView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class EditUserController implements Initializable {
	private HashMap<String, String> data;
	@FXML private Pane editUserPane;
	@FXML private TextField editUsernameField = new TextField();
	@FXML private TextField editPasswordField = new TextField();
	@FXML private TextField editEmailField = new TextField();
	@FXML private ChoiceBox<Role> editPositionChoiceBox = new ChoiceBox<>();
	@FXML private TextField editAgeField = new TextField();

	@FXML private Button confirmEditButton;
	@FXML private Button cancelEditButton;

	@FXML
	public void onConfirmEditButtonClick() throws IOException {

		if (editUsernameField.getText().isEmpty() || editEmailField.getText().isEmpty() || editAgeField.getText().isEmpty()) {
			NotificationView notificationView = new NotificationView("Please fill in the missing fields", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
			notificationView.show();
			return;
		}

		if (!Validation.isValidEmail(editEmailField.getText())) {
			NotificationView notificationView = new NotificationView("Invalid email", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
			notificationView.show();
			return;
		}

		if (!Validation.isValidNumeric(editAgeField.getText())) {
			NotificationView notificationView = new NotificationView("Age must be numeric", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
			notificationView.show();
			return;
		} else {
			if (Integer.parseInt(editAgeField.getText()) < 0) {
				NotificationView notificationView = new NotificationView("Age cannot be negative", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
				notificationView.show();
				return;
			}
		}


		HashMap<String, String> data = EditUserView.getData();
		String username = editUsernameField.getText();
		String email = editEmailField.getText();
		String password = editPasswordField.getText();
		Role position = editPositionChoiceBox.getValue();
		String age = editAgeField.getText();

		try {
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			boolean res = qb.target("db/User.txt")
				.update(
					data.get("userID"),
					new String[]{
							username,
							email,
							password.isEmpty() ? data.get("password") : Helper.SHA_Hashing(password),
							position.getName(),
							age,
							position.getId()
					});

			if (res) {
				Layout layout = Layout.getInstance();
				BorderPane root = layout.getRoot();
				root.getChildren().remove(this.editUserPane);
				UserListController controller = EditUserView.getController();
				controller.getRootPane().setDisable(false);

				Platform.runLater(()-> {
					try {
						QueryBuilder<User> checkerQb = new QueryBuilder<>(User.class);
						ArrayList<HashMap<String, String>> existingData = checkerQb
								.select()
								.from("db/User.txt")
								.joins(Role.class, "roleID")
								.get();
						ObservableList<HashMap<String, String>> oListItems = FXCollections.observableArrayList(existingData);
						controller.getTableView().setItems(oListItems);
						controller.getTableView().refresh();

						NotificationView notificationView = new NotificationView("User updated successfully", NotificationController.popUpType.success, NotificationController.popUpPos.BOTTOM_RIGHT);
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
	public void onCancelEditButtonClick() throws IOException {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.editUserPane);

		UserListController controller = EditUserView.getController();
		controller.getRootPane().setDisable(false);
	}


	public void setComboBoxOptions(){
		try {
			QueryBuilder<Role> qb = new QueryBuilder<>(Role.class);
			ArrayList<Role> roles = qb.select().from("db/Role.txt").getAsObjects();

			this.editPositionChoiceBox.getItems().addAll(roles);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		editPositionChoiceBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(Role object) {
				return object.getName();
			}

			@Override
			public Role fromString(String string) {
				return null;
			}
		});

		HashMap<String, String> data = EditUserView.getData();

		editUsernameField.setText(data.get("username"));
		editEmailField.setText(data.get("email"));
		editPositionChoiceBox.setValue(Role.getRole(data.get("position")));
		editAgeField.setText(data.get("age"));
	}
}
