package models.Utils;

import javafx.scene.Parent;
import org.start.owsb.Layout;

public class Navigator {
	private static Navigator instance;
	private Layout layout;

	public static Navigator getInstance() {
		if (instance == null) {
			instance = new Navigator();
		}
		return instance;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public void navigate(Parent view) {
		this.layout.setView(view);
	}
}
