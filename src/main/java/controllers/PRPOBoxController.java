package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

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
    public Text items_text;
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

    // TODO: setup this 2 setData function
    public void setPOData(){ // Params: PurchaseOrder purchaseOrder ?
        // To be continued
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
        label_1_text.setText("PO ID: PO5A1R034");
        label_2_text.setText("Linked PR: PR2478A45");
    }

    public void setPRData(){ // Params: PurchaseRequisition purchaseRequisition ?
        // To be continued
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
        label_1_text.setText("PR ID: PR2478A45");
        label_2_text.setText("Created By: John Doe");
    }

    public void view_all_item(MouseEvent mouseEvent) {
    }

    public void setBoxStatus(String status){
        statusBoxController.setStatus(status);
    }
}
