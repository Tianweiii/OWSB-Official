module org.start.owsb {
    requires javafx.controls;
    requires javafx.fxml;
	requires java.desktop;


	opens org.start.owsb to javafx.fxml;
    exports org.start.owsb;

    opens controllers to javafx.fxml;
    exports controllers;

	opens controllers.salesController to javafx.fxml;
    exports controllers.salesController;

	exports views.salesViews;
	opens views.salesViews to javafx.fxml;

}