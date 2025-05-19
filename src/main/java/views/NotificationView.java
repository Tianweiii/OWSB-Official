package views;

import controllers.NotificationController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import models.Utils.Helper;
import org.start.owsb.Layout;

import java.io.IOException;
import java.net.URL;

public class NotificationView implements View{
	private Pane notificationPane;

	private NotificationController.popUpType type;
	private String message;
	private NotificationController.popUpPos pos;

	public NotificationView(String message, NotificationController.popUpType type, NotificationController.popUpPos pos) throws IOException {
		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/Components/Notification.fxml"));
		this.notificationPane = loader.load();

		NotificationController controller = loader.getController();
		controller.setMessage(message);
		controller.setType(type);
		this.pos = pos;
	}

	public void show() {
		FadeTransition fadeIn = new FadeTransition(Duration.millis(500));
		PauseTransition pause = new PauseTransition(Duration.millis(3000)); // Show for 3 seconds
		FadeTransition fadeOut = new FadeTransition(Duration.millis(500));

		SequentialTransition sequentialTransition = new SequentialTransition(fadeIn, pause, fadeOut);

		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();

		root.getChildren().add(this.notificationPane);
		Helper.adjustPanePosition(this.pos, root, this.notificationPane);

		fadeIn.setNode(this.notificationPane);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);

		fadeOut.setNode(this.notificationPane);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		fadeOut.setOnFinished(event -> {
			root.getChildren().remove(this.notificationPane);
		});

		sequentialTransition.play();
	}
	@Override
	public Parent getView() {
		return this.notificationPane;
	}
}
