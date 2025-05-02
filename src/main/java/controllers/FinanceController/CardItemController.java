package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import models.Datas.PaymentCard;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class CardItemController implements Initializable {

    @FXML
    private HBox cardItem;
    @FXML
    private Text cardName;
    @FXML
    private Text cardNumber;
    @FXML
    private Text expiryDate;

    private MakePaymentController mainController;
    private PaymentCard card;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPurpleBackground(cardItem);
    }

    public void setupPurpleBackground(HBox anchorPane) {
        Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#a378ff")),
                new Stop(1, Color.web("#7c4dff"))
        };

        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops
        );

        CornerRadii cornerRadii = new CornerRadii(20);

        Background background = new Background(
                new BackgroundFill(gradient, cornerRadii, Insets.EMPTY)
        );

        anchorPane.setBackground(background);

        anchorPane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
    }

    public void setMainController(MakePaymentController controller) {
        mainController = controller;
    }

    public void setData(PaymentCard data) {
        card = data;
        cardName.setText(data.getCardName());
        cardNumber.setText(String.valueOf(data.getCardNumber()).replaceAll("(.{" + 4 + "})", "$1 ").trim());
        expiryDate.setText(data.getExpiryDate());
    }

    public void onPressCard() {
        mainController.onPressCard(card);
    }
}
