package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

	private String[] sidebarItems;
	@FXML
	private GridPane sidebarGrid;

	public void setSidebarItems(String[] sidebarItems) {
		this.sidebarItems = sidebarItems;
	}

	public void setText() {
		for(int i = 0; i < sidebarItems.length; i++) {
			Button button = new Button(sidebarItems[i]);
			button.setPadding(new Insets(5, 5, 5, 15));
			button.setBackground(null);
			sidebarGrid.add(button, 0, i);
		}
	}
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}
}
