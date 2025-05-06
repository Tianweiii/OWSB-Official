package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PRPOController implements Initializable {
    @FXML
    private Text pending_pr_text;
    @FXML
    private Button request_tab;
    @FXML
    private Button order_tab;
    @FXML
    private AnchorPane pr_pane;
    @FXML
    private VBox pr_list_container;
    @FXML
    private AnchorPane po_pane;
    @FXML
    private VBox po_list_container;
    @FXML
    private Button create_new_pr_button;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get user role info
        SessionManager session = SessionManager.getInstance();
        String user_role = session.getUserData().get("role_id");

        // Hide the PR/PO Tab based on role:
            // Admin & Sales & Purchase = show both PR & PO Tab
            // Inventory & Finance = show PO Tab ONLY
        if(user_role.equals("4") || user_role.equals("5")){
            // Hiding the PR tab and PR Pane
            request_tab.setVisible(false);
            request_tab.setManaged(false); //  Tell the layout not to reserve space for this button
            pr_pane.setVisible(false);

            // load data
            loadPOData();

            // initialize the PR/PO list display
            switchTab("order");
        } else {
            loadPRData();
            loadPOData();
            switchTab("request");
        }

        // Hide the Create PR Button if user is not sales manager (Only SM can create PR)
        if(!user_role.equals("2")){
            create_new_pr_button.setVisible(false);
        }
    }

    // TODO: Currently the loadPRData and loadPOData just create empty dummy PR PO box, will replace with real data in the future
    public void loadPRData(){
        for(int i=0; i < 10; i++){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/PRPOBox.fxml"));
                Parent boxPane = loader.load();

                PRPOBoxController boxController = loader.getController();
                if(i < 6){
                    boxController.setBoxStatus("pending");
                } else {
                    boxController.setBoxStatus("approved");
                }
                boxController.setPRData();

                pr_list_container.getChildren().add(boxPane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadPOData(){
        for(int i=0; i < 4; i++){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/PRPOBox.fxml"));
                Parent boxPane = loader.load();

                PRPOBoxController boxController = loader.getController();
                boxController.setBoxStatus("pending");
                boxController.setPOData();
                po_list_container.getChildren().add(boxPane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchTab(String procurement_type){
        if(procurement_type.toLowerCase().equals("request")){
            po_pane.setVisible(false);
            order_tab.getStyleClass().remove("selected");

            pr_pane.setVisible(true);
            if(!request_tab.getStyleClass().contains("selected")){
                request_tab.getStyleClass().add("selected");
            }
        } else {
            pr_pane.setVisible(false);
            request_tab.getStyleClass().remove("selected");

            po_pane.setVisible(true);
            if(!order_tab.getStyleClass().contains("selected")){
                order_tab.getStyleClass().add("selected");
            }
        }
    }

    // TODO: Add the function to Create New PR
    public void createNewPR(MouseEvent mouseEvent) {
        System.out.println("clicked");
    }

    public void buttonMouseEntered(MouseEvent mouseEvent) {
        create_new_pr_button.setOpacity(0.8);
    }

    public void buttonMouseExited(MouseEvent mouseEvent) {
        create_new_pr_button.setOpacity(1);
    }

    public void switch_to_pr(MouseEvent mouseEvent) {
        switchTab("request");
    }

    public void switch_to_po(MouseEvent mouseEvent) {
        switchTab("order");
    }
}
