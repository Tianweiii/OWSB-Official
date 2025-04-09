module org.start.owsb {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.start.owsb to javafx.fxml;
    exports org.start.owsb;

    opens controllers to javafx.fxml;
    exports controllers;
}