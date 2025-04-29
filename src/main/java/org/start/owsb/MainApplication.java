package org.start.owsb;

import controllers.FinanceController.FinanceMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Users.FinanceManager;
import models.Utils.Navigator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
        //Set login page or initial landing page here
        FXMLLoader home = new FXMLLoader(getClass().getResource("/FinanceFXML/FinanceMain.fxml"));
        navigator.setLayout(layout);
        navigator.navigate(home.load());

        //Init sidebar code
        // Layout layout = Layout.getInstance();
        // layout.initSidebar(new String[]{"Home", "Manage Supplier List", "Submit Daily Sales Entry", "Submit Daily Sales Entry", "Create Purchase Request"});

        Scene scene = new Scene(layout.getRoot());
        scene.getStylesheets().getClass().getResource("/css/general.css");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
//        launch();
        //Test queryBuilder usage
//        QueryBuilder<User> qb = new QueryBuilder<>(User.class);
//        String[] columns = new String[]{"email", "password"};
//        ArrayList<HashMap<String, String>> res = qb.select()
//                .from("db/User.txt")
////                .where("user_id", ">", "2")
//                .sort("user_id", "desc")
//                .get();
////        qb.target("db/User.txt").values(new String[]{"Bobby","moooo@mail.com","123456","lol","30","1"}).create();
////        String roleid = res.get(0).get("role_id");
//        System.out.println(res);
        FinanceManager.sendReceipt("isaacchong0913@gmail.com", "Hi", "payment_report.pdf");
    }
}
