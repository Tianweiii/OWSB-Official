package controllers.FinanceController;

import controllers.NotificationController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import javafx.scene.text.Text;
import javafx.util.Duration;
import models.DTO.PaymentDTO;
import models.Datas.Payment;
import models.Datas.PaymentCard;
import models.Datas.PurchaseOrder;
import models.Utils.Helper;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;
import views.NotificationView;

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
    @FXML
    private CheckBox saveCardField;

    @FXML
    private Text subtotalField;
    @FXML
    private Text shippingField;
    @FXML
    private Text totalField;

    Navigator navigator = Navigator.getInstance();

    // PO data
    private PurchaseOrder currentPO = SessionManager.getCurrentPaymentPO();
    private Map<String, List<PaymentDTO>> paymentItems;

    private FinanceMainController mainController;
    private ArrayList<PaymentCard> cardDatas;
    private boolean newCard = true;
    private double subtotal = 0;
    private double shipping = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // show toast if no currentPO
        // TODO: clear session manager payment after making payment
        try {
            QueryBuilder<PaymentCard> qb = new QueryBuilder<>(PaymentCard.class);
            cardDatas = qb.select().from("db/PaymentCard").getAsObjects();

            paymentItems = currentPO.getPurchaseItemList();
            System.out.println(paymentItems);

        } catch (ReflectiveOperationException | IOException e) {
            throw new RuntimeException(e);
        }

        setupGreyBackground(newCardContainer);

        // rendering cards
        for (PaymentCard item : cardDatas) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/CardItem.fxml"));
                Parent card = loader.load();

                CardItemController controller = loader.getController();
                controller.setMainController(this);
                controller.setData(item);

                cardItemContainer.getChildren().add(card);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // rendering payment items
        for (var entry : paymentItems.entrySet()) {
            System.out.println(paymentItems);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/PaymentItem.fxml"));
                Parent card = loader.load();
                System.out.println(loader);

                PaymentItemController controller = loader.getController();
                controller.setData(entry.getKey(), entry.getValue());

                subtotal += entry.getValue().get(0).getAmount();

                orderSummaryContainer.getChildren().add(card);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        subtotalField.setText("RM" + String.valueOf(subtotal));
        totalField.setText("RM" + String.valueOf(subtotal + shipping));
    }

    @Override
    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressBack() {
//        mainController.goBack();
        navigator.goBack();
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
        saveCardField.setDisable(true);
        saveCardField.setSelected(false);
        cardNumberField.setText(String.valueOf(card.getCardNumber()));
        nameField.setText(card.getCardName());
        expirationDateField.setText(card.getExpiryDate());
        cvvField.setText(String.valueOf(card.getCvv()));
        newCard = false;
    }

    public void onPressNewCard() {
        saveCardField.setDisable(false);
        saveCardField.setSelected(false);
        cardNumberField.clear();
        nameField.clear();
        expirationDateField.clear();
        cvvField.clear();
        newCard = true;
    }

    private boolean validateCardFields() throws IOException {
        String cardNumber = cardNumberField.getText().trim();
        String name = nameField.getText().trim();
        String expirationDate = expirationDateField.getText().trim();
        String cvv = cvvField.getText().trim();

        if (!cardNumber.matches("\\d{16}")) {
            NotificationView notificationView = new NotificationView("Card number must have exactly 16 digits!", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
            notificationView.show();
            return false;
        }

        if (name.isEmpty()) {
            NotificationView notificationView = new NotificationView("Name cannot be empty!", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
            notificationView.show();
            return false;
        }

        if (!cvv.matches("\\d{3}")) {
            NotificationView notificationView = new NotificationView("CVV must be exactly 3 digits.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
            notificationView.show();
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate date = LocalDate.parse(expirationDate, formatter);
            if (date.isBefore(LocalDate.now())) {
                NotificationView notificationView = new NotificationView("Expiration date must be in the future.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notificationView.show();
                return false;
            }
        } catch (DateTimeParseException e) {
            NotificationView notificationView = new NotificationView("Expiration date must be in the format dd-MM-yyyy.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
            notificationView.show();
            return false;
        }

        return true;
    }


    public void onPressPay() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
//        mainController.renderLoader();
//        navigator.renderLoader();

        if (validateCardFields()) {
            new Thread(() -> {
                boolean res = false;
                long startTime = System.currentTimeMillis();

                try {
                    QueryBuilder<Payment> qb = new QueryBuilder<>(Payment.class);
                    res = qb.target("db/Payment")
                            .values(new String[]{
                                    currentPO.getpoID(),
                                    currentPO.getUserID(),
                                    String.valueOf(subtotal + shipping),
                                    "Credit Card",
                                    Helper.getTodayDate(),
                                    Payment.generatePaymentReference(Payment.getPaymentLatestRowCount())
                            }).create("PY");

                    // update PO status to paid
                    QueryBuilder<PurchaseOrder> qb3 = new QueryBuilder<>(PurchaseOrder.class);
                    qb3.update(currentPO.getpoID(), new String[]{
                            currentPO.getPrRequisitionID(),
                            currentPO.getUserID(),
                            currentPO.getTitle(),
                            String.valueOf(currentPO.getPayableAmount()),
                            "Paid"
                    });

                    // if save card, save card
                    if (saveCardField.isSelected()) {
                        QueryBuilder<PaymentCard> qb2 = new QueryBuilder<>(PaymentCard.class);
                        qb2.target("db/PaymentCard")
                                .values(new String[]{
                                        cardNumberField.getText(),
                                        nameField.getText(),
                                        expirationDateField.getText(),
                                        cvvField.getText(),
                                        "9999"
                                }).create("PC");
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                long elapsed = System.currentTimeMillis() - startTime;
                // load for at least 2 seconds to mimic real ting lol
                long remainingTime = Math.max(0, 1000 - elapsed);

                boolean finalRes = res;
                Platform.runLater(() -> {
                    PauseTransition pause = new PauseTransition(Duration.millis(remainingTime));
                    pause.setOnFinished(ev -> {
                        if (finalRes) {
                            navigator.navigate(navigator.getRouters("finance").getRoute("paymentSuccess"));
//                            mainController.goToPaymentSuccess();
                        }
//                        mainController.removeLoader();
//                        navigator.removeLoader();
                    });
                    pause.play();
                });

            }).start();
        } else {
//            mainController.removeLoader();
//            navigator.removeLoader();
        }
    }

}
