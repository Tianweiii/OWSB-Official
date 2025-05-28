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
import models.DTO.PODataDTO;
import models.DTO.PRDataDTO;
import models.Datas.*;
import models.Utils.*;
import views.NotificationView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static models.Datas.Item.stringifyDateTime;

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
    public Text label_2_text; //  Eg. Linked PR: PR2301241 || PR Created By: John Doe
    @FXML
    public ImageView label_3_image;
    @FXML
    public Text label_3_text; // Eg. PO Created By: John Doe
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
    public HBox datepicker_container;
    @FXML
    public DatePicker datepicker;
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
    @FXML
    public Button popupNotifActionButton;

    private SessionManager session = SessionManager.getInstance();
    private String user_role = session.getUserData().get("roleID");
    private String user_id = session.getUserData().get("userID");

    private AccessPermission.AccessType currentAccess;

    public StatusBoxController statusBoxController;
    public PODataDTO poData;
    public PRDataDTO prData;

    private int quantity = 1;
    private Item selectedItem;
    private ObservableList<ItemRow> tableData;
    private ObservableList<ItemRow> originalTableData;
    HashMap<String, Item> itemMap;
    private String originalPOTitle;
    private String originalPRDate;

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

        // Adding listener to elements
        item_dropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal instanceof Item) {
                selectedItem = newVal;
            }
        });

        po_title_text_field.textProperty().addListener((observable, oldValue, newValue) -> {
            isTitleModified();
        });

        qty_text_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*")) {
                // Only digits allowed
                quantity = newValue.isEmpty() ? 1 : Integer.parseInt(newValue);
            } else {
                // Reject invalid input by reverting to old value
                qty_text_field.setText(oldValue);
            }
        });

        datepicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            isDateModified();
        });


        try {
            itemMap = Item.getItemMap();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setupTableStructure(){
        try {
            // Set up the table columns
            itemIDCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemID()));
            itemNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemName()));
            quantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

            tableData = FXCollections.observableArrayList();
            originalTableData = FXCollections.observableArrayList();

            // Setup item list dropdown (ComboBox) in the hidden popup modal
            item_dropdown.setItems(itemList);
            item_list_table.setItems(tableData);

            if(currentAccess.equals(AccessPermission.AccessType.CREATE_PR)){
                return;
            }
            if (prData == null && poData == null) {
                return;
            }

            if (prData != null) {
                tableData.addAll(prData.getPrItemList());
                originalTableData.addAll(prData.getPrItemList());
                originalPRDate = prData.getReceivedByDate();
            } else {
                tableData.addAll(poData.getPoItemList());
                originalTableData.addAll(poData.getPoItemList());
                originalPOTitle = poData.getTitle();
            }
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
        label_2_image.setImage(cargoIconImage);
        label_3_image.setImage(userIconImage);
        label_3_image.setVisible(true);

        // Setup label's text
        label_1_text.setText("PO ID: " + poData.getPoID());
        label_2_text.setText("Linked PR: " + poData.getPrID());
        label_3_text.setText("PO Created By: " + poData.getUserName());
        label_3_text.setVisible(true);

        statusBoxController.setStatus(poData.getStatus());

        conditionalRendering();
        setupTableStructure();
    }

    public void loadPageContent(AccessPermission.AccessType access, PRDataDTO prData){
        currentAccess = access;
        this.prData = prData;

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
        datepicker_container.setVisible(true);

        // Setup label's text
        if(prData != null){
            label_1_text.setText("PR ID: " + prData.getPrID());
            label_2_text.setText("PR Created By: " + prData.getUserName());

            statusBoxController.setStatus(prData.getStatus());

            // Parsing string to LocalDate
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
            LocalDate date = LocalDate.parse(prData.getReceivedByDate(), formatter);
            datepicker.setValue(date);
        }

        conditionalRendering();
        setupTableStructure();
    }

    public void conditionalRendering() {
        switch (currentAccess) {
            case VIEW_PR, VIEW_PO -> {
                edit_button.setVisible(false);
                save_change_button_container.setVisible(false);
                action_button_1.setVisible(false);
                action_button_2.setVisible(false);
                if(poData != null){
                    po_title_container.setVisible(true);
                    po_title_text_field.setText(poData.getTitle());
                } else if (prData != null){
                    datepicker.setEditable(false);
                    datepicker.setDisable(true);
                }
            }

            case CREATE_PR -> {
                addActionButtons();

                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
                String todayDate = LocalDate.now().format(formatter);

                title_text.setText("New Purchase Request");
                Image calendarIconImage = new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream("/Assets/icon/calendar_icon.png")),
                        20, 20,
                        false,  // preserveRatio
                        true    // smooth scaling
                );
                label_1_text.setText("Today's Date: " + todayDate);
                label_1_image.setImage(calendarIconImage);
                label_2_text.setVisible(false);
                label_2_image.setVisible(false);
                statusbox_container.setVisible(false);

                LocalDate defaultRequiredDate = LocalDate.now().plusDays(7);
                datepicker.setValue(defaultRequiredDate);

                action_button_1.setVisible(false);

                action_button_2.setText("Create New PR");
                action_button_2.setOnMouseClicked(event ->{
                    createNewPR();
                });

                edit_button.setText("Add Item +");

            }

            case EDIT_PR -> {
                addActionButtons();

                title_text.setText("Purchase Request Item List");
                action_button_1.setVisible(false);
                action_button_2.setText("Delete PR");
                action_button_2.setStyle(
                        "-fx-background-color: rgb(255, 178, 178); " +
                                "-fx-background-radius: 5px; " +
                                "-fx-text-fill: rgb(102, 48, 48);"
                );
                action_button_2.setOnMouseClicked(event ->{
                    setPopUpNotifButtonToRed("Delete");
                    prpo_id_text_field.setText(prData.getPrID());
                    delete_confirmation_message.setText("Are you sure you want to delete this PR?");
                    openPopupNotif();
                });

                popupNotifActionButton.setOnMouseClicked(event ->{
                    deletePR();
                });

                discard_button.setOnMouseClicked(event -> {
                    discardPRPOChanges();
                });

                save_change_button.setOnMouseClicked(event -> {
                    applyPRChanges();
                });

                edit_button.setText("Add Item +");
            }

            // FOR: Purchase, Finance & Inventory Manager
                // PM can Edit pending/late PO's item list & Delete pending PO
                // IM can Verify or Return Approved PO
                // FM can edit pending PO's item list & Approve pending PO
            case EDIT_PO -> {
                addActionButtons();
                title_text.setText("Purchase Order Item List");
                action_button_1.setVisible(false);
                po_title_container.setVisible(true);
                po_title_text.setText("Purchase Order Title: ");
                po_title_text_field.setText(poData.getTitle());
                po_title_text_field.setEditable(true);
                edit_button.setText("Add Item +");

                save_change_button.setOnMouseClicked(event -> {
                    applyPOChanges();
                });

                discard_button.setOnMouseClicked(event -> {
                    discardPRPOChanges();
                });

                action_button_2.setText("Delete PO");
                action_button_2.setStyle(
                        "-fx-background-color: rgb(255, 178, 178); " +
                                "-fx-background-radius: 5px; " +
                                "-fx-text-fill: rgb(102, 48, 48);"
                );
                action_button_2.setOnMouseClicked(event -> {
                    setPopUpNotifButtonToRed("Delete");
                    prpo_id_text_field.setText(poData.getPoID());
                    delete_confirmation_message.setText("Are you sure you want to delete this PO?");
                    openPopupNotif();
                });

                popupNotifActionButton.setOnMouseClicked(event -> {
                    try {
                        updatePRPOStatus("deleted", "Purchase Order - "+ poData.getPoID() + " Deleted Successfully", "Failed To Delete Purchase Order - "+poData.getPoID());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            case APPROVE_PO -> {
                addActionButtons();

                action_button_2.setText("Approve PO");
                action_button_2.setOnMouseClicked(event -> {
                    setPopUpNotifButtonToNormal("Approve");
                    prpo_id_text_field.setText(poData.getPoID());
                    delete_confirmation_message.setText("Approve this PO?");
                    openPopupNotif();
                });

                popupNotifActionButton.setOnMouseClicked(event -> {
                    try {
                        updatePRPOStatus("approved", "Purchase Order - "+ poData.getPoID() + " Approved Successfully", "Failed To Approved Purchase Order - "+poData.getPoID());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                title_text.setText("Purchase Order Item List");
                action_button_1.setVisible(false);
                po_title_container.setVisible(true);
                po_title_text.setText("Purchase Order Title: ");
                po_title_text_field.setText(poData.getTitle());
                po_title_text_field.setEditable(true);
                edit_button.setText("Add Item +");

                save_change_button.setOnMouseClicked(event -> {
                    applyPOChanges();
                });

                discard_button.setOnMouseClicked(event -> {
                    discardPRPOChanges();
                });
            }

            case VERIFY_PO -> {
                edit_button.setVisible(false);
                action_button_1.setText("Mark As Returned");
                po_title_container.setVisible(true);
                po_title_text_field.setText(poData.getTitle());

                prpo_id_text_field.setText(poData.getPoID());
                action_button_1.setOnMouseClicked(event -> {
                    setPopUpNotifButtonToRed("Return PO");
                    delete_confirmation_message.setText("Mark PO as Returned?");
                    openPopupNotif();

                    popupNotifActionButton.setOnMouseClicked(event2 -> {
                        try {
                            updatePRPOStatus("returned", "Purchase Order - "+ poData.getPoID() + " Is Marked As Returned", "Failed To Return Purchase Order - "+poData.getPoID());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                });

                action_button_2.setText("Verify PO");
                action_button_2.setOnMouseClicked(event -> {
                    setPopUpNotifButtonToNormal("Verify");
                    delete_confirmation_message.setText("Mark PO as Verified?");
                    openPopupNotif();

                    popupNotifActionButton.setOnMouseClicked(event2 -> {
                        verifyPO();
                    });
                });
            }

            // FOR: Purchase Manager & Admin
            case APPROVE_PR -> {
                title_text.setText("Purchase Request Item List");
                edit_button.setVisible(false);

                po_title_container.setVisible(true);
                po_title_text.setText("Enter a Purchase Order Title: ");
                po_title_text_field.setEditable(true);
                datepicker_container.setVisible(false);

                action_button_1.setVisible(false);
                action_button_2.setText("Approve PR");
                action_button_2.setOnMouseClicked(event -> {
                    // Check if the title is typed or not
                    String poTitle = po_title_text_field.getText();
                    if(poTitle.isBlank()){
                        try {
                            notification = new NotificationView("Please enter a title for the purchase order first! ", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                            notification.show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    setPopUpNotifButtonToNormal("Approve");
                    prpo_id_text_field.setText(prData.getPrID());
                    delete_confirmation_message.setText("Approve this PR?");
                    openPopupNotif();
                });

                popupNotifActionButton.setOnMouseClicked(event -> {
                   approvePR();
                });

            }

            case EDIT_AND_APPROVE_PR -> {
                addActionButtons();

                title_text.setText("Purchase Request Item List");
                po_title_container.setVisible(true);
                po_title_text_field.setEditable(true);
                action_button_1.setText("Delete PR");
                action_button_1.setStyle(
                        "-fx-background-color: rgb(255, 178, 178); " +
                                "-fx-background-radius: 5px; " +
                                "-fx-text-fill: rgb(102, 48, 48);"
                );
                action_button_1.setOnMouseClicked(event ->{
                    setPopUpNotifButtonToRed("Delete");
                    prpo_id_text_field.setText(prData.getPrID());
                    delete_confirmation_message.setText("Are you sure you want to delete this PR?");
                    popupNotifActionButton.setOnMouseClicked(event2 ->{
                        deletePR();
                    });
                    openPopupNotif();
                });

                action_button_2.setText("Approve PR");
                action_button_2.setOnMouseClicked(event ->{
                    setPopUpNotifButtonToNormal("Approve");
                    prpo_id_text_field.setText(prData.getPrID());
                    delete_confirmation_message.setText("Approve this PR?");
                    popupNotifActionButton.setOnMouseClicked(event2 -> {
                        approvePR();
                    });
                    openPopupNotif();
                });

                discard_button.setOnMouseClicked(event -> {
                    discardPRPOChanges();
                });

                save_change_button.setOnMouseClicked(event -> {
                    applyPRChanges();
                });

                edit_button.setText("Add Item +");
            }

            default -> {
                System.out.println("Err message location: EditPRPOController.java");
                throw new IllegalStateException("Unexpected access: " + currentAccess);
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

    public void addItem(MouseEvent mouseEvent) throws IOException {
        if (selectedItem == null) {
            notification = new NotificationView("Please select an item in the drop down first", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
            notification.show();
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

        quantity = 1;
        closePopupModal(mouseEvent);
        updateSaveChangeContainer();
    }

    public void closePopupNotif(MouseEvent mouseEvent) {
        popup_notif.setVisible(false);
        dimmedBg.setVisible(false);
    }

    public void openPopupModal(){
        dimmedBg.setVisible(true);
        popup_notif.setVisible(false);
        popup_modal.setVisible(true);
        qty_text_field.setText(String.valueOf(quantity));
    }

    public void openPopupNotif(){
        dimmedBg.setVisible(true);
        popup_notif.setVisible(true);
        popup_modal.setVisible(false);
    }

    private void updateSaveChangeContainer (){
        // Creating a new PR isn't editing PR, so doesn't need to Save or Discard any changes
        if(currentAccess.equals(AccessPermission.AccessType.CREATE_PR)){
            return;
        }
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

    private void isDateModified(){
        if(originalPRDate == null){
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        String formattedDate = datepicker.getValue().format(formatter);
        save_change_button_container.setVisible(!originalPRDate.equals(formattedDate));
    }

    public void setPopUpNotifButtonToRed(String buttonText){
        popupNotifActionButton.setStyle("-fx-background-color: #F87474; -fx-background-radius: 5; -fx-text-fill: #663030;");
        popupNotifActionButton.setText(buttonText);
    }

    public void setPopUpNotifButtonToNormal(String buttonText){
        popupNotifActionButton.setStyle("-fx-background-color: #E9E9E9; -fx-background-radius: 5; -fx-text-fill: #505050;");
        popupNotifActionButton.setText(buttonText);
    }

    public void approvePR(){
        try {
            if(currentAccess == AccessPermission.AccessType.EDIT_AND_APPROVE_PR){
                // TODO: to be continued
                // Table, or date
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
                String formattedDate = datepicker.getValue().format(formatter);
                if(isTableModified() || !originalPRDate.equals(formattedDate)){
                    notification = new NotificationView("Please save your changes before approving PR.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                    notification.show();
                    return;
                } else if (po_title_text_field.getText().isBlank()){
                    notification = new NotificationView("Please enter a PO title before approving PR.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                    notification.show();
                    return;
                }
            }
            // Create new PO
            QueryBuilder<PurchaseOrder> POqb = new QueryBuilder<>(PurchaseOrder.class).target("db/PurchaseOrder.txt");

            double totalPayableAmount = 0;
            for(ItemRow itemRow : tableData){
                totalPayableAmount += itemMap.get(itemRow.getItemID()).getUnitPrice() * itemRow.getQuantity();
            }
            // Round up to 2 decimal place
            totalPayableAmount = Math.ceil(totalPayableAmount * 100.0) / 100.0;

            String[] newPOData = new String[] {
                    prData.getPrID(),
                    user_id,
                    po_title_text_field.getText().trim(),
                    String.valueOf(totalPayableAmount),
                    "pending"
            };

            if (po_title_text_field.getText().trim().contains(",")) {
                notification = new NotificationView("Invalid Purchase Order Title. ',' is a reserved symbols.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }

            boolean createPORes = POqb.values(newPOData).create();


            PurchaseOrder newlyCreatedPO = new QueryBuilder<>(PurchaseOrder.class).select().from("db/PurchaseOrder.txt").where("prID", "=", prData.getPrID()).getAsObjects().get(0);
            String newlyCreatedPOID = newlyCreatedPO.getpoID();

            boolean createPOItemRes = true;

            for(ItemRow itemRow : tableData){
                String[] newPOItemData = new String[] {
                        newlyCreatedPOID,
                        itemRow.getItemID(),
                        String.valueOf(itemRow.getQuantity())
                };

                QueryBuilder<PurchaseOrderItem> POIqb = new QueryBuilder<>(PurchaseOrderItem.class).target("db/PurchaseOrderItem.txt");
                boolean res = POIqb.values(newPOItemData).create();
                if(!res){
                    createPOItemRes = false;
                }
            }

            if(createPORes && createPOItemRes){
                updatePRPOStatus("approved", "Purchase Request - "+ prData.getPrID() + " Is Approved", "Failed To Approve Purchase Request - "+prData.getPrID());
            } else {
                notification = new NotificationView("Failed to Approve Purchase Request - " +prData.getPrID(), NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createNewPR(){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

            if(tableData.isEmpty()){
                notification = new NotificationView("Failed to create new Purchase Request. The PR is Empty.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }

            QueryBuilder<PurchaseRequisition> PRqb = new QueryBuilder<>(PurchaseRequisition.class);
            QueryBuilder<PurchaseRequisitionItem> PRIqb = new QueryBuilder<>(PurchaseRequisitionItem.class);

            // Update PR required date
            LocalDate selectedDate = datepicker.getValue();
            String newRequiredDate;
            if (selectedDate != null) {
                if(selectedDate.isBefore(LocalDate.now())){
                    notification = new NotificationView("Required date cannot be earlier than today", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                    notification.show();
                    return;
                }
                newRequiredDate = selectedDate.format(formatter);
            } else {
                notification = new NotificationView("Please Set a Valid Required Date", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }

            // Format of PurchaseOrderRequest: PR1,US12,pending,31/01/2025,31/12/2025
            String[] newPRData = new String[] {
                    user_id,
                    "pending",
                    LocalDate.now().format(formatter),
                    newRequiredDate
            };

            String[] poAttrs = PRqb.getAttrs(false);
            if(newPRData.length!= poAttrs.length){
                notification = new NotificationView("Invalid Purchase Request Data", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }

            boolean createRes = PRqb.target("db/PurchaseRequisition.txt").values(newPRData).create();

            // Create new PR Item
            // Extracting the id for the newly created PR
            List<String> allLines = Files.readAllLines(Path.of("src/main/java/db/PurchaseRequisition.txt"));
            String newlyCreatedPRID = allLines.isEmpty() ? "" : allLines.get(allLines.size()-1).split(",")[0];

            boolean createItemRes = true;

            for(ItemRow itemRow : tableData){
//              format: PRI35,PR1,IT5,3
                String[] newPRItem = new String[] {
                        newlyCreatedPRID,
                        itemRow.getItemID(),
                        String.valueOf(itemRow.getQuantity())
                };

                boolean res = PRIqb.target("db/PurchaseRequisitionItem.txt").values(newPRItem).create();
                if(!res){
                    createItemRes = false;
                }
            }

            if(createRes && createItemRes){
                Navigator navigator = Navigator.getInstance();
                navigator.navigate(navigator.getRouters("sales").getRoute("PRPO"));
                notification = new NotificationView("New Purchase Request Created Successfully", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notification.show();
            } else {
                notification = new NotificationView("Something went wrong. Failed to create new PR.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
            }

        } catch (IOException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void deletePR(){
        try {
            QueryBuilder<PurchaseRequisition> prQb = new QueryBuilder<>(PurchaseRequisition.class).target("db/PurchaseRequisition.txt");
            QueryBuilder<PurchaseRequisitionItem> priQb = new QueryBuilder<>(PurchaseRequisitionItem.class).target("db/PurchaseRequisitionItem.txt");

            boolean deletePRRes = prQb.delete(prData.getPrID());
            boolean deletePRItemRes = priQb.deleteAnyMatching("prRequisitionID", "=", prData.getPrID());
            if(deletePRRes && deletePRItemRes){
                Navigator navigator = Navigator.getInstance();
                FXMLLoader PRPOLoader = new FXMLLoader(getClass().getResource("/PRPO/PRPO.fxml"));
                navigator.navigate(PRPOLoader.load());

                notification = new NotificationView("PR - " +prData.getPrID() + " is deleted successfully", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notification.show();
            } else {
                notification = new NotificationView("Failed to delete PR - " +prData.getPrID(), NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void verifyPO(){
        try {
            // Create Inventory Update Log
            ArrayList<String> targetIDsList = new ArrayList<>();
            ArrayList<String[]> newDataList = new ArrayList<>();

            for(ItemRow itemRow : tableData){
                Item targetItem = itemMap.get(itemRow.getItemID());
                System.out.println("targetItem:" + targetItem.getDescription());
                int prevQuantity = targetItem.getQuantity();
                int newQuantity = prevQuantity + itemRow.getQuantity();
                String note = "Verifying Purchase Order - "+poData.getPoID();
                // Params format: logItemUpdate(String itemId, int prevQty, int newQty, String userID, String note, boolean verified)
                InventoryUpdateLog.logItemUpdate(itemRow.getItemID(), prevQuantity, newQuantity, user_id, note, true);

                targetIDsList.add(itemRow.getItemID());

                newDataList.add(new String[]{
                        targetItem.getItemName(),
                        targetItem.getDescription(),
                        stringifyDateTime(targetItem.getCreatedAt()),
                        stringifyDateTime(LocalDateTime.now()),
                        String.valueOf(targetItem.getAlertSetting()),
                        String.valueOf(newQuantity),
                        String.valueOf(targetItem.getUnitPrice()),
                        targetItem.getSupplierID()
                });
            }

            // Update item stock quantity
            String[] targetIDs = targetIDsList.toArray(new String[0]);
            QueryBuilder<Item> itemQb = new QueryBuilder<>(Item.class).target("db/Item.txt");
            itemQb.updateManyParallelArr(targetIDs, newDataList);

            // Update PO status to "Verified"
            updatePRPOStatus("verified", "Purchase Order - "+ poData.getPoID() + " Is Marked As Verified", "Failed To Verify Purchase Order - "+poData.getPoID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void applyPOChanges(){
        try {
            if(tableData.isEmpty()){
                notification = new NotificationView("Failed to save changes. The item list is empty.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }
            // Deleting all the previous PO Item's
            QueryBuilder<PurchaseOrderItem> POIqb = new QueryBuilder<>(PurchaseOrderItem.class);
            boolean deleteRes = POIqb.target("db/PurchaseOrderItem.txt").deleteAnyMatching("poID", "=", poData.getPoID());

            // Rewriting all the new PO item's
            QueryBuilder<PurchaseOrder> POqb = new QueryBuilder<>(PurchaseOrder.class);
            String[] attrs = POIqb.getAttrs(false);
            double totalPayableAmount = 0;
            boolean rewriteRes = true;

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
                    if(!res){
                        rewriteRes = false;
                    }
                }
            }

            // Round to 2 decimal. Eg: 1200.548 -> 1200.55
            totalPayableAmount = Math.ceil(totalPayableAmount * 100.0) / 100.0;

            // Update PO title
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
                notification = new NotificationView("Invalid Purchase Order Title Input. ',' is a reserved symbols.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }
            boolean updateRes = POqb.target("db/PurchaseOrder.txt").update(poData.getPoID(), newPOData);

            if(deleteRes && rewriteRes && updateRes){
                notification = new NotificationView("Changes Have Been Saved", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notification.show();
                originalTableData.setAll(tableData);
                originalPOTitle = po_title_text_field.getText();
                updateSaveChangeContainer();
            } else {
                notification = new NotificationView("Something went wrong. Failed to save changes.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void applyPRChanges(){
        try {
            if(tableData.isEmpty()){
                notification = new NotificationView("Failed to save changes. The item list is empty.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }

            // Check for valid "Required Date" input
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
            LocalDate selectedDate = datepicker.getValue();

            if(selectedDate.isBefore(LocalDate.now())){
                notification = new NotificationView("Required date cannot be earlier than today", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }

            // Deleting all the previous PR Item's
            QueryBuilder<PurchaseRequisitionItem> PRIqb = new QueryBuilder<>(PurchaseRequisitionItem.class);
            boolean deleteRes = PRIqb.target("db/PurchaseRequisitionItem.txt").deleteAnyMatching("prRequisitionID", "=", prData.getPrID());
            QueryBuilder<PurchaseRequisition> PRqb = new QueryBuilder<>(PurchaseRequisition.class);

            // Rewriting all the new PR item's
            String[] attrs = PRIqb.getAttrs(false);
            boolean rewriteRes = true;

            for(ItemRow itemRow : tableData){
                // Converts itemRow into new POItem record
                String[] newPRItem = new String[]{
                        prData.getPrID(),
                        itemRow.getItemID(),
                        String.valueOf(itemRow.getQuantity())
                };

                if (newPRItem.length != attrs.length) {
                    notification = new NotificationView("Invalid Purchase Order Item Input", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                    notification.show();
                    throw new Exception("Data length does not match attribute length");
                }else {
                    boolean res = PRIqb.target("db/PurchaseRequisitionItem.txt").values(newPRItem).create();
                    if(!res){
                        rewriteRes = false;
                    }
                }
            }

            // Update PR required date
            String newRequiredDate = selectedDate.format(formatter);

            String status = prData.getStatus();
            if(status.equals("pending") && !LocalDate.now().isBefore(selectedDate)){
                status = "late";
            } else if (status.equals("late") && LocalDate.now().isBefore(selectedDate)){
                status = "pending";
            }

            // Format of PurchaseOrderRequest: PR1,US12,pending,31/01/2025,31/12/2025
            String[] newPRData = new String[] {
                prData.getUserID(),
                status,
                prData.getCreatedDate(),
            newRequiredDate
            };

            String[] poAttrs = PRqb.getAttrs(false);
            if(newPRData.length!= poAttrs.length){
                notification = new NotificationView("Invalid Purchase Request Update Input", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }
            boolean updateRes = PRqb.target("db/PurchaseRequisition.txt").update(prData.getPrID(), newPRData);

            if(deleteRes && rewriteRes && updateRes){
                notification = new NotificationView("Changes Have Been Saved", NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                notification.show();
                originalTableData.setAll(tableData);
                originalPRDate = newRequiredDate;
                updateSaveChangeContainer();
                statusBoxController.setStatus(status);
            } else {
                notification = new NotificationView("Something went wrong. Failed to save changes.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void discardPRPOChanges(){
        // Revert back to the original table data
        tableData.setAll(originalTableData);
        if(poData != null){
            po_title_text_field.setText(originalPOTitle);
        }
        updateSaveChangeContainer();

        try {
            notification = new NotificationView("Changes discarded", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        notification.show();
    }

    public void updatePRPOStatus(String status, String afterActionSuccessMessage, String afterActionFailedMessage) throws IOException {
        if(currentAccess == AccessPermission.AccessType.APPROVE_PO){
            boolean titleIsModified = !originalPOTitle.equals(po_title_text_field.getText());
            if(isTableModified() || titleIsModified){
                notification = new NotificationView("Please save your changes before approving PO.", NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                notification.show();
                return;
            }
        }

        status = status.toLowerCase();
        if(prData != null){
            // Update PR Status
            QueryBuilder<PurchaseRequisition> PRqb;
            try {
                PRqb = new QueryBuilder<>(PurchaseRequisition.class).target("db/PurchaseRequisition.txt");
                String[] newPRData = new String[] {
                        prData.getUserID(),
                        status,
                        prData.getCreatedDate(),
                        prData.getReceivedByDate()
                };
                boolean approveRes = PRqb.update(prData.getPrID(), newPRData);
                if(approveRes){
                    prData.setStatus(status);
                    statusBoxController.setStatus(status);
                    rerenderPage();
                    notification = new NotificationView(afterActionSuccessMessage, NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                } else {
                    notification = new NotificationView(afterActionFailedMessage, NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                }
                notification.show();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Update PO Status
            QueryBuilder<PurchaseOrder> POqb;
            try {
                POqb = new QueryBuilder<>(PurchaseOrder.class).target("db/PurchaseOrder.txt");
                String[] newPOData = new String[] {
                        poData.getPrID(),
                        poData.getUserID(),
                        poData.getTitle(),
                        String.valueOf(poData.getPayableAmount()),
                        status
                };
                boolean approveRes = POqb.update(poData.getPoID(), newPOData);
                if(approveRes){
                    poData.setStatus(status);
                    statusBoxController.setStatus(status);
                    rerenderPage();
                    notification = new NotificationView(afterActionSuccessMessage, NotificationController.popUpType.success, NotificationController.popUpPos.TOP);
                } else {
                    notification = new NotificationView(afterActionFailedMessage, NotificationController.popUpType.error, NotificationController.popUpPos.TOP);
                }
                notification.show();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void rerenderPage() throws IOException {
        Navigator navigator = Navigator.getInstance();
        FXMLLoader editPRPOLoader = new FXMLLoader(getClass().getResource("/PRPO/EditPRPO.fxml"));
        navigator.navigate(editPRPOLoader.load());
        EditPRPOController editPRPOController = editPRPOLoader.getController();
        switch(user_role){
            case "1" -> {
                AccessPermission.AccessType adminAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("1").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("1").get(poData.getStatus());

                if(prData != null){
                    editPRPOController.loadPageContent(adminAccess, prData);
                } else {
                    editPRPOController.loadPageContent(adminAccess, poData);
                }
            }

            case "2" -> {
                AccessPermission.AccessType salesAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("2").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("2").get(poData.getStatus());
                if(prData != null){
                    editPRPOController.loadPageContent(salesAccess, prData);
                } else {
                    editPRPOController.loadPageContent(salesAccess, poData);
                }
            }

            case "3" -> {
                AccessPermission.AccessType purchaseAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("3").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("3").get(poData.getStatus());

                if(prData != null){
                    editPRPOController.loadPageContent(purchaseAccess, prData);
                } else {
                    editPRPOController.loadPageContent(purchaseAccess, poData);
                }
            }

            case "4" -> {
                AccessPermission.AccessType inventoryAccess = AccessPermission.POPermissionMap.get("4").get(poData.getStatus());
                if(poData != null){
                    editPRPOController.loadPageContent(inventoryAccess, poData);
                }
            }

            case "5" -> {
                AccessPermission.AccessType financeAccess = prData != null ?
                        AccessPermission.PRPermissionMap.get("5").get(prData.getStatus()) :
                        AccessPermission.POPermissionMap.get("5").get(poData.getStatus());

                if(prData != null){
                    editPRPOController.loadPageContent(financeAccess, prData);
                } else {
                    editPRPOController.loadPageContent(financeAccess, poData);
                }
            }
        }
    }

}

