package org.start.owsb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Datas.Role;
import models.Users.User;
import models.Utils.Navigator;
import models.Utils.SessionManager;

import java.io.IOException;
import java.util.HashMap;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Layout layout = Layout.getInstance();
        Navigator navigator = Navigator.getInstance();
        SessionManager session = SessionManager.getInstance();
        navigator.setLayout(layout);


        // Add dummy user for testing
        HashMap<String, String> dummyUser = new HashMap<>();
        dummyUser.put("username", "inventory_JY");
        dummyUser.put("role_name", "4");
        session.setUserData(dummyUser);


        layout.initSidebar("inventory", new String[]{"Home", "Stock Management", "Generate Stock Report", "Sales Purchase Request List"});

        //Set login page or initial landing page here
//        FXMLLoader inventoryHome = new FXMLLoader(getClass().getResource("test.fxml"));
//        InventoryView stockView = new InventoryView();
        navigator.navigate(navigator.getRouters("inventory").getRoute("inventoryHome"));
//        navigator.navigate(navigator.getRouters("all").getRoute("login"));

        Scene scene = new Scene(layout.getRoot());
        scene.getStylesheets().add(getClass().getResource("/css/Inventory.css").toExternalForm());
        stage.setTitle("OWSB Inventory System");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }
}
