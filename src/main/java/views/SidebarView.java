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
		FXMLLoader sidebar = new FXMLLoader(new URL("file:src/main/resources/Components/Sidebar.fxml"));
		this.sidebarController = sidebar.load();
		//Sidebar scaling to screen size
		this.sidebarController.setMinHeight(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		this.sidebarController.setPrefWidth(Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.15);
		this.sidebarController.setMaxWidth(Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.15);

		ObservableList<Pane> list = this.sidebarController.getChildren().stream().map(x  -> {
			Pane pane = (Pane) x;
			pane.setPrefWidth(Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.15);
			return pane;
		}).collect(Collectors.toCollection(FXCollections::observableArrayList));

		SidebarController ctrlPointer = sidebar.getController();
		ctrlPointer.setSidebarItems(sidebarType, sidebarItems);
		ctrlPointer.setText();
	}

	public Parent getView() {
		return this.sidebarController;
	}
}
