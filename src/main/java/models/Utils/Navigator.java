package models.Utils;

import javafx.scene.Parent;
import org.start.owsb.Layout;
import routes.Router;
import views.Inventory.GenerateStockReportView;
import views.Inventory.InventoryView;
import views.Inventory.InventoryUpdateRequestView;
import views.Inventory.StockManagementView;
import views.LoginView;
import views.PRPOView;
import views.UserRegistrationView;
import views.salesViews.ItemListView;

import java.util.HashMap;

public class Navigator {
	private static Navigator instance;
	private Layout layout;
	private static HashMap<String, Router> roleRoutes = new HashMap<>();

	private static void initRoutes() {
		Router allRouter = new Router();
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

		// Sales manager
		salesRouter.addRoute("home", UserRegistrationView.class);
		salesRouter.addRoute("item-list", ItemListView.class);

		// Purchase manager
		purchaseRouter.addRoute("PRPO", PRPOView.class);

		// Inventory manager
		inventoryRouter.addRoute("inventoryHome", InventoryView.class);
		inventoryRouter.addRoute("stockManagement", StockManagementView.class);
		inventoryRouter.addRoute("generateStockReport", GenerateStockReportView.class);
		inventoryRouter.addRoute("inventoryUpdateRequestView", InventoryUpdateRequestView.class);

		// Finance manage

		// NON SIDEBAR ROUTES
		allRouter.addRoute("login", LoginView.class);

		roleRoutes.put("all", allRouter);
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
