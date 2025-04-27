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
	private AnchorPane sidebarController;
	//Init sidebar on login

	public SidebarView(String sidebarType, String[] sidebarItems) throws IOException {
		FXMLLoader sidebar = new FXMLLoader(getClass().getResource("/Components/Sidebar.fxml"));
//				new URL("file:src/main/resources/Components/Sidebar.fxml"));
		this.sidebarController = sidebar.load();

		SidebarController ctrlPointer = sidebar.getController();
		ctrlPointer.setSidebarItems(sidebarType, sidebarItems);
		ctrlPointer.setText();
	}

	public Parent getView() {
		return this.sidebarController;
	}
}
