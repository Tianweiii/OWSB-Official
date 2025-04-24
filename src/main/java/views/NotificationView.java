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
		FadeTransition fade = new FadeTransition(Duration.millis(500));
		PauseTransition pause = new PauseTransition(Duration.millis(3000));
		SequentialTransition sequentialTransition = new SequentialTransition(pause, fade);

		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();

		root.getChildren().add(this.notificationPane);
		this.notificationPane.boundsInLocalProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue.getWidth() > 0 && newValue.getHeight() > 0) {
				if (this.pos == NotificationController.popUpPos.CENTER) {
					double centerX = (root.getWidth() - newValue.getWidth()) / 2;
					double centerY = (root.getHeight() - newValue.getHeight()) / 2;
					this.notificationPane.setLayoutX(centerX);
					this.notificationPane.setLayoutY(centerY);

				} else if (this.pos == NotificationController.popUpPos.TOP) {
					double centerX = (root.getWidth() - newValue.getWidth()) / 2;
					double topY = newValue.getHeight() - 20;
					this.notificationPane.setLayoutX(centerX);
					this.notificationPane.setLayoutY(topY);

				} else if (this.pos == NotificationController.popUpPos.BOTTOM_RIGHT) {
					double rightX = root.getWidth() - newValue.getWidth() - 20;
					double bottomY = root.getHeight() - newValue.getHeight() - 20;
					this.notificationPane.setLayoutX(rightX);
					this.notificationPane.setLayoutY(bottomY);

				}
			}
		});

		fade.setNode(this.notificationPane);
		fade.setFromValue(1.0);
		fade.setToValue(0.0);
		fade.setOnFinished(event -> {
			root.getChildren().remove(this.notificationPane);
		});

		sequentialTransition.play();
	}
	@Override
	public Parent getView() {
		return this.notificationPane;
	}
}
