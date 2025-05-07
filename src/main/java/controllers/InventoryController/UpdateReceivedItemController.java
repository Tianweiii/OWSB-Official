package controllers.InventoryController;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import models.Datas.InventoryUpdateLog;
import models.Datas.Item;
import models.Datas.PurchaseItem;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

import static models.Datas.Item.stringifyDateTime;

public class UpdateReceivedItemController {

    @FXML
    private Button btnCancelUpdate;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnDecrease;

    @FXML
    private Button btnIncrease;

    @FXML
    private Button btnUpdateInventory;

    @FXML
    private ComboBox<?> dropDownItem;

    @FXML
    private TextField txtNum;

    @FXML
    private StackPane updateItemPane;

    private int stockNum;
    private PurchaseItem purchaseItem;

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();
        HashMap<String, String> userData = session.getUserData();
        int currentUserID = Integer.parseInt(userData.get("user_id"));

        btnIncrease.setOnMouseClicked(e -> incrementStock());
        btnDecrease.setOnMouseClicked(e -> decrementStock());
        btnClose.setOnMouseClicked(e -> closeDialog());
        btnCancelUpdate.setOnMouseClicked(e -> closeDialog());
        btnUpdateInventory.setOnMouseClicked(e -> {
            try {
                updateInventoryItem(currentUserID);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void setPurchaseItemData(String itemID, PurchaseItem purchaseItem){

    }

    public void incrementStock() {
        stockNum++;
        txtNum.setText(String.valueOf(stockNum));
    }

    public void decrementStock() {
        if (Integer.parseInt(txtNum.getText()) > 0) {
            stockNum--;
            txtNum.setText(String.valueOf(stockNum));
        }
    }

    public void closeDialog() {
        AnchorPane dimmedBackground = (AnchorPane) updateItemPane.getParent();
        if (dimmedBackground != null && dimmedBackground.getParent() instanceof Pane) {
            Pane parentPane = (Pane) dimmedBackground.getParent();
            parentPane.getChildren().remove(dimmedBackground);

//            REMEMBER TO CHANGE PANE
            Node stockPane = parentPane.lookup("#stockManagementPane");
            if (stockPane != null) {
                stockPane.toFront();
            }
        }
    }

    private void updateInventoryItem(int currentUserID) {

//        stockNum = Integer.parseInt(txtNum.getText());
//
//        QueryBuilder<Item> qb = new QueryBuilder(Item.class);
//        ArrayList<HashMap<String, String>> itemResult = qb.select().from("db/Item").where("itemID", "=", String.valueOf(item.getItemID())).get();
//
//        HashMap<String, String> fullItemMap = itemResult.get(0);
//
//        double unitPrice = Double.parseDouble(fullItemMap.get("unitPrice"));
//        int supplierID = Integer.parseInt(fullItemMap.get("supplierID"));
//
//        InventoryUpdateLog.logItemUpdate(item.getItemID(), item.getQuantity(), stockNum, currentUserID, "Update Inventory Item", true);
//
//        String[] values = new String[]{
//                item.getItemName(),
//                stringifyDateTime(item.getCreatedAt()),
//                stringifyDateTime(LocalDateTime.now()),
//                String.valueOf(newAlertLevel),
//                String.valueOf(stockNum),
//                String.format("%.2f", unitPrice),
//                String.valueOf(supplierID),
//        };
//
//        boolean updateSuccess = qb.update(String.valueOf(item.getItemID()), values);
//        closeDialog();
//        if (refreshCallback != null) {
//            refreshCallback.accept(item, updateSuccess);
//        }
    }



}

