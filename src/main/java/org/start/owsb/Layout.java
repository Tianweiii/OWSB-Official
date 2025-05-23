package org.start.owsb;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import views.SidebarView;

import java.io.IOException;

public class Layout {
	private final BorderPane root = new BorderPane();
	private static Layout instance;

	public static Layout getInstance() {
		if (instance == null) {
			instance = new Layout();
		}
		return instance;
	}

	public void initSidebar(String type, String[] options) throws IOException {
		SidebarView sidebarView = new SidebarView(type, options);
		root.setLeft(sidebarView.getView());
	}

	public void setView(Parent view) {
		setCenterWithScaleTransition(root, view, 200);
	}

	public static void setCenterWithScaleTransition(BorderPane borderPane, Node newCenter, double duration) {
		Node currentCenter = borderPane.getCenter();

		if (currentCenter != null) {
			Scale scaleTransform = new Scale();
			scaleTransform.setPivotX(0);
			scaleTransform.setPivotY(currentCenter.getBoundsInLocal().getHeight() / 2);
			currentCenter.getTransforms().add(scaleTransform);

			Timeline scaleOut = new Timeline(
					new KeyFrame(Duration.millis(duration),
							new KeyValue(scaleTransform.xProperty(), 0.0),
							new KeyValue(scaleTransform.yProperty(), 0.0)
					)
			);

			scaleOut.setOnFinished(e -> {
				currentCenter.getTransforms().remove(scaleTransform);

				borderPane.setCenter(newCenter);

				Scale newScaleTransform = new Scale();
				newScaleTransform.setPivotX(0);
				newScaleTransform.setPivotY(currentCenter.getBoundsInLocal().getHeight() / 2);
				newScaleTransform.setX(0.0);
				newScaleTransform.setY(0.0);
				newCenter.getTransforms().add(newScaleTransform);

				Timeline scaleIn = new Timeline(
						new KeyFrame(Duration.millis(duration),
								new KeyValue(newScaleTransform.xProperty(), 1.0),
								new KeyValue(newScaleTransform.yProperty(), 1.0)
						)
				);

				scaleIn.setOnFinished(event -> {
					newCenter.getTransforms().remove(newScaleTransform);
				});

				scaleIn.play();
			});

			scaleOut.play();
		} else {
			borderPane.setCenter(newCenter);
		}
	}

	public static void fadeIn(BorderPane root, Parent paneToAdd) {
		paneToAdd.setOpacity(0);

		root.setCenter(paneToAdd);

		FadeTransition fadeIn = new FadeTransition(Duration.millis(300), paneToAdd);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.play();
	}

	// FOR MODALS
	public static void fadeScaleIn(BorderPane root, Node paneToAdd) {
		paneToAdd.setOpacity(0);
		paneToAdd.setScaleX(0.8);
		paneToAdd.setScaleY(0.8);

		root.getChildren().add(paneToAdd);

		ParallelTransition parallel = new ParallelTransition();

		FadeTransition fade = new FadeTransition(Duration.millis(300), paneToAdd);
		fade.setFromValue(0.0);
		fade.setToValue(1.0);

		ScaleTransition scale = new ScaleTransition(Duration.millis(300), paneToAdd);
		scale.setFromX(0.8);
		scale.setFromY(0.8);
		scale.setToX(1.0);
		scale.setToY(1.0);
		scale.setInterpolator(Interpolator.EASE_OUT);

		parallel.getChildren().addAll(fade, scale);
		parallel.play();
	}

	public BorderPane getRoot() {
		return root;
	}
}