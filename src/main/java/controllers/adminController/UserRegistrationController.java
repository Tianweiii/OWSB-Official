package controllers.adminController;

import controllers.NotificationController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import models.Datas.Role;
import models.Users.User;
import models.Utils.Helper;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import views.NotificationView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class UserRegistrationController implements Initializable {
	@FXML private TextField usernameField = new TextField();
	@FXML private TextField emailField = new TextField();
	@FXML private TextField passwordField = new TextField();
	@FXML private ComboBox<Role> positionField = new ComboBox<>();
	@FXML private TextField ageField = new TextField();
	@FXML private Button registerButton;

	private ArrayList<Role> roles = new ArrayList<Role>();

	@FXML
	public void handleRegisterButtonClick() {
		try {
			String[] dataToAdd = new String[]{
					usernameField.getText(),
					emailField.getText(),
					Helper.SHA_Hashing(passwordField.getText()),
					positionField.getValue().getName(),
					ageField.getText(),
					String.valueOf(positionField.getValue().getId())
			};
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			ArrayList<HashMap<String, String>> data = qb.select(new String[]{"username"}).from("db/User.txt").get();

			for (HashMap<String, String> item: data) {
				if (item.get("username").equals(usernameField.getText())) {
					NotificationView notificationView = new NotificationView("Username already exists", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
					notificationView.show();
					return;
				}
			}

			String[] attrs = qb.getAttrs(false);

			for (String item: dataToAdd) {
				if (item == null || item.isEmpty()) {
					NotificationView notificationView = new NotificationView("Field(s) cannot be null", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
					notificationView.show();
					return;
				}
			}

			if (dataToAdd.length != attrs.length) {
				NotificationView notificationView = new NotificationView("Field(s) are not filled in", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
			}else {
				boolean res = qb.target("db/User.txt").values(dataToAdd).create();
				if (res) {
					FXMLLoader home = new FXMLLoader(new URL("file:src/main/resources/org/start/owsb/test.fxml"));
					Navigator navigator = Navigator.getInstance();
					navigator.navigate(home.load());
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void setComboBoxOptions(){
		try {
			QueryBuilder<Role> qb = new QueryBuilder<>(Role.class);
			this.roles = qb.select().from("db/Role.txt").getAsObjects();

		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
		positionField.getItems().addAll(this.roles);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		positionField.setConverter(new StringConverter<>() {
			@Override
			public String toString(Role object) {
				return object.getName();
			}

			@Override
			public Role fromString(String string) {
				return null;
			}
		});
	}
}
