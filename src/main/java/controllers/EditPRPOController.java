package controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import jdk.swing.interop.SwingInterOpUtils;
import models.DTO.PODataDTO;
import models.DTO.PRDataDTO;
import models.Datas.Item;
import models.Datas.PurchaseOrder;
import models.Datas.PurchaseOrderItem;
import models.Datas.PurchaseRequisitionItem;
import models.Users.User;
import models.Utils.*;
import views.NotificationView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class EditPRPOController implements Initializable {
    public interface ItemRow {
        String getItemID();
        String getItemName();
        int getQuantity();
    }

    @FXML
    public Text title_text; // Eg. Purchase Request Item List
    @FXML
    public AnchorPane statusbox_container;
    @FXML
    public HBox label_text_container;
    @FXML
    public ImageView label_1_image;
    @FXML
    public Text label_1_text; // Eg. PR ID: PR2301241
    @FXML
    public ImageView label_2_image;
    @FXML
    public Text label_2_text; //  Eg. Created By: John Doe
    @FXML
    public HBox save_change_button_container; // The container for the 'Discard' & 'Save Changes' button
    @FXML
    private TableView<ItemRow> item_list_table;
    @FXML
    private TableColumn<ItemRow, String> itemIDCol;
    @FXML
    private TableColumn<ItemRow, String> itemNameCol;
    @FXML
    private TableColumn<ItemRow, Integer> quantityCol;
    @FXML
    private TableColumn<ItemRow, Void> xButtonCol;
    @FXML
    public Button edit_button;
    @FXML
    public HBox po_title_container;
    @FXML
    public Text po_title_text;
    @FXML
    public TextField po_title_text_field;
    @FXML
    public Button action_button_1;
    @FXML
    public Button discard_button;
    @FXML
    public Button save_change_button;
    @FXML
    public Button action_button_2;
    // Action button are the 2 button in the bottom right corner, it has different looks and function based on role:
    // [  1  ] [  2  ]
    // Button 1 - for IM = 'Mark As Returned'
    // Button 2 -
        // SM = Create PR / Delete PR
        // PM = Verify PR / Delete PO
        // FM = Approve PO
        // IM = Verify PO
    @FXML
    public AnchorPane dimmedBg;
    @FXML
    public AnchorPane popup_modal;
    @FXML
    public ComboBox<Item> item_dropdown;
    @FXML
    public TextField qty_text_field;
    @FXML
    public AnchorPane popup_notif;
    @FXML
    public Text delete_confirmation_message;
    @FXML
    public Text prpo_id_text_field;

    private SessionManager session = SessionManager.getInstance();
    private String user_role = session.getUserData().get("roleID");

    private AccessPermission.AccessType currentAccess;

    public StatusBoxController statusBoxController;
    public PODataDTO poData;
    public PRDataDTO prData;

    private int quantity = 1;
    private Item selectedItem;
    private ObservableList<ItemRow> tableData;
    private ObservableList<ItemRow> originalTableData;
    private String originalPOTitle;

    private NotificationView notification;

    public List<Item> itemArrayList;
    {
        try {
            itemArrayList = new QueryBuilder<>(Item.class).select().from("db/Item.txt").getAsObjects();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public ObservableList<Item> itemList = FXCollections.observableArrayList(itemArrayList);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        save_change_button_container.setVisible(false); // Hide 'Discard' & 'Save Changes' button

        // Load the statusbox
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/StatusBox.fxml"));
            Node statusBoxNode = loader.load();

            statusBoxController = loader.getController();
            statusbox_container.getChildren().add(statusBoxNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Setup the item dropdown in the popup Modal
        item_dropdown.setConverter(new StringConverter<Item>() {
            @Override
            public String toString(Item item) {
                return item == null ? "" : item.getItemID() + " - " + item.getItemName();
            }

            @Override
            public Item fromString(String string) {
                // User typed something manually — find the best match
                for (Item item : itemList) {
                    String display = item.getItemID() + " - " + item.getItemName();
                    if (display.equalsIgnoreCase(string)) {
                        return item;
                    }
                }
                return null; // not found
            }
        });

        // Add listener to each dropdown item in the ComboBox
        item_dropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal instanceof Item) {
                System.out.println("Selected Item ID: " + newVal.getItemID());
                System.out.println("Selected Item Name: " + newVal.getItemName());
                selectedItem = newVal;
            } else {
                System.out.println("Invalid selection (not an Item): " + newVal);
            }
        });

        // Add listener to the title text field
        po_title_text_field.textProperty().addListener((observable, oldValue, newValue) -> {
            isTitleModified();
        });
    }

    public void setupTableStructure(){
        try {
            if (prData == null && poData == null) {
                System.out.println("Both PR and PO data are null.");
                return;
            }

            // Set up the table columns
            itemIDCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemID()));
            itemNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemName()));
            quantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

            tableData = FXCollections.observableArrayList();
            originalTableData = FXCollections.observableArrayList();

            if (prData != null) {
                tableData.addAll(prData.getPrItemList());
                originalTableData.addAll(prData.getPrItemList());
            } else {
                tableData.addAll(poData.getPoItemList());
                originalTableData.addAll(poData.getPoItemList());
                originalPOTitle = poData.getTitle();
            }

            item_list_table.setItems(tableData);

            // Setup item list dropdown (ComboBox) in the hidden popup modal
            item_dropdown.setItems(itemList);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Add a x button on each row of table data
    public void addActionButtons() {
        xButtonCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("✕");
            {
                deleteBtn.setOnAction(event -> {
                    ItemRow currentItem = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(currentItem);
                    updateSaveChangeContainer();
                    // Optional: add backend update logic here
                });

                deleteBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 5px; -fx-border-color: transparent");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    // Example usage:
    // thisController.loadPageContent(EditPRPOController.AccessType.EDIT_PO, poDataDTO);
    public void loadPageContent(AccessPermission.AccessType access, PODataDTO poData){
        currentAccess = access;
        this.poData = poData;

        // Setup label's image
        Image cargoIconImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/image/cargo_icon.png")),
                20, 20,
                false,  // preserveRatio
                true    // smooth scaling
        );
        label_1_image.setImage(cargoIconImage);
        label_2_image.setImage(cargoIconImage);

        // Setup label's text
        label_1_text.setText("PO ID: " + poData.getPoID());
        label_2_text.setText("Linked PR: " + poData.getPrID());

        statusBoxController.setStatus(poData.getStatus());

        conditionalRendering();
        setupTableStructure();
    }

    public void loadPageContent(AccessPermission.AccessType access, PRDataDTO prData){
        currentAccess = access;
        this.prData = prData;

        // Setup label's image
        Image cargoIconImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/image/cargo_icon.png")),
                20, 20,
                false,  // preserveRatio
                true    // smooth scaling
        );
        Image userIconImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/image/user_icon_blue.png")),
                20, 20,
                false,  // preserveRatio
                true    // smooth scaling
        );
        label_1_image.setImage(cargoIconImage);
        label_2_image.setImage(userIconImage);

        // Setup label's text
        label_1_text.setText("PR ID: " + prData.getPrID());
        label_2_text.setText("Created By: " + prData.getUserName());

        statusBoxController.setStatus(prData.getStatus());

        conditionalRendering();
        setupTableStructure();
    }

    public void conditionalRendering() {
        switch (currentAccess) {
            case NO_ACCESS -> {
                System.out.println("You should've even be here, something went wrong");
                System.out.println("Message location: EditPRPOController.java line 169");
            }

            case VIEW_PR, VIEW_PO -> {
                System.out.println("RENDERING: VIEW PR/ VIEW PO");
                edit_button.setVisible(false);
                save_change_button_container.setVisible(false);
                action_button_1.setVisible(false);
                action_button_2.setVisible(false);
            }

            case CREATE_PR -> {
                System.out.println("RENDERING: CREATE PR");
                title_text.setText("New Purchase Request");
                label_text_container.setVisible(false);

                action_button_1.setVisible(false);

                action_button_2.setText("Create New PR");
                action_button_2.setOnMouseClicked(event ->{
                    System.out.println("Your create PR function here");
                });

                edit_button.setText("Add Item +");
//                edit_button.setOnMouseClicked(event -> {
//                    System.out.println("Add item function here");
//                });

                // other code here...

            }

            case EDIT_PR -> {
                System.out.println("RENDERING: EDIT PR");
                title_text.setText("Purchase Request Item List");

                addActionButtons();

                action_button_1.setVisible(false);
                action_button_2.setText("Delete PR");
                action_button_2.setOnMouseClicked(event ->{
                    System.out.println("Your Delete PR function here");
                });

                discard_button.setOnMouseClicked(event -> {
                    System.out.println("Discard change function here");
                });

                save_change_button.setOnMouseClicked(event -> {
                    System.out.println("Save change function here");
                });

                edit_button.setText("Add Item +");
//                edit_button.setOnMouseClicked(event -> {
//                    System.out.println("Add item function here");
//                });
            }

            // FOR: Purchase, Finance & Inventory Manager
                // PM can Edit PO's item list & Delete PO
                // IM can Verify or Return PO
                // FM can edit PO's item list & Approve PO
            case EDIT_PO -> {
                System.out.println("RENDERING: EDIT PO: "+user_role);
                addActionButtons();
                switch(user_role){
                    // Admin
                    case "1" -> {
                        // I have absolutely no idea about this :skull:
                    }

                    // Purchase AND Finance
                    case "3", "5" -> {
                        title_text.setText("Purchase Order Item List");
                        action_button_1.setVisible(false);

                        if(user_role.equals("3")){
                            action_button_2.setStyle(
                                    "-fx-background-color: rgb(255, 178, 178); " +
                                            "-fx-background-radius: 5px; " +
                                            "-fx-text-fill: rgb(102, 48, 48);"
                            );

                            action_button_2.setText("Delete PO");
                            action_button_2.setOnMouseClicked(event -> {
                                System.out.println("ORIGINAL");
                                for(ItemRow item : originalTableData){
                                    System.out.println(item.getItemID() + " - " + item.getItemName() + " *"+item.getQuantity());
                                }

                                System.out.println(" ");
                                System.out.println("CURRENT DATA");
                                for(ItemRow item : tableData){
                                    System.out.println(item.getItemID() + " - " + item.getItemName() + " *"+item.getQuantity());
                                }
                            });
                        } else {
                            action_button_2.setText("Approve PO");
                            action_button_2.setOnMouseClicked(event -> {
                                System.out.println("Approve PO function here");
                                QueryBuilder<PurchaseOrder> POqb;
                                try {
                                    POqb = new QueryBuilder<>(PurchaseOrder.class).target("db/PurchaseOrder.txt");
                                    String[] newPOData = new String[] {
                                            poData.getPrID(),
                                            poData.getUserID(),
                                            poData.getTitle(),
                                            String.valueOf(poData.getPayableAmount()),
                                            "approved"
                                    };
                                    boolean approveRes = POqb.update(poData.getPoID(), newPOData);
                                    if(approveRes){
                                        statusBoxController.setStatus("approved");
                                        notification = new NotificationView("Purhcase Order - "+ poData.getPoID() + " Approved Successfully", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                                    } else {
                                        notification = new NotificationView("Failed To Approved Purchase Order - "+poData.getPoID(), NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                                    }

                                    notification.show();
                                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                                         IllegalAccessException | IOException e) {
                                    throw new RuntimeException(e);
                                }

                            });
                        }

                        // For other button, PM and FM is the same, so leave it.
                        save_change_button.setOnMouseClicked(event -> {
                            try {
                                // Deleting all target PO Item's
                                Path filePath = Paths.get("src/main/java/db/PurchaseOrderItem.txt");
                                List<String> lines = Files.readAllLines(filePath);

                                // Filter out the line to delete
                                List<String> updatedLines = lines.stream()
                                        .filter(line -> !line.split(",")[1].equals(poData.getPoID()))
                                        .collect(Collectors.toList());

                                Files.write(filePath, updatedLines,
                                        StandardOpenOption.WRITE,
                                        StandardOpenOption.TRUNCATE_EXISTING,
                                        StandardOpenOption.CREATE);

                                // Writting all the new PO item's
                                QueryBuilder<PurchaseOrder> POqb = new QueryBuilder<>(PurchaseOrder.class);
                                QueryBuilder<PurchaseOrderItem> POIqb = new QueryBuilder<>(PurchaseOrderItem.class);
                                String[] attrs = POIqb.getAttrs(false);
                                HashMap<String, Item> itemMap = Item.getItemMap();
                                double totalPayableAmount = 0;
                                boolean successFlag = true;

                                for(ItemRow itemRow : tableData){
                                    // Converts itemRow into new POItem record
                                    String[] newPOItem = new String[]{
                                            poData.getPoID(),
                                            itemRow.getItemID(),
                                            String.valueOf(itemRow.getQuantity())
                                    };

                                    // Stacking (quantity * unitPrice) to get total amount
                                    totalPayableAmount += itemMap.get(itemRow.getItemID()).getUnitPrice() * itemRow.getQuantity();

                                    if (newPOItem.length != attrs.length) {
                                        notification = new NotificationView("Invalid Purchase Order Item Input", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                                        notification.show();
                                        throw new Exception("Data length does not match attribute length");
                                    }else {
                                        boolean res = POIqb.target("db/PurchaseOrderItem.txt").values(newPOItem).create();
                                        if (!res) {
                                            successFlag = false;
                                        }
                                    }
                                }

                                // Round to 2 decimal. Eg: 1200.548 -> 1200.55
                                totalPayableAmount = Math.ceil(totalPayableAmount * 100.0) / 100.0;

                                // Update PO data (title)
                                String newTitle = po_title_text_field.getText();
                                // Format of PurchaseOrder: PO1,PR2,US12,A purchase order,1011.89,approved
                                String[] newPOData = new String[] {
                                        poData.getPrID(),
                                        poData.getUserID(),
                                        newTitle,
                                        String.valueOf(totalPayableAmount),
                                        poData.getStatus()
                                };
                                String[] poAttrs = POqb.getAttrs(false);
                                if(newPOData.length!= poAttrs.length || newTitle.contains(",")){
                                    notification = new NotificationView("Invalid Purchase Order Title Input. Please do not include the character ','", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                                    notification.show();
                                    return;
                                }
                                boolean res = POqb.target("db/PurchaseOrder.txt").update(poData.getPoID(), newPOData);

                                if(successFlag && res){
                                    notification = new NotificationView("Changes Have Been Saved", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                                    notification.show();
                                    originalTableData.setAll(tableData);
                                    originalPOTitle = po_title_text_field.getText();
                                    updateSaveChangeContainer();
                                }

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                        discard_button.setOnMouseClicked(event -> {
                            System.out.println("Discard changes function here");
                            // Revert back to the original table data
                            tableData.setAll(originalTableData);
                            po_title_text_field.setText(originalPOTitle);
                            updateSaveChangeContainer();

                            try {
                                notification = new NotificationView("Changes discarded", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            notification.show();
                        });

                        po_title_container.setVisible(true);
                        po_title_text.setText("Purchase Order Title: ");
                        po_title_text_field.setText(poData.getTitle());
                        po_title_text_field.setEditable(true);

                        edit_button.setText("Add Item +");

                    }

                    // Inventory
                    case "4" -> {
                        edit_button.setText("Edit Item");
                        action_button_1.setText("Mark As Returned");
                        action_button_1.setOnMouseClicked(event -> {
                            System.out.println("Mark the PO as Returned");
                        });

                        action_button_2.setText("Verify PO");
                        action_button_2.setOnMouseClicked(event -> {
                            System.out.println("Verify PO");
                        });

                    }
                }
            }

            // FOR: Purchase Manager & Admin
            case APPROVE_PR -> {
                System.out.println("RENDERING: APPROVE PR");
                title_text.setText("Purchase Request Item List");

                po_title_container.setVisible(true);
                po_title_text.setText("Enter a Purchase Order Title: ");
                po_title_text_field.setEditable(true);

                action_button_1.setVisible(false);
                action_button_2.setText("Approve PR");
                action_button_2.setOnMouseClicked(event -> {
                    // Check if the title is typed or not
                    System.out.println("Approve PR function here");
                });

                edit_button.setVisible(false);

            }

            default -> {
                System.out.println("Err message location: EditPRPOController.java");
                throw new IllegalStateException("Unexpected value: " + currentAccess);
            }
        }
    }

    // FXML button onClick function
    public void closeAllPopup(MouseEvent mouseEvent) {
        popup_modal.setVisible(false);
        popup_notif.setVisible(false);
        dimmedBg.setVisible(false);
    }

    public void closePopupModal(MouseEvent mouseEvent) {
        popup_modal.setVisible(false);
        dimmedBg.setVisible(false);
    }

    public void decreaseQuantity(MouseEvent mouseEvent) {
        if(quantity <= 1){
            return;
        }
        quantity -= 1;
        qty_text_field.setText(String.valueOf(quantity));
    }

    public void increaseQuantity(MouseEvent mouseEvent) {
        quantity += 1;
        qty_text_field.setText(String.valueOf(quantity));
    }

    public void addItem(MouseEvent mouseEvent) {
        if (selectedItem == null) {
            // Notif user on selecting an item first
            return;
        }

        boolean existingItem = false;
        int existingItemIndex = -1;

        for(int i = 0; i < tableData.size(); i++){
            if(selectedItem.getItemID().equals(tableData.get(i).getItemID())){
                existingItem = true;
                existingItemIndex = i;
                break;
            }
        }

        if(existingItem){
            // Add the quantity on top of previous value
            int sumQty = tableData.get(existingItemIndex).getQuantity() + quantity;
            ItemRow newItem = new Item(selectedItem.getItemID(), selectedItem.getItemName(), sumQty);
            tableData.set(existingItemIndex, newItem);
        } else {
            // Add a new record
            tableData.add(new Item(selectedItem.getItemID(), selectedItem.getItemName(), quantity));
        }

        closePopupModal(mouseEvent);
        updateSaveChangeContainer();
    }


    public void closePopupNotif(MouseEvent mouseEvent) {
        popup_notif.setVisible(false);
        dimmedBg.setVisible(false);
    }

    public void openPopupModal(){
        dimmedBg.setVisible(true);
        popup_modal.setVisible(true);
        qty_text_field.setText(String.valueOf(quantity));
    }

    public void openPopupNotif(){
        popup_notif.setVisible(true);
        // To be continued
    }

    public void deletePRPO(MouseEvent mouseEvent) {
    }

    private void updateSaveChangeContainer (){
        if(isTableModified()){
            save_change_button_container.setVisible(true);
        } else {
            save_change_button_container.setVisible(false);
        }
    }

    private boolean isTableModified() {
        List<ItemRow> sortedOriginal = new ArrayList<>(originalTableData);
        List<ItemRow> sortedCurrent = new ArrayList<>(tableData);

        Comparator<ItemRow> comparator = Comparator.comparing(ItemRow::getItemID);
        sortedOriginal.sort(comparator);
        sortedCurrent.sort(comparator);

        if (sortedOriginal.size() != sortedCurrent.size()) return true;

        for (int i = 0; i < sortedOriginal.size(); i++) {
            ItemRow item1 = sortedOriginal.get(i);
            ItemRow item2 = sortedCurrent.get(i);

            if (!item1.getItemID().equals(item2.getItemID()) ||
                    !item1.getItemName().equals(item2.getItemName()) ||
                    item1.getQuantity() != item2.getQuantity()) {
                return true;
            }
        }

        return false;
    }

    private void isTitleModified(){
        if(originalPOTitle == null) {
            return;
        }
        save_change_button_container.setVisible(!originalPOTitle.equals(po_title_text_field.getText()));
    }

}

