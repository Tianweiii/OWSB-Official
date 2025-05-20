package org.start.owsb;

import controllers.FinanceController.FinanceMainController;
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
import views.UserRegistrationView;

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
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }
}
