package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class NotificationController implements Initializable {
	@FXML private Pane rootPane;
	@FXML private HBox notificationContainer;
	@FXML private Text notificationLabel;

	public enum popUpType {
		error, warning, info, success
	}
	public enum popUpPos {
		CENTER, TOP, BOTTOM_RIGHT
	}
	public void setType(popUpType type) {
		switch (type) {
			case error:
//		        notifactionImage.setImage(new Image("icons/" + type.toString() + ".png"));
				notificationContainer.setStyle("-fx-border-color: red; " +
						"-fx-border-radius: 5; " +
						"-fx-background-radius: 5; " +
						"-fx-background-color: rgba(248, 207, 207, 1)");
				break;
			case success:
//		        notifactionImage.setImage(new Image("icons/" + type.toString() + ".png"));
				notificationContainer.setStyle("-fx-border-color: green; " +
						"-fx-border-radius: 5; " +
						"-fx-background-radius: 5; " +
						"-fx-background-color: rgba(200, 238, 200, 1)");
				break;
		}
	}

	public void setMessage(String message) {
		this.notificationLabel.setText(message);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}
}
