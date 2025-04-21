package controllers.InventoryController;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import models.Datas.Item;
import models.Utils.QueryBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import static models.Datas.Item.stringifyDateTime;

public class UpdateInventoryController {

    @FXML
    private Button btnClose;

    @FXML
    private Button btnDecrease;

    @FXML
    private Button btnIncrease;

    @FXML
    private TextField txtAlertLevel;

    @FXML
    private Label txtItemName;

    @FXML
    private Label txtNum;

    @FXML
    private StackPane updateItemPane;

    @FXML
    private Button btnUpdateInventory;

    private int stockNum;
    private Item item;
    private Consumer<Item> refreshCallback;

    @FXML
    public void initialize() {
        btnIncrease.setOnMouseClicked(e -> incrementStock());
        btnDecrease.setOnMouseClicked(e -> decrementStock());
        btnClose.setOnMouseClicked(e -> closeDialog());
        btnUpdateInventory.setOnMouseClicked(e -> {
            try {
                updateInventoryItem();
            } catch (IOException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void setItemData(int itemID, Item item) {
        this.item = item;
        this.stockNum = item.getQuantity();
        txtItemName.setText(item.getItemName());
        txtNum.setText(String.valueOf(stockNum));
        txtAlertLevel.setText(String.valueOf(item.getAlertSetting()));
    }

    public void incrementStock() {
        stockNum++;
        txtNum.setText(String.valueOf(stockNum));
    }

    public void decrementStock() {
        stockNum--;
        txtNum.setText(String.valueOf(stockNum));
    }

    public void closeDialog() {
        AnchorPane dimmedBackground = (AnchorPane) updateItemPane.getParent();
        if (dimmedBackground != null && dimmedBackground.getParent() instanceof Pane) {
            ((Pane) dimmedBackground.getParent()).getChildren().remove(dimmedBackground);
        }
    }

    public void setRefreshCallback(Consumer<Item> callback) {
        this.refreshCallback = callback;
    }

    public void updateInventoryItem() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        int newAlertLevel = Integer.parseInt(txtAlertLevel.getText());

        QueryBuilder<Item> qb = new QueryBuilder(Item.class);
        ArrayList<HashMap<String, String>> itemResult = qb.select().from("db/Item").where("itemID", "=", String.valueOf(item.getItemID())).get();

        HashMap<String, String> fullItemMap = itemResult.get(0);

        double unitPrice = Double.parseDouble(fullItemMap.get("unitPrice"));
        int supplierID = Integer.parseInt(fullItemMap.get("supplierID"));

        String[] values = new String[]{
                item.getItemName(),
                stringifyDateTime(item.getCreatedAt()),
                stringifyDateTime(LocalDateTime.now()),
                String.valueOf(newAlertLevel),
                String.valueOf(stockNum),
                String.format("%.2f", unitPrice),
                String.valueOf(supplierID),
        };

/* *************Use this temp firstt ****************** */
//        boolean updateSuccess = qb.update(String.valueOf(item.getItemID()), values);
        qb.update(String.valueOf(item.getItemID()), values);

//        if(updateSuccess) {
//            showNotification("Success", "Inventory updated successfully.", "success");
//            if (refreshCallback != null) {
//                refreshCallback.accept(item);
//            }
//        } else {
//            showNotification("Failure", "Failed to update inventory. Please try again.", "error");
//        }

        if (refreshCallback != null) {
            refreshCallback.accept(item);
        }

        closeDialog();

    }

    private void showNotification(String title, String message, String type) {
        Alert alert = new Alert(type.equals("success") ? Alert.AlertType.CONFIRMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
