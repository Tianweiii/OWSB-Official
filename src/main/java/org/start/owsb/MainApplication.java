package org.start.owsb;

import controllers.FinanceController.FinanceMainController;
import controllers.FinanceController.PaymentSuccessController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.DTO.TransactionDTO;
import models.Datas.*;

import models.Users.FinanceManager;
import models.Users.User;
import models.Utils.FileIO;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;
import net.sf.jasperreports.engine.JRException;
import views.UserRegistrationView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Layout layout = Layout.getInstance();
        Navigator navigator = Navigator.getInstance();

        SessionManager session = SessionManager.getInstance();
        navigator.setLayout(layout);

        navigator.setPrevFile(navigator.getRouters("all").getRoute("login"));
        ArrayList<Parent> stackList = navigator.getStackList();
        stackList.add(navigator.getRouters("all").getRoute("login"));
        navigator.navigate(navigator.getRouters("all").getRoute("login"));

        Scene scene = new Scene(layout.getRoot());
        scene.getStylesheets().getClass().getResource("/css/general.css");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException, JRException {
//        QueryBuilder<PurchaseRequisition> qb = new QueryBuilder<>(PurchaseRequisition.class);
//        ArrayList<PurchaseRequisition> PRs = qb.select().from("db/PurchaseRequisition").getAsObjects();
//        System.out.println(PRs.get(0).getUserID());

        launch();
//        SessionManager ins = SessionManager.getInstance();
//        SessionManager.setCurrentPaymentPO(new PurchaseOrder("PO12", "PR12", "US12", "Office Equipments", 1231.25, "Verified"));
//        PaymentSuccessController.pressPrintReceipt();
    }
}
