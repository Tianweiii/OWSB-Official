package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.DTO.PaymentDTO;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentItemController implements Initializable {

    @FXML private Text supplierTitle;
    @FXML private VBox itemContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setData(String supplierName, List<PaymentDTO> payments) {
        supplierTitle.setText(supplierName);
        itemContainer.getChildren().clear();

        for (PaymentDTO payment : payments) {
            try {
                HBox itemRow = createItemRow(payment);
                itemContainer.getChildren().add(itemRow);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private HBox createItemRow(PaymentDTO payment) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER);
        row.setPrefWidth(370.0);
        row.setSpacing(20.0);

        VBox imageContainer = new VBox();
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        imageContainer.setPrefHeight(70.0);
        imageContainer.setPrefWidth(70.0);
        imageContainer.setMinHeight(70);
        imageContainer.setMinWidth(70);
        imageContainer.setStyle("-fx-background-color: #e7e4f5; -fx-background-radius: 8;");

        ImageView imageView = new ImageView(new Image(getClass().getResource("/assets/images/bag.png").toExternalForm()));
        imageView.setFitHeight(32.0);
        imageView.setFitWidth(32.0);
        imageView.setPreserveRatio(true);
        imageView.setPickOnBounds(true);

        imageContainer.getChildren().add(imageView);

        VBox detailsContainer = new VBox();
        detailsContainer.setPrefHeight(52.0);
        detailsContainer.setPrefWidth(134.0);
        HBox.setHgrow(detailsContainer, javafx.scene.layout.Priority.ALWAYS);

        Text itemName = new Text(payment.getItemName());
        itemName.setFont(new javafx.scene.text.Font("Baloo Da 2 Medium", 16.0));

        Text itemID = new Text(payment.getItem());
        itemID.setFont(new javafx.scene.text.Font("Baloo Da 2 Medium", 12.0));
        itemID.setFill(javafx.scene.paint.Color.valueOf("#a8a8a8"));

        detailsContainer.getChildren().addAll(itemName, itemID);

        VBox quantityContainer = new VBox();
        quantityContainer.setAlignment(javafx.geometry.Pos.CENTER);
        quantityContainer.setPrefHeight(44.0);
        quantityContainer.setPrefWidth(57.0);

        Text quantity = new Text(String.valueOf(payment.getQuantity()) + "x");
        quantity.setFont(new javafx.scene.text.Font("Baloo Da 2 Medium", 14.0));
        quantity.setFill(javafx.scene.paint.Color.valueOf("#a8a8a8"));
        quantity.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);

        quantityContainer.getChildren().add(quantity);

        Text price = new Text("RM" + payment.getAmount());
        price.setFont(new javafx.scene.text.Font("Baloo Da 2 Medium", 16.0));

        row.getChildren().addAll(imageContainer, detailsContainer, quantityContainer, price);

        return row;
    }
}
