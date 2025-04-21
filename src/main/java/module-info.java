module org.start.owsb {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.sf.jasperreports.core;
    requires java.sql;
    requires javafx.swing;


    opens org.start.owsb to javafx.fxml;
    exports org.start.owsb;

    opens controllers to javafx.fxml;
    exports controllers;

    opens controllers.InventoryController to javafx.fxml;
    exports controllers.InventoryController;

    opens models.Datas to javafx.base;
}