package controllers.salesController;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CompleteSalesReportController implements Initializable {

    @FXML private Pane rootPane;
    @FXML private VBox confirmPane;
    @FXML private Label confirmLabel, noteLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private Runnable onConfirmCallback;
    private Runnable onCancelCallback;
    private LocalDate selectedDate;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupKeyboardShortcuts();
        setupTooltips();
        playFadeInAnimation();
    }

    private void setupKeyboardShortcuts() {
        rootPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                onCancelClicked();
            } else if (event.getCode() == KeyCode.ENTER) {
                onConfirmClicked();
            }
        });
    }

    private void setupTooltips() {
        Tooltip.install(confirmButton, new Tooltip("Generate report and create inventory update request (Enter)"));
        Tooltip.install(cancelButton, new Tooltip("Cancel and close (Esc)"));
    }

    @FXML
    public void onConfirmClicked() {
        if (onConfirmCallback != null) {
            onConfirmCallback.run();
        }
    }

    @FXML
    public void onCancelClicked() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        if (date != null) {
            String formattedDate = date.format(formatter);
            confirmLabel.setText("This will generate a sales report for " + formattedDate + 
                " and notify inventory management. Are you sure you want to proceed?");
        }
    }

    public void setOnConfirm(Runnable callback) {
        this.onConfirmCallback = callback;
    }

    public void setOnCancel(Runnable callback) {
        this.onCancelCallback = callback;
    }

    private void playFadeInAnimation() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(280), rootPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void playFadeOutAnimation(Runnable onFinish) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> onFinish.run());
        fadeOut.play();
    }
} 