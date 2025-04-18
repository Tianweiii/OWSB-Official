package org.start.owsb;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import views.SidebarView;

import java.io.IOException;

public class Layout {
	private final BorderPane root = new BorderPane();
	private SidebarView sidebarView = new SidebarView();

	public Layout() throws IOException {
		root.setLeft(sidebarView.getView());
	}

	public void setView(Parent view) {
		root.setCenter(view);
	}

	public BorderPane getRoot() {
		return root;
	}
}
