package org.start.owsb;

import controllers.SpinnerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Users.User;
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
        FXMLLoader spinnerSceneLoader = new FXMLLoader(getClass().getResource("test.fxml"));
        Parent root = (Parent) spinnerSceneLoader.load();

        SpinnerController ctrlrPointer = (SpinnerController) spinnerSceneLoader.getController();


        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
//        launch();
        //Test queryBuilder usage
        QueryBuilder<User> qb = new QueryBuilder<>(User.class);
        String[] columns = new String[]{"email", "password"};
        ArrayList<HashMap<String, String>> res = qb.select()
                .from("db/User.txt")
//                .where("email", "=", "many@mail.com")
                .sort("user_id", "desc")
                .get();
        qb.target("db/User.txt").values(new String[]{"Bobby","moooo@mail.com","123456","lol","30","1"}).create();
//        String roleid = res.get(0).get("role_id");
        System.out.println(res);

    }
}
