package views.adminViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;
import java.net.URL;

public class AdminDashboardView implements View {
	private final AnchorPane rootPane;

	public AdminDashboardView() throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/Admin/AdminDashboard.fxml"));
		this.rootPane = loader.load();
	}

	@Override
	public Parent getView() {
		return this.rootPane;
	}
}
