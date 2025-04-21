package org.start.owsb;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import views.SidebarView;

import java.io.IOException;

public class Layout {
	private final BorderPane root = new BorderPane();
	private SidebarView sidebarView;
	private static Layout instance;

	public static Layout getInstance() throws IOException {
		if (instance == null) {
			instance = new Layout();
		}
		return instance;
	}

	public void initSidebar(String[] options) throws IOException {
		sidebarView = new SidebarView(options);
		root.setLeft(sidebarView.getView());
	}

	public void setView(Parent view) {
		root.setCenter(view);
	}

	public BorderPane getRoot() {
		return root;
	}
}
