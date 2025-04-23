package org.start.owsb;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
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
		root.setCenter(view);
	}

	public BorderPane getRoot() {
		return root;
	}
}