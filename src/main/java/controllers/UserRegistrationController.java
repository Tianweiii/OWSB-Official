package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import models.Datas.Role;
import models.Users.User;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;

import java.net.URL;
import java.util.ArrayList;
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
					passwordField.getText(),
					positionField.getValue().getName(),
					ageField.getText(),
					String.valueOf(positionField.getValue().getId())
			};
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			String[] attrs = qb.getAttrs(false);

			for (String item: dataToAdd) {
				if (item == null || item.isEmpty()) {
					//TODO show error popup or error message
					throw new Exception("Data cannot be null");
				}
			}

			if (dataToAdd.length != attrs.length) {
				//TODO show error popup or error message
				throw new Exception("Data length does not match attribute length");
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
