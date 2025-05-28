package views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;

public class LoginView implements View{
	private HBox loginPane;

	public LoginView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/AuthFXML/login.fxml"));
		this.loginPane = loader.load();
	}
	@Override
	public Parent getView() {
		return this.loginPane;
	}
}
