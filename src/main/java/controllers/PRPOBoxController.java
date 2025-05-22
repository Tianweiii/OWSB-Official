package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import models.DTO.PODataDTO;
import models.DTO.POItemDTO;
import models.DTO.PRDataDTO;
import models.DTO.PRItemDTO;
import models.Datas.PurchaseOrder;
import models.Datas.PurchaseRequisition;
import models.Utils.AccessPermission;
import models.Utils.Navigator;
import models.Utils.SessionManager;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class PRPOBoxController implements Initializable {
    @FXML
    public ImageView label_1_image;
    @FXML
    public Text label_1_text;
    @FXML
    public ImageView label_2_image;
    @FXML
    public Text label_2_text;
    @FXML
    public Label items_text;
    @FXML
    public Text total_items_text;
    @FXML
    public Text total_quantity_text;
    @FXML
    public Text total_price_text;
    @FXML
    public Text pr_creation_date_text;
    @FXML
    public Text required_by_text;
    @FXML
    public HBox statusBox;
    @FXML
    public AnchorPane statusbox_container;
    @FXML
    private StatusBoxController statusBoxController;

    public PODataDTO poData;
    public PRDataDTO prData;
    public EditPRPOController editPRPOcontroller;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/StatusBox.fxml"));
            Node statusBoxNode = loader.load();

            statusBoxController = loader.getController();
            statusbox_container.getChildren().add(statusBoxNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPOData(PODataDTO poData){
        this.poData = poData;

        // Setup label's image
        Image cargoIconImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Assets/icon/cargo_icon.png")),
                20, 20,
                false,  // preserveRatio
                true    // smooth scaling
        );
        label_1_image.setImage(cargoIconImage);
        label_2_image.setImage(cargoIconImage);

        // Setup label's text
        label_1_text.setText("PO ID: " + poData.getPoID());
        label_2_text.setText("Linked PR: " + poData.getPrID());

        // Build the item string. Eg; Items: Tomato x50, Rice x100, Oil x20, ...
        StringBuilder itemsString = new StringBuilder();
        for(POItemDTO poItem : poData.getPoItemList()){
            itemsString.append(poItem.getItemName()).append(" x").append(poItem.getItemQuantity()).append(", ");
        }
        items_text.setText("Items: " + itemsString);
        total_items_text.setText("Total Items: "+poData.getPoItemList().size());
        total_quantity_text.setText("Total Quantity: " + poData.getTotalQuantity());
        total_price_text.setText("Total Price: RM" + poData.getPayableAmount());
        pr_creation_date_text.setText("PR Creation Date: " + poData.getCreatedDate());
        required_by_text.setText("Required By: " + poData.getReceivedByDate());

        // Update status
        statusBoxController.setStatus(poData.getStatus());
    }

    public void setPRData(PRDataDTO prData){
        this.prData = prData;

        // Hide price text (Only PO shows the Total price text: Total Price: RM 3,200.00)
        total_price_text.setVisible(false);

        // Setup label's image
        Image cargoIconImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Assets/icon/cargo_icon.png")),
                20, 20,
                false,  // preserveRatio
                true    // smooth scaling
        );
        Image userIconImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Assets/icon/user_icon_blue.png")),
                20, 20,
                false,  // preserveRatio
                true    // smooth scaling
        );
        label_1_image.setImage(cargoIconImage);
        label_2_image.setImage(userIconImage);

        // Setup label's text
        label_1_text.setText("PR ID: " + prData.getPrID());
        label_2_text.setText("Created By: " + prData.getUserName());

        // Build the item string. Eg; Items: Tomato x50, Rice x100, Oil x20, ...
        StringBuilder itemsString = new StringBuilder();
        for(PRItemDTO prItem : prData.getPrItemList()){
            itemsString.append(prItem.getItemName()).append(" x").append(prItem.getItemQuantity()).append(", ");
        }
        items_text.setText("Items: " + itemsString);
        total_items_text.setText("Total Items: " + prData.getPrItemList().size());
        total_quantity_text.setText("Total Quantity: " + prData.getTotalQuantity());
        pr_creation_date_text.setText("PR Creation Date: " + prData.getCreatedDate());
        required_by_text.setText("Required By: " + prData.getReceivedByDate());

        // Update Status
        statusBoxController.setStatus(prData.getStatus());
    }

    public void ViewPRPODetails(MouseEvent mouseEvent) throws IOException {
        String userRole = SessionManager.getInstance().getUserData().get("roleID");

        if(prData == null & poData == null){
            return;
        }

        // Loading the fxml content
        Navigator navigator = Navigator.getInstance();
        FXMLLoader editPRPOLoader = new FXMLLoader(getClass().getResource("/PRPO/EditPRPO.fxml"));
        navigator.navigate(editPRPOLoader.load());
        editPRPOcontroller = editPRPOLoader.getController();

        // Conditional rendering
        switch(userRole){
            case "1" -> {
                AccessPermission.AccessType adminAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("1").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("1").get(poData.getStatus());

                if(prData != null){
                    editPRPOcontroller.loadPageContent(adminAccess, prData);
                } else {
                    editPRPOcontroller.loadPageContent(adminAccess, poData);
                }
            }

            case "2" -> {
                AccessPermission.AccessType salesAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("2").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("2").get(poData.getStatus());
                if(prData != null){
                    editPRPOcontroller.loadPageContent(salesAccess, prData);
                } else {
                    editPRPOcontroller.loadPageContent(salesAccess, poData);
                }
            }

            case "3" -> {
                AccessPermission.AccessType purchaseAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("3").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("3").get(poData.getStatus());

                if(prData != null){
                    editPRPOcontroller.loadPageContent(purchaseAccess, prData);
                } else {
                    editPRPOcontroller.loadPageContent(purchaseAccess, poData);
                }
            }

            case "4" -> {
                AccessPermission.AccessType inventoryAccess = AccessPermission.POPermissionMap.get("4").get(poData.getStatus());
                if(poData != null){
                    editPRPOcontroller.loadPageContent(inventoryAccess, poData);
                }
            }

            case "5" -> {
                AccessPermission.AccessType financeAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("5").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("5").get(poData.getStatus());

                if(prData != null){
                    editPRPOcontroller.loadPageContent(financeAccess, prData);
                } else {
                    editPRPOcontroller.loadPageContent(financeAccess, poData);
                }
            }
        }

    }

    public void test() throws IOException {
        Navigator navigator = Navigator.getInstance();
        FXMLLoader test = new FXMLLoader(getClass().getResource("/PRPO/EditPRPO.fxml"));
        navigator.navigate(test.load());

    }
}
