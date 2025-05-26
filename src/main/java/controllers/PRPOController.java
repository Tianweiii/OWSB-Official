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
import models.Datas.PurchaseRequisition;
import models.Utils.AccessPermission;
import models.Utils.Navigator;
import models.Utils.QueryBuilder;
import models.Utils.SessionManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private String user_role;
    private boolean viewingPRPane;
    private final ObservableList<String> PRFilterList = FXCollections.observableArrayList("ALL", "PENDING", "LATE", "APPROVED");
    private final ObservableList<String> POFilterList = FXCollections.observableArrayList("ALL", "PENDING", "APPROVED", "VERIFIED", "PAID", "RETURNED", "DELETED");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updatePRStatus();
        // Get user role info
        SessionManager session = SessionManager.getInstance();
        user_role = session.getUserData().get("roleID");

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

            // Show the Create PR Button if user is sales manager or admin (Only SM & Admin can create PR)
            if(user_role.equals("2") || user_role.equals("1")){
                create_new_pr_button.setVisible(true);
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
            (procurement_type.equalsIgnoreCase("order") && !viewingPRPane && !user_role.equals("4"))
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

    public void createNewPR(MouseEvent mouseEvent) throws IOException {
        Navigator navigator = Navigator.getInstance();
        FXMLLoader editPRPOLoader = new FXMLLoader(getClass().getResource("/PRPO/EditPRPO.fxml"));
        navigator.navigate(editPRPOLoader.load());
        EditPRPOController editPRPOcontroller = editPRPOLoader.getController();
        editPRPOcontroller.loadPageContent(AccessPermission.AccessType.CREATE_PR, (PRDataDTO) null);
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

    public void updatePRStatus(){
        try {
            QueryBuilder<PurchaseRequisition> PRqb = new QueryBuilder<>(PurchaseRequisition.class);
            List<PurchaseRequisition> prList = PRqb.select().from("db/PurchaseRequisition.txt").getAsObjects();

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
            LocalDate today = LocalDate.now();

            ArrayList<String> targetIdList = new ArrayList<>();
            ArrayList<String[]> writtingData = new ArrayList<>();

            for(PurchaseRequisition pr : prList){
                String status = pr.getPRStatus().trim().toLowerCase();
                LocalDate requiredByDate = LocalDate.parse(pr.getReceivedByDate(), formatter);

                if (status.equals("pending") && !today.isBefore(requiredByDate)) {
                    targetIdList.add(pr.getPrRequisitionID());
                    writtingData.add(new String[]{
                        pr.getUserID(),
                        "late",
                        pr.getCreatedDate(),
                        pr.getReceivedByDate()
                    });
                }
            }

            String[] targetIds = targetIdList.toArray(new String[0]);
            PRqb.target("db/PurchaseRequisition.txt").updateManyParallelArr(targetIds, writtingData);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 IOException e) {
            throw new RuntimeException(e);
        }
    }
}
