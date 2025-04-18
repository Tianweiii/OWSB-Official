package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
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
			Label label = new Label(sidebarItems[i]);
			label.setPadding(new Insets(5, 5, 5, 15));
			sidebarGrid.add(label, 0, i);
		}
	}
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}
}
