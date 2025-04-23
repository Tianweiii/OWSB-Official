package org.start.owsb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Users.User;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import views.InventoryView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        Parent parent = FXMLLoader.load(getClass().getResource("login.fxml"));
//        Scene scene = new Scene(parent);
//        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
//        stage.setTitle("OWSB");
//
//        stage.setScene(scene);
//        stage.show();

        // temp test
//        FXMLLoader sidebar = new FXMLLoader(new URL("file:src/main/resources/Components/Sidebar.fxml"));
//        sidebar.load();
        Layout layout = Layout.getInstance();
        Navigator navigator = Navigator.getInstance();
        navigator.setLayout(layout);
        layout.initSidebar(new String[]{
                "Home",
                "Stock Management",
                "Procurement Management",
                "Sales Request"
        });
        //Set login page or initial landing page here
//        FXMLLoader inventoryHome = new FXMLLoader(getClass().getResource("test.fxml"));
        InventoryView stockView = new InventoryView();
        navigator.navigate(stockView.getView());

        Scene scene = new Scene(layout.getRoot());
        scene.getStylesheets().add(getClass().getResource("/css/Inventory.css").toExternalForm());
        stage.setTitle("OWSB Inventory System");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();

        //Init sidebar code
        // Layout layout = Layout.getInstance();
        // layout.initSidebar(new String[]{"Home", "Manage Supplier List", "Submit Daily Sales Entry", "Submit Daily Sales Entry", "Create Purchase Request"});

    }

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        launch(args);
        //Test queryBuilder usage
        QueryBuilder<User> qb = new QueryBuilder<>(User.class);
        String[] columns = new String[]{"email", "password"};
        ArrayList<HashMap<String, String>> res = qb.select()
                .from("db/User.txt")
//                .where("user_id", ">", "2")
                .sort("user_id", "desc")
                .get();
//        qb.target("db/User.txt").values(new String[]{"Bobby","moooo@mail.com","123456","lol","30","1"}).create();
//        String roleid = res.get(0).get("role_id");
        System.out.println(res);
    }
}
