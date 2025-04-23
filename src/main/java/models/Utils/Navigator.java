package models.Utils;

import javafx.scene.Parent;
import org.start.owsb.Layout;
import routes.Router;
import views.UserRegistrationView;

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

		// SIDEBAR ROUTES
		// Ensure routes are the same order as labels in the sidebar
		// Admin
		adminRouter.addRoute("register", UserRegistrationView.class);
//		adminRouter.addRoute("EEEE", SidebarView.class);
//		adminRouter.addRoute("fffff", UserRegistrationView.class);

		// Inventory manager

		// Finance manage

		// Sales manager

		// Purchase manager

		// NON SIDEBAR ROUTES


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
