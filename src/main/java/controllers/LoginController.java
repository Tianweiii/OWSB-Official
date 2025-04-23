package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import models.Users.User;
import models.Utils.Helper;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import org.start.owsb.Layout;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
	@FXML private Button loginButton;
	@FXML private TextField usernameField = new TextField();
	@FXML private TextField passwordField = new TextField();

	@FXML
	public void handleLoginButtonClick() {
		Navigator navigator = Navigator.getInstance();
		Layout layout = Layout.getInstance();
		String hashedPassword = Helper.SHA_Hashing(passwordField.getText());
		try {
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			ArrayList<HashMap<String, String>> data = qb.select()
					.from("db/User.txt")
					.where("username", "=", usernameField.getText())
					//TODO hash password first
					.and("password", "=", hashedPassword)
					.get();

			String SUPERUSER_USERNAME = "admin";
			String SUPERUSER_PASSWORD = "admin";
			System.out.println(usernameField.getText() + " " + passwordField.getText());
			if (usernameField.getText().equals(SUPERUSER_USERNAME) && passwordField.getText().equals(SUPERUSER_PASSWORD)) {
				layout.initSidebar("admin", new String[]{"Register"});
				// Navigate to dashboard
				FXMLLoader test = new FXMLLoader(new URL("file:src/main/resources/org/start/owsb/test.fxml"));
				navigator.navigate(navigator.getRouters("admin").getRoute("register"));
				System.out.println("superuser");
				return;
			}

			if (!data.isEmpty()) {
				switch (data.get(0).get("role_id")) {
					case "1":
						layout.initSidebar("admin", new String[]{"Register"});
						// Navigate to dashboard
						FXMLLoader test = new FXMLLoader(new URL("file:src/main/resources/org/start/owsb/test.fxml"));
						navigator.navigate(test.load());
						break;
					case "2":
						layout.initSidebar("sales", new String[]{"Test"});
						//Navigate to dashboard
						navigator.navigate(navigator.getRouters("sales").getRoute("register"));
						break;
					case "3":
						layout.initSidebar("purchase", new String[]{"Register"});
						//Navigate to dashboard
//						navigator.navigate(navigator.getRouters("sales").getRoute("somewhere"));
						break;
					case "4":
						layout.initSidebar("inventory", new String[]{"Register"});
						//Navigate to dashboard
//						navigator.navigate(navigator.getRouters("sales").getRoute("somewhere"));
						break;
					case "5":
						layout.initSidebar("finance", new String[]{"Register"});
						//Navigate to dashboard
//						navigator.navigate(navigator.getRouters("sales").getRoute("somewhere"));
						break;

				}
			} else {
				// To show error popup
				throw new Exception("Invalid username or password");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}
}
