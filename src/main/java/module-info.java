module org.start.owsb {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.sf.jasperreports.core;
    requires java.sql;
    requires javafx.swing;
	requires java.desktop;
    requires java.management;
    requires jdk.unsupported.desktop;
    requires com.fasterxml.jackson.annotation;


    opens org.start.owsb to javafx.fxml;
    exports org.start.owsb;

    opens views to javafx.fxml;
    exports views;

    opens models.DTO to javafx.base;

    opens controllers to javafx.fxml;
    exports controllers;

	opens controllers.salesController to javafx.fxml;
    exports controllers.salesController;

	exports views.salesViews;
	opens views.salesViews to javafx.fxml;

    opens controllers.InventoryController to javafx.fxml;
    exports controllers.InventoryController;

    opens models.Datas to javafx.base;
    exports views.Inventory;
    opens views.Inventory to javafx.fxml;
}