package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import models.Utils.Navigator;
import routes.Router;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

	private String[] sidebarItems;
	private String sidebarType;
	@FXML
	private GridPane sidebarGrid;

	public void setSidebarItems(String sidebarType, String[] sidebarItems) {
		this.sidebarType = sidebarType;
		this.sidebarItems = sidebarItems;
	}

	public void setText() {
		Navigator navigator = Navigator.getInstance();
		Router router = navigator.getRouters(sidebarType);
		String[] routerPaths = router.getRoutePaths();
		System.out.println(Arrays.toString(routerPaths));

		if (routerPaths.length != sidebarItems.length) {
			throw new IllegalArgumentException("Number of sidebar items does not match number of routes");
		}

		for(int i = 0; i < sidebarItems.length; i++) {
			Button button = new Button(sidebarItems[i]);
			button.setPadding(new Insets(5, 5, 5, 15));
			button.setBackground(null);

			int finalI = i;
			button.setOnAction((actionEvent) -> {
				navigator.navigate(router.getRoute(routerPaths[finalI]));
			});

			sidebarGrid.add(button, 0, i);
		}
	}
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}
}
