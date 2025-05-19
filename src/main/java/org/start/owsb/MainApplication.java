package org.start.owsb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Datas.Item;

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


        navigator.navigate(navigator.getRouters("all").getRoute("login"));

        Scene scene = new Scene(layout.getRoot());
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


