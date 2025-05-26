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
import models.Utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.HashMap;

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
        stage.setTitle("OWSB Purchase Order Management System");
        scene.getStylesheets().getClass().getResource("/css/general.css");
        stage.setTitle("OWSB Purchase Order Management System");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();

        //Responsiveness: toggle the "narrow" class on your root-pane when scene width < 600
        scene.widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() < 600) {
                layout.getRoot().getStyleClass().add("narrow");
            } else {
                layout.getRoot().getStyleClass().remove("narrow");
            }
        });

    }

    public static void main(String[] args) {
        launch();
//        SessionManager ins = SessionManager.getInstance();
//        SessionManager.setCurrentPaymentPO(new PurchaseOrder("PO12", "PR12", "US12", "Office Equipments", 1231.25, "Verified"));
//        PaymentSuccessController.pressPrintReceipt();
    }
}
