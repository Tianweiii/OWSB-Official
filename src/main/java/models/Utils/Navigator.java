package models.Utils;

import javafx.scene.Parent;
import org.start.owsb.Layout;
import routes.Router;
import views.SidebarView;
import views.UserRegistrationView;
import views.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Navigator {
	private static Navigator instance;
	private Layout layout;
	private static HashMap<String, Router> roleRoutes = new HashMap<>();

	private static void initRoutes() {
		Router adminRouter =  new Router();
		Router salesRouter =  new Router();
		Router purchaseRouter =  new Router();
		Router inventoryRouter =  new Router();
		Router financeRouter =  new Router();

		// Ensure routes are the same order as labels in the sidebar
		// Admin routes
		adminRouter.addRoute("register", UserRegistrationView.class);
//		adminRouter.addRoute("EEEE", SidebarView.class);
//		adminRouter.addRoute("fffff", UserRegistrationView.class);

		// Inventory manager routes

		// Finance manage routes

		// Sales manager routes

		// Purchase manager routes

		roleRoutes.put("admin", adminRouter);
		roleRoutes.put("sales", salesRouter);
		roleRoutes.put("purchase", purchaseRouter);
		roleRoutes.put("inventory", inventoryRouter);
		roleRoutes.put("finance", financeRouter);
	}

	public static Navigator getInstance() {
		if (instance == null) {
			instance = new Navigator();
			initRoutes();
		}
		return instance;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public void navigate(Parent view) {
		this.layout.setView(view);
	}

	public Router getRouters(String role) {
		return roleRoutes.get(role);
	}
}
