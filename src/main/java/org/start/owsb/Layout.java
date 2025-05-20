package org.start.owsb;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import views.SidebarView;

import java.io.IOException;

public class Layout {
	private final BorderPane root = new BorderPane();
//	private VBox loaderOverlay;
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
		root.setCenter(view);
	}

	public BorderPane getRoot() {
		return root;
	}

//	public void renderLoader() {
//		if (loaderOverlay != null) return; // avoid duplicates
//
//		loaderOverlay = new VBox();
//		loaderOverlay.setAlignment(Pos.CENTER);
//		loaderOverlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.6);");
//		loaderOverlay.setPrefSize(root.getWidth(), root.getHeight());
//
//		loaderOverlay.prefWidthProperty().bind(root.widthProperty());
//		loaderOverlay.prefHeightProperty().bind(root.heightProperty());
//
//		loaderOverlay.setMouseTransparent(false);
//
//		Group spinnerGroup = new Group();
//
//		Circle outerRing = new Circle(25, Color.TRANSPARENT);
//		outerRing.setStroke(Color.DODGERBLUE);
//		outerRing.setStrokeWidth(3);
//		outerRing.getStrokeDashArray().addAll(90.0, 10.0);
//
//		DropShadow glow = new DropShadow();
//		glow.setColor(Color.rgb(70, 150, 255, 0.7));
//		glow.setRadius(10);
//		outerRing.setEffect(glow);
//
//		spinnerGroup.getChildren().add(outerRing);
//
//		RotateTransition rotate = new RotateTransition(Duration.seconds(1.2), outerRing);
//		rotate.setByAngle(360);
//		rotate.setCycleCount(Animation.INDEFINITE);
//		rotate.setInterpolator(Interpolator.EASE_BOTH);
//		rotate.play();
//
//		loaderOverlay.getChildren().add(spinnerGroup);
//
//		root.getChildren().add(loaderOverlay); // adds on top
//	}
//
//	public void removeLoader() {
//		if (loaderOverlay != null) {
//			root.getChildren().remove(loaderOverlay);
//			loaderOverlay = null;
//		}
//	}
}