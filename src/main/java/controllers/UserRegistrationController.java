package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import models.Users.User;
import models.Utils.QueryBuilder;

import java.net.URL;
import java.util.ResourceBundle;

public class UserRegistrationController implements Initializable {
	@FXML private Button registerButton;

	@FXML
	public void handleRegisterButtonClick() {
		try {
			String[] dataToAdd = new String[]{"Bobby","moooo@mail.com","123456","lol","30","1"};
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			String[] attrs = qb.getAttrs(false);
			if (dataToAdd.length != attrs.length) {
				//TODO show error popup or error message
				throw new Exception("Data length does not match attribute length");
			}else {
				qb.target("db/User.txt").values(dataToAdd).create();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}
}
