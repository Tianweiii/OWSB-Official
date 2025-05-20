package models.Utils;

import javafx.scene.Parent;
import org.start.owsb.Layout;
import routes.Router;
import views.FinanceViews.*;
import views.LoginView;
import views.PRPOView;
import views.UserRegistrationView;
import views.salesViews.ItemListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Navigator {
	private static Navigator instance;
	private Layout layout;
	private static HashMap<String, Router> roleRoutes = new HashMap<>();

	private Parent prevFile;
	private final ArrayList<Parent> stackList = new ArrayList<>();

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

		// Finance manager
		financeRouter.addRoute("financeHome", FinanceHomeView.class);
		financeRouter.addRoute("financeReport", FinanceReportView.class);
		financeRouter.addRoute("financePayments", FinancePaymentsView.class);
		financeRouter.addRoute("makePayments", MakePaymentsView.class);
		financeRouter.addRoute("viewAllSales", ViewAllSales.class);
		financeRouter.addRoute("viewAllPayments", ViewAllPayments.class);
		financeRouter.addRoute("paymentSuccess", PaymentSuccessView.class);

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
		// this is to not add same page to stack list is same
		Parent last = stackList.get(stackList.size() - 1);
		if (!Objects.equals(last, prevFile)) stackList.add(prevFile);
		this.layout.setView(view);

		prevFile = view;
	}

	public void goBack() {
		int index = stackList.size() - 1;
		Parent path = index < 0 ? getRouters("all").getRoute("login") : stackList.get(index);
		navigate(path);
		stackList.remove(stackList.size() - 1);
	}

	public Router getRouters(String role) {
		return roleRoutes.get(role);
	}

	public ArrayList<Parent> getStackList() {
		return stackList;
	}

	public void setPrevFile(Parent prevFile) {
		this.prevFile = prevFile;
	}

//	public void renderLoader() {
//		this.layout.renderLoader();
//	}
//
//	public void removeLoader() {
//		this.layout.removeLoader();
//	}
}
