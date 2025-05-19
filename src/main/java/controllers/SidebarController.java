package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import models.Utils.Navigator;
import models.Utils.SessionManager;
import org.start.owsb.Layout;
import routes.Router;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import static java.lang.Integer.MAX_VALUE;

public class SidebarController implements Initializable {

	private String[] sidebarItems;
	private final ArrayList<Button> sidebarButtons = new ArrayList<>();
	private String sidebarType;
	private static AnchorPane sidebarPane;
	@FXML private AnchorPane sidebarRoot;
	@FXML
	private GridPane sidebarGrid;
	@FXML private Button logoutButton;
	@FXML private Label usernameLabel;
	@FXML private Label positionLabel;

	public void setSidebarItems(String sidebarType, String[] sidebarItems) {
		this.sidebarType = sidebarType;
		this.sidebarItems = sidebarItems;
		SidebarController.sidebarPane = sidebarRoot;
	}

	public void setText() {
		Navigator navigator = Navigator.getInstance();
		Router router = navigator.getRouters(sidebarType);
		String[] routerPaths = router.getRoutePaths();

		for(int i = 0; i < sidebarItems.length; i++) {
			HBox hBox = new HBox();
			Button button = new Button(sidebarItems[i]);
			button.setAlignment(Pos.CENTER_LEFT);
			button.setPadding(new Insets(5, 5, 5, 15));
			button.setBackground(null);
			button.setMaxWidth(MAX_VALUE);
			HBox.setHgrow(button, Priority.ALWAYS);

			hBox.getChildren().add(button);
			sidebarButtons.add(button);
			int finalI = i;
			button.setOnAction((actionEvent) -> {
				sidebarButtons.forEach(button1 -> {
					button1.setBackground(null);
					button1.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
				});
				button.setStyle("-fx-background-color: #CFDBF0; -fx-text-fill: #092165; -fx-font-weight: bold;");
				navigator.navigate(router.getRoute(routerPaths[finalI]));
			});

			sidebarGrid.add(hBox, 0, i);
		}

		SessionManager session = SessionManager.getInstance();
		HashMap<String, String> user = session.getUserData();
		usernameLabel.setText(user.get("username"));
		positionLabel.setText(user.get("roleName"));
	}

	public static AnchorPane getSidebar() {
		return SidebarController.sidebarPane;
	}

	@FXML
	public void handleLogoutButtonClick() {
		Navigator navigator = Navigator.getInstance();
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();

		navigator.navigate(navigator.getRouters("all").getRoute("login"));
		root.setLeft(null);

	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}

	public void highlightHomeButton() {
		sidebarButtons.get(0).setStyle("-fx-background-color: #CFDBF0; -fx-text-fill: #092165; -fx-font-weight: bold;");
	}
}
