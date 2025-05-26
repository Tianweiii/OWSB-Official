package views.adminViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;
import java.net.URL;

public class UserListView implements View {
	private AnchorPane rootPane;

	public UserListView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/Admin/UserList.fxml"));
		this.rootPane = loader.load();
	}
	@Override
	public Parent getView() {
		return this.rootPane;
	}
}
