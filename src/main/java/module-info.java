module org.start.owsb {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires net.sf.jasperreports.core;
    requires java.desktop;
    requires java.management;
    requires java.mail;
    requires activation;

    opens org.start.owsb to javafx.fxml;
    exports org.start.owsb;
    exports controllers;
    opens controllers to javafx.fxml;

    opens controllers.FinanceController to javafx.fxml;
    exports controllers.FinanceController;

    exports models.Datas;
}