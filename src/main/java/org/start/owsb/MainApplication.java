package org.start.owsb;

import controllers.FinanceController.FinanceMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Datas.Item;

import models.Datas.Role;
import models.Users.User;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;
import views.UserRegistrationView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Layout layout = Layout.getInstance();
        Navigator navigator = Navigator.getInstance();
        SessionManager session = SessionManager.getInstance();
        navigator.setLayout(layout);
        navigator.navigate(navigator.getRouters("all").getRoute("login"));

        Scene scene = new Scene(layout.getRoot());
        scene.getStylesheets().getClass().getResource("/css/general.css");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
//        try {
//            QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
////            qb.target("db/Item.txt").delete("11");
//            HashMap<String, String> newdata = new HashMap<>();
//            newdata.put("item_name", "testingg");
//            qb.target("db/Item.txt").update("1", newdata);
//            qb.target("db/Item.txt").update("2", new String[]{"ddjadjasid","2025-04-27","2025-04-27","1","1099","1"});
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
    }
}
