package models.Utils;

import javafx.scene.Parent;
import org.start.owsb.Layout;
import routes.Router;
import views.Inventory.InventoryView;
import views.Inventory.InventoryUpdateRequestView;
import views.Inventory.StockManagementView;
import views.LoginView;
import views.PRPO.EditPRPOView;
import views.PRPO.PRPOView;
import views.UserRegistrationView;
import views.adminViews.AdminDashboardView;
import views.adminViews.UserListView;
import views.salesViews.DailyItemSalesView;
import views.salesViews.ItemListView;
import views.salesViews.SalesManagerDashboardView;
import views.salesViews.SupplierView;

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
		adminRouter.addRoute("dashboard", AdminDashboardView.class);
		adminRouter.addRoute("register", UserRegistrationView.class);
		adminRouter.addRoute("user-list", UserListView.class);
//		adminRouter.addRoute("fffff", UserRegistrationView.class);

		// Sales manager
		salesRouter.addRoute("home", SalesManagerDashboardView.class);
		salesRouter.addRoute("item-list", ItemListView.class);
		salesRouter.addRoute("supplier", SupplierView.class);
		salesRouter.addRoute("daily-sales", DailyItemSalesView.class);
		salesRouter.addRoute("PRPO", PRPOView.class);

		// Purchase manager
		purchaseRouter.addRoute("PRPO", PRPOView.class);
		purchaseRouter.addRoute("edit-PRPO", EditPRPOView.class);

		// Inventory manager
		inventoryRouter.addRoute("inventoryHome", InventoryView.class);
		inventoryRouter.addRoute("stockManagement", StockManagementView.class);
		inventoryRouter.addRoute("PRPO", PRPOView.class);
		inventoryRouter.addRoute("inventoryUpdateRequestView", InventoryUpdateRequestView.class);

		// Finance manage
		financeRouter.addRoute("PRPO", PRPOView.class);
		financeRouter.addRoute("edit-PRPO", EditPRPOView.class);

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
