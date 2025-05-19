package controllers.salesController;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import models.Datas.DailySalesHistory;
import models.Datas.Transaction;
import service.DailySalesService;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DeleteDailyItemSalesController implements Initializable {

    @FXML private Pane rootPane;

    @FXML private Pane deletePane;
    @FXML private Label deleteLabel;
    @FXML private Button confirmDeleteButton;
    @FXML private Button cancelDeleteButton;

    @FXML private Pane dailyDeletePane;
    @FXML private Label dailyDeleteLabel;
    @FXML private Button confirmDailyDeleteButton;
    @FXML private Button cancelDailyDeleteButton;

    private final DailySalesService service = new DailySalesService();

    private Consumer<Void> onDeletedCallback;

    private Transaction transactionToDelete;
    private DailySalesHistory dailySalesHistoryToDelete;
    private Runnable onDeleteCallback;
    private Runnable onCancelCallback;

    @FXML
    private void onConfirmDeleteClicked() {
        if (onDeleteCallback != null) onDeleteCallback.run();
    }

    @FXML
    private void onCancelClicked() {
        if (onCancelCallback != null) onCancelCallback.run();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonActions();
        setupKeyboardShortcuts();
        setupTooltips();
        applyFocusTraversable();
        playFadeInAnimation();
        confirmDeleteButton.setOnAction(e -> onConfirmDeleteClicked());
        cancelDeleteButton.setOnAction(e -> onCancelClicked());
    }

    private void setupButtonActions() {
        confirmDeleteButton.setOnAction(e -> {
            if (transactionToDelete != null) {
                service.deleteTransaction(transactionToDelete.getTransactionID());
                closeWithReload();
            }
        });

        cancelDeleteButton.setOnAction(e -> closeWithoutReload());
    }

    private void setupKeyboardShortcuts() {
        rootPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeWithoutReload();
            } else if (event.getCode() == KeyCode.ENTER) {
                if (deletePane.isVisible() && !confirmDeleteButton.isDisabled()) {
                    confirmDeleteButton.fire();
                } else if (dailyDeletePane.isVisible() && !confirmDailyDeleteButton.isDisabled()) {
                    confirmDailyDeleteButton.fire();
                }
            }
        });
    }

    private void setupTooltips() {
        Tooltip.install(confirmDeleteButton, new Tooltip("Confirm deletion (Enter)"));
        Tooltip.install(cancelDeleteButton, new Tooltip("Cancel and close (Esc)"));
        Tooltip.install(confirmDailyDeleteButton, new Tooltip("Confirm deletion of all sales (Enter)"));
        Tooltip.install(cancelDailyDeleteButton, new Tooltip("Cancel and close (Esc)"));
    }

    private void applyFocusTraversable() {
        confirmDeleteButton.setFocusTraversable(true);
        cancelDeleteButton.setFocusTraversable(true);
        rootPane.setFocusTraversable(true);
    }

    public void showSingleDelete(Transaction transaction, Consumer<Void> onDeleted) {
        this.transactionToDelete = transaction;
        this.dailySalesHistoryToDelete = null;
        this.onDeletedCallback = onDeleted;

        deleteLabel.setText(String.format(
                "Are you sure you want to delete the sale of \"%s\" (Qty: %d)?",
                transaction.getItemName(), transaction.getSoldQuantity()));
        deletePane.setVisible(true);
        deletePane.setManaged(true);
        dailyDeletePane.setVisible(false);
        dailyDeletePane.setManaged(false);

        playFadeInAnimation();
        rootPane.requestFocus();
    }

    public void showDailyDelete(DailySalesHistory dailySalesHistory, Consumer<Void> onDeleted) {
        this.dailySalesHistoryToDelete = dailySalesHistory;
        this.transactionToDelete = null;
        this.onDeletedCallback = onDeleted;

        dailyDeleteLabel.setText(String.format(
                "Are you sure you want to delete all sales for the day: %s?",
                dailySalesHistory.getCreatedAt().toString()));
        dailyDeletePane.setVisible(true);
        dailyDeletePane.setManaged(true);
        deletePane.setVisible(false);
        deletePane.setManaged(false);

        playFadeInAnimation();
        rootPane.requestFocus();
    }

    private void closeWithReload() {
        playFadeOutAnimation(() -> {
            if (onDeletedCallback != null) {
                onDeletedCallback.accept(null);
            }
            closeDialog();
        });
    }

    private void closeWithoutReload() {
        playFadeOutAnimation(this::closeDialog);
    }

    private void closeDialog() {
        rootPane.getScene().getWindow().hide();
    }

    private void playFadeInAnimation() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(280), rootPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void playFadeOutAnimation(Runnable onFinish) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> onFinish.run());
        fadeOut.play();
    }

    public void setOnDelete(Runnable callback) {
        this.onDeleteCallback = callback;
    }

    public void setOnCancel(Runnable callback) {
        this.onCancelCallback = callback;
    }

    public void setDeleteLabel(String label) {
        deleteLabel.setText(label);
    }
}
