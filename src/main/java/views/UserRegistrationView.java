package views;

import controllers.adminController.UserRegistrationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class UserRegistrationView implements View{
	private final AnchorPane userRegistrationPane;

	public UserRegistrationView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/AuthFXML/registerUser.fxml"));
		this.userRegistrationPane = loader.load();

		UserRegistrationController controller = loader.getController();
		controller.setComboBoxOptions();
	}

	@Override
	public Parent getView() {
		return userRegistrationPane;
	}
}
