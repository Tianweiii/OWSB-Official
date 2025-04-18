package org.start.owsb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Users.User;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;

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
        Layout layout = new Layout();
        Navigator navigator = Navigator.getInstance();
        FXMLLoader home = new FXMLLoader(getClass().getResource("test.fxml"));
        navigator.setLayout(layout);
        navigator.navigate(home.load());

        Scene scene = new Scene(layout.getRoot());
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        launch();
        //Test queryBuilder usage
        QueryBuilder<User> qb = new QueryBuilder<>(User.class);
        String[] columns = new String[]{"email", "password"};
        ArrayList<HashMap<String, String>> res = qb.select()
                .from("db/User.txt")
//                .where("email", "=", "many@mail.com")
                .get();
//        qb.target("db/User.txt").values(new String[]{"Bobby","moooo@mail.com","123456","lol","30","1"}).create();
//        String roleid = res.get(0).get("role_id");
        System.out.println(res);
    }
}
