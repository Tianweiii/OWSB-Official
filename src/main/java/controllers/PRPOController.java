package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.DTO.PODataDTO;
import models.DTO.PRDataDTO;
import models.Utils.Navigator;
import models.Utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PRPOController implements Initializable {
    @FXML
    private Text pending_pr_text;
    @FXML
    private Button request_tab;
    @FXML
    private Button order_tab;
    @FXML
    public ComboBox filter_dropdown;
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

    private boolean viewingPRPane;
    private final ObservableList<String> PRFilterList = FXCollections.observableArrayList("ALL", "PENDING", "LATE", "APPROVED");
    private final ObservableList<String> POFilterList = FXCollections.observableArrayList("ALL", "PENDING", "APPROVED", "VERIFIED", "PAID", "RETURNED", "DELETED");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get user role info
        SessionManager session = SessionManager.getInstance();
        String user_role = session.getUserData().get("roleID");

        // Hide the PR/PO Tab based on role:
            // Admin & Sales & Purchase & Finance = show both PR & PO Tab
            // Inventory = show PO Tab ONLY
        try {
            if(user_role.equals("4")){
                // Hiding the PR tab and PR Pane
                request_tab.setVisible(false);
                request_tab.setManaged(false); //  Tell the layout not to reserve space for this button
                pr_pane.setVisible(false);

                // load data
                loadPOData("all");
                // initialize the PR/PO list display
                switchTab("order");
            } else {
                loadPRData("all");
                loadPOData("all");
                switchTab("request");
            }

            // Hide the Create PR Button if user is not sales manager (Only SM can create PR)
            if(!user_role.equals("2")){
                create_new_pr_button.setVisible(false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Add listener for filter dropdown
        filter_dropdown.setOnAction(event -> {
            if(filter_dropdown.getValue() == null){
                return;
            }

            String selectedFilter = filter_dropdown.getValue().toString();
            try {
                if(viewingPRPane){
                    loadPRData(selectedFilter);
                } else{
                    loadPOData(selectedFilter);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void loadPRData(String filter_status) throws Exception {
        pr_list_container.getChildren().clear();
        List<PRDataDTO> prDTOList = PRDataDTO.getPRDataDTOs(filter_status.toLowerCase());
        for(PRDataDTO prDTO : prDTOList){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/PRPOBox.fxml"));
                Parent boxPane = loader.load();

                PRPOBoxController boxController = loader.getController();
                boxController.setPRData(prDTO);
                pr_list_container.getChildren().add(boxPane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadPOData(String filter_status) throws Exception {
        po_list_container.getChildren().clear();
        List<PODataDTO> poDTOList = PODataDTO.getPODataDTOs(filter_status.toLowerCase());
        for(PODataDTO poDTO : poDTOList){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/PRPOBox.fxml"));
                Parent boxPane = loader.load();

                PRPOBoxController boxController = loader.getController();
                boxController.setPOData(poDTO);
                po_list_container.getChildren().add(boxPane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchTab(String procurement_type){
        // Prevent refetching and reloading data when clicked on the same tab
        if((procurement_type.equalsIgnoreCase("request") && viewingPRPane)
            ||
            (procurement_type.equalsIgnoreCase("order") && !viewingPRPane)
        ){
            return;
        }

        if(procurement_type.toLowerCase().equals("request")){
            filter_dropdown.setItems(PRFilterList);
            filter_dropdown.setValue("ALL");

            po_pane.setVisible(false);
            order_tab.getStyleClass().remove("selected");

            pr_pane.setVisible(true);
            if(!request_tab.getStyleClass().contains("selected")){
                request_tab.getStyleClass().add("selected");
            }

            viewingPRPane = true;
        } else {
            filter_dropdown.setItems(POFilterList);
            filter_dropdown.setValue("ALL");

            pr_pane.setVisible(false);
            request_tab.getStyleClass().remove("selected");

            po_pane.setVisible(true);
            if(!order_tab.getStyleClass().contains("selected")){
                order_tab.getStyleClass().add("selected");
            }
            viewingPRPane = false;
        }
    }

    // TODO: Add the function to Create New PR
    public void createNewPR(MouseEvent mouseEvent) {
        System.out.println("clicked");
        Navigator navigator = Navigator.getInstance();
        navigator.navigate(navigator.getRouters("sales").getRoute("EditPRPO"));
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
