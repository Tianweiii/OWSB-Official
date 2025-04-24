package org.start.owsb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        
    }

    public static void main(String[] args) {
//        launch();
        try {
            QueryBuilder<User> qb = new QueryBuilder<>(User.class);
            ArrayList<HashMap<String, String>> data = qb
                    .select()
                    .from("db/User.txt")
                    .joins(Role.class, "role_id")
                    .get();

            System.out.println(data.get(5));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
