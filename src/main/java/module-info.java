module org.start.owsb {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires net.sf.jasperreports.core;
    requires java.desktop;


    opens org.start.owsb to javafx.fxml;
    exports org.start.owsb;

    opens controllers to javafx.fxml;
    exports controllers;

    exports models.Datas;
}