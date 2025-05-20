package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import models.Datas.Role;
import models.Users.FinanceManager;
import models.Users.User;
import models.Utils.Helper;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;
import org.start.owsb.Layout;
import views.NotificationView;

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
		SessionManager session = SessionManager.getInstance();

		String SUPERUSER_USERNAME = "admin";
		String SUPERUSER_PASSWORD = "admin";
		HashMap<String, String> superUser = new HashMap<>();

		if (usernameField.getText().equals(SUPERUSER_USERNAME) && passwordField.getText().equals(SUPERUSER_PASSWORD)) {
			try {
				superUser.put("roleID", "1");
				superUser.put("username", SUPERUSER_USERNAME);
				superUser.put("password", SUPERUSER_PASSWORD);
				superUser.put("roleName", "Admin");
				session.setUserData(superUser);
				layout.initSidebar("admin", new String[]{"Register"});
				// Navigate to dashboard
				navigator.navigate(navigator.getRouters("admin").getRoute("register"));

				NotificationView notificationView = new NotificationView("Login successful", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
				notificationView.show();
				return;
			} catch (Exception e) {
				System.out.println("error"+ e.getMessage());
			}
		}

		String hashedPassword = Helper.SHA_Hashing(passwordField.getText());
		try {
			QueryBuilder<User> qb = new QueryBuilder<>(User.class);
			ArrayList<HashMap<String, String>> data = qb.select()
					.from("db/User.txt")
					.where("username", "=", usernameField.getText())
					.and("password", "=", hashedPassword)
					.joins(Role.class, "roleID")
					.get();

			if (!data.isEmpty()) {
				session.setUserData(data.get(0));

				switch (data.get(0).get("roleID")) {
					case "1":
						layout.initSidebar("admin", new String[]{"Register"});
						// Navigate to dashboard
						FXMLLoader test = new FXMLLoader(new URL("file:src/main/resources/org/start/owsb/test.fxml"));
						navigator.navigate(test.load());
						break;
					case "2":
						layout.initSidebar("sales", new String[]{"Home", "Manage Item List"});
						//Navigate to dashboard
						navigator.navigate(navigator.getRouters("sales").getRoute("home"));
						break;
					case "3":
						layout.initSidebar("purchase", new String[]{"Procurement Management"});
						//Navigate to dashboard
						navigator.navigate(navigator.getRouters("purchase").getRoute("PRPO"));
						break;
					case "4":
						layout.initSidebar("inventory", new String[]{"Register"});
						//Navigate to dashboard
//						navigator.navigate(navigator.getRouters("sales").getRoute("somewhere"));
						break;
					case "5":
						HashMap<String, String> FUD = data.get(0);
						session.setFinanceManagerData(new FinanceManager(FUD.get("userID"), FUD.get("username"), FUD.get("email"), FUD.get("password"), FUD.get("position"), Integer.parseInt(FUD.get("age")), FUD.get("roleID")));
						layout.initSidebar("finance", new String[]{"Home", "Procurement Management", "Financial Report", "Payments"});
						//Navigate to dashboard
						navigator.navigate(navigator.getRouters("finance").getRoute("financeHome"));
						break;
				}

				NotificationView notificationView = new NotificationView("Login successful", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
				notificationView.show();
			} else {
				NotificationView notificationView = new NotificationView("Invalid username or password", NotificationController.popUpType.error, NotificationController.popUpPos.BOTTOM_RIGHT);
				notificationView.show();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}
}
