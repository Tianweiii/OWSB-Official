package controllers.FinanceController;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;
import models.Datas.PaymentCard;
import models.Utils.QueryBuilder;

public class MakePaymentController implements Initializable, IdkWhatToNameThis {

    @FXML
    private VBox cardItemContainer;
    @FXML
    private HBox newCardContainer;
    @FXML
    private VBox orderSummaryContainer;
    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField expirationDateField;
    @FXML
    private TextField cvvField;

    private String[] testArr = new String[]{"1", "2", "3", "4", "5", "6", "7"};
    private FinanceMainController mainController;
    private ArrayList<PaymentCard> cardDatas;
    private boolean newCard = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            QueryBuilder<PaymentCard> qb = new QueryBuilder<>(PaymentCard.class);
            cardDatas = qb.select().from("db/PaymentCard").getAsObjects();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        setupGreyBackground(newCardContainer);
        for (PaymentCard item : cardDatas) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/CardItem.fxml"));
                Parent card = loader.load();

                CardItemController controller = loader.getController();
                controller.setMainController(this);
                controller.setData(item);

                cardItemContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String i : testArr) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/PaymentItem.fxml"));
                Parent card = loader.load();

                PaymentItemController controller = loader.getController();
//                controller.setData(item); // Inject data into component

                orderSummaryContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressBack() {
        mainController.goBack();
    }

    private void setupGreyBackground(HBox hbox) {
        Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#e0e0e0")),
                new Stop(1, Color.web("#c0c0c0"))
        };

        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops
        );

        CornerRadii cornerRadii = new CornerRadii(20);

        Background background = new Background(
                new BackgroundFill(gradient, cornerRadii, Insets.EMPTY)
        );

        hbox.setBackground(background);

        hbox.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
    }

    public void onPressCard(PaymentCard card) {
        cardNumberField.setText(String.valueOf(card.getCardNumber()));
        nameField.setText(card.getCardName());
        expirationDateField.setText(card.getExpiryDate());
        cvvField.setText(String.valueOf(card.getCvv()));
        newCard = false;
    }

    public void onPressNewCard() {
        cardNumberField.clear();
        nameField.clear();
        expirationDateField.clear();
        cvvField.clear();
        newCard = true;
    }

    public void onPressPay() {
        // show loading screen
        mainController.renderLoader();
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> {
            mainController.removeLoader();
        });

        delay.play();

        // create new payment row
        // nav to payment success screen
        // send receipt to email
    }
}
