package views;

import controllers.SidebarController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;

public class SidebarView implements View{
	private final AnchorPane sidebarPane;

	public SidebarView(String sidebarType, String[] sidebarItems) throws IOException {
		FXMLLoader sidebar = new FXMLLoader(new URL("file:src/main/resources/Components/Sidebar.fxml"));
		this.sidebarPane = sidebar.load();

		SidebarController ctrlPointer = sidebar.getController();
		ctrlPointer.setSidebarItems(sidebarType, sidebarItems);
		ctrlPointer.setText();
		ctrlPointer.highlightHomeButton();
	}

	public Parent getView() {
		return this.sidebarPane;
	}
}
