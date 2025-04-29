package routes;

import javafx.scene.Parent;
import views.View;

import java.util.LinkedHashMap;

public class Router {
	private final LinkedHashMap<String, Class<? extends View>> routes = new LinkedHashMap<>();

	public void addRoute(String path, Class<? extends View> view) {
		routes.put(path, view);
	}

	public Parent getRoute(String path) {
		Parent view = null;
		try {
			Class<? extends View> route = routes.get(path);
			view = route.getDeclaredConstructor().newInstance().getView();

		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return view;
	}

	public String[] getRoutePaths() {
		return routes.keySet().toArray(new String[0]);
	}
}
