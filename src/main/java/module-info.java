module org.start.owsb {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.sf.jasperreports.core;
    requires java.sql;
	requires java.desktop;
    requires java.management;

    requires java.mail;
    requires activation;

	opens org.start.owsb to javafx.fxml;
    exports org.start.owsb;

    opens views to javafx.fxml;
    exports views;

    opens models.DTO to javafx.base;
    exports models.DTO;

    opens controllers to javafx.fxml;
    exports controllers;

    opens controllers.FinanceController to javafx.fxml;
    exports controllers.FinanceController;

    opens models.Datas to java.base;
    exports models.Datas;

	opens controllers.salesController to javafx.fxml;
    exports controllers.salesController;

	exports views.salesViews;
	opens views.salesViews to javafx.fxml;

    opens controllers.InventoryController to javafx.fxml;
    exports controllers.InventoryController;

    exports views.Inventory;
    opens views.Inventory to javafx.fxml;
}