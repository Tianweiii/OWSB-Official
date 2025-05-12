package controllers.FinanceController;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

// where the sidebar tings will be
public class FinanceMainController implements Initializable {

    @FXML
    private AnchorPane mainStage;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private Pane transitionPanel1;
    @FXML
    private Pane transitionPanel2;
    @FXML
    private Pane transitionPanel3;
    @FXML
    private HBox homeNavButton;
    @FXML
    private HBox prNavButton;
    @FXML
    private HBox poNavButton;
    @FXML
    private HBox financeNavButton;
    @FXML
    private HBox inventoryNavButton;
    @FXML
    private VBox loaderPane;

    private List<HBox> menuItems;

    // for nav shit
    private String prevFile = "FinanceHome.fxml";
    private final ArrayList<String> stackList = new ArrayList<>();

    // caching
//    private Map<String, Object> controllerCache = new HashMap<>();

    // for anim shit
    private double appHeight;
    private final Interpolator inOut = Interpolator.SPLINE(0.25, 0.25, 0.75, 0.75);

//    Map<String, Supplier<Object>> controllerFactory = Map.of(
//            "FinancePayments.fxml", () -> {
//                FinancePaymentsController c = new FinancePaymentsController();
//                c.setMainController(this);
//                return c;
//            },
//            "MakePayment.fxml", () -> {
//                MakePaymentController c = new MakePaymentController();
//                c.setMainController(this);
//                return c;
//            }
//    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setAnchor("/FinanceFXML/FinanceHome.fxml");
            stackList.add(prevFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        javafx.application.Platform.runLater(() -> {
            appHeight = mainStage.getScene().getWindow().getHeight();
            menuItems = List.of(homeNavButton, prNavButton, poNavButton, financeNavButton, inventoryNavButton);
            setupSidebarSelection(homeNavButton);
        });
    }

    public void setAnchor(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Parent root = loader.load();
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        contentArea.getChildren().setAll(root);
    }

    private void loadPage(String fxmlFile, boolean back) {
        runTransitionGrow(() -> {
            try {
                // this shit so ass lmao
                if (back) {
                    stackList.remove(stackList.size() - 1);
                } else {
                    String last = stackList.get(stackList.size() - 1);
                    if (!Objects.equals(last, prevFile)) stackList.add(prevFile);
                }

                prevFile = fxmlFile;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinanceFXML/" + fxmlFile));

                Parent page = loader.load();
                AnchorPane.setTopAnchor(page, 0.0);
                AnchorPane.setBottomAnchor(page, 0.0);
                AnchorPane.setLeftAnchor(page, 0.0);
                AnchorPane.setRightAnchor(page, 0.0);
                contentArea.getChildren().setAll(page);

//                setAnchor("/FinanceFXML/" + fxmlFile);

                switch (fxmlFile) {
                    case "FinancePayments.fxml" -> {
                        FinancePaymentsController paymentsController = loader.getController();
                        paymentsController.setMainController(this);
                    }
                    case "MakePayment.fxml" -> {
                        MakePaymentController idkController = loader.getController();
                        idkController.setMainController(this);
                    }
                    case "FinancePR.fxml" -> {
                        FinancePRController financePRController = loader.getController();
                        financePRController.setMainController(this);
                    }
                    case "PRDetails.fxml" -> {
                        PRDetailsController prDetailsController = loader.getController();
                        prDetailsController.setMainController(this);
                    }
                    case "PaymentSuccess.fxml" -> {
                        PaymentSuccessController paymentSuccessController = loader.getController();
                        paymentSuccessController.setMainController(this);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            PauseTransition pause = getPauseTransition();
            pause.play();
        });
    }

    private PauseTransition getPauseTransition() {
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(event -> runTransitionShrink(() -> {
            AnchorPane.setTopAnchor(transitionPanel1, null);
            AnchorPane.setTopAnchor(transitionPanel2, null);
            AnchorPane.setTopAnchor(transitionPanel3, null);

            AnchorPane.setBottomAnchor(transitionPanel1, (double) 0);
            AnchorPane.setBottomAnchor(transitionPanel2, (double) 0);
            AnchorPane.setBottomAnchor(transitionPanel3, (double) 0);
        }));
        return pause;
    }

    public void renderLoader() {
        Group spinnerGroup = new Group();

        Circle outerRing = new Circle(25, Color.TRANSPARENT);
        outerRing.setStroke(Color.DODGERBLUE);
        outerRing.setStrokeWidth(3);
        // Create a gap in the circle
        outerRing.getStrokeDashArray().addAll(90.0, 10.0);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(70, 150, 255, 0.7));
        glow.setRadius(10);
        outerRing.setEffect(glow);

        spinnerGroup.getChildren().add(outerRing);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1.2), outerRing);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.EASE_BOTH);
        rotate.play();

        loaderPane.getChildren().add(spinnerGroup);

        loaderPane.setOpacity(1);
        loaderPane.setMouseTransparent(false);
    }

    public void removeLoader() {
        loaderPane.setOpacity(0);
        loaderPane.setMouseTransparent(true);
        loaderPane.getChildren().clear();
    }

    public void onPressHomeButton() {
        loadPage("FinanceHome.fxml", false);
        setupSidebarSelection(homeNavButton);
    }

    public void onPressPRButton() {
        loadPage("FinancePR.fxml", false);
        setupSidebarSelection(prNavButton);
    }

    public void onPressPOButton() {
        loadPage("FinancePO.fxml", false);
        setupSidebarSelection(poNavButton);
    }

    public void onPressFinanceReportButton() {
        loadPage("FinanceReport.fxml", false);
        setupSidebarSelection(financeNavButton);
    }

    public void onPressInventoryButton() {
        loadPage("FinancePayments.fxml", false);
        setupSidebarSelection(inventoryNavButton);
    }

    public void onPressPayment() {
        loadPage("MakePayment.fxml", false);
    }

    public void onPressPRDetails() {
        loadPage("PRDetails.fxml", false);
    }

    public void goToPaymentSuccess() {
        loadPage("PaymentSuccess.fxml", false);
    }

    public void goBack() {
        int index = stackList.size() - 1;
        String path = index < 0 ? "FinanceHome.fxml" : stackList.get(index);
        loadPage(path, true);
    }

    public void goHome() {
        loadPage("FinanceHome.fxml", false);
        setupSidebarSelection(homeNavButton);
    }

    private void setupSidebarSelection(HBox target) {
        for (HBox item : menuItems) {
            item.setStyle("-fx-background-color: transparent;");
        }

        target.setStyle("-fx-background-color: #CFDBF0;"); // your active color
    }

    private void runTransitionGrow(Runnable onFinished) {
        Timeline growAnims = new Timeline(
            new KeyFrame(Duration.seconds(0),
                new KeyValue(transitionPanel1.prefHeightProperty(), 0)
            ),
            new KeyFrame(Duration.seconds(0.25),
                new KeyValue(transitionPanel1.prefHeightProperty(), appHeight, inOut)
            ),
            new KeyFrame(Duration.seconds(0.2),
                new KeyValue(transitionPanel2.prefHeightProperty(), 0)
            ),
            new KeyFrame(Duration.seconds(0.45),
                new KeyValue(transitionPanel2.prefHeightProperty(), appHeight, inOut)
            ),
            new KeyFrame(Duration.seconds(0.4),
                new KeyValue(transitionPanel3.prefHeightProperty(), 0)
            ),
            new KeyFrame(Duration.seconds(0.65),
                new KeyValue(transitionPanel3.prefHeightProperty(), appHeight, inOut)
            )
        );

        growAnims.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });

        growAnims.play();
    }

    private void runTransitionShrink(Runnable onFinished) {
        Timeline shrinkAnims = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(transitionPanel3.prefHeightProperty(), appHeight, inOut)
                ),
                new KeyFrame(Duration.seconds(0.25),
                        new KeyValue(transitionPanel3.prefHeightProperty(), 0)
                ),
                new KeyFrame(Duration.seconds(0.2),
                        new KeyValue(transitionPanel2.prefHeightProperty(), appHeight, inOut)
                ),
                new KeyFrame(Duration.seconds(0.45),
                        new KeyValue(transitionPanel2.prefHeightProperty(), 0)
                ),
                new KeyFrame(Duration.seconds(0.4),
                        new KeyValue(transitionPanel1.prefHeightProperty(), appHeight, inOut)
                ),
                new KeyFrame(Duration.seconds(0.65),
                        new KeyValue(transitionPanel1.prefHeightProperty(), 0)
                )
        );

        // this is also damn skibidi
        AnchorPane.setBottomAnchor(transitionPanel1, null);
        AnchorPane.setBottomAnchor(transitionPanel2, null);
        AnchorPane.setBottomAnchor(transitionPanel3, null);

        AnchorPane.setTopAnchor(transitionPanel1, (double) 0);
        AnchorPane.setTopAnchor(transitionPanel2, (double) 0);
        AnchorPane.setTopAnchor(transitionPanel3, (double) 0);

        shrinkAnims.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });

        shrinkAnims.play();
    }
}
