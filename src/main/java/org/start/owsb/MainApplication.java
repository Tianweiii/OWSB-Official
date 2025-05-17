package org.start.owsb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import models.Utils.Navigator;
import models.Utils.SessionManager;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Layout layout = Layout.getInstance();
        Navigator navigator = Navigator.getInstance();
        SessionManager session = SessionManager.getInstance();
        navigator.setLayout(layout);
        navigator.navigate(navigator.getRouters("all").getRoute("login"));
//        String[] columnNames = new String[]{"Item Id", "Item Name", "Supplier Name", "Created At", "Updated At"};
//        CustomTableView.setJoins(Supplier.class, "supplier_id");
//        CustomTableView tableView = new CustomTableView(columnNames, Item.class);

//        navigator.navigate(tableView.getView());

        Scene scene = new Scene(layout.getRoot());
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }
}
