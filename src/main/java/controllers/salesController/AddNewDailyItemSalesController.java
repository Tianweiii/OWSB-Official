package controllers.salesController;

import controllers.SidebarController;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import models.Datas.Item;
import models.Datas.Transaction;
import models.Utils.Navigator;
import org.start.owsb.Layout;
import service.DailySalesService;
import controllers.NotificationController;
import views.NotificationView;
import views.salesViews.AddNewDailyItemSalesView;
import models.Utils.Validation;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddNewDailyItemSalesController implements Initializable {

	@FXML
	public Pane root;
	@FXML private VBox container;
	@FXML private Label titleLabel;
	@FXML private ComboBox<Item> itemCombo;
	@FXML private Button decBtn, incBtn;
	@FXML private TextField qtyField;
	@FXML private Button submitBtn, cancelBtn;

	private final DailySalesService service = new DailySalesService();
	private final ObservableList<Item> masterItems = FXCollections.observableArrayList();
	private Transaction currentTx;
	private LocalDate selectedDate;
	private AddNewDailyItemSalesView.Mode mode;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configureComboBox();
		loadItems();
		configureQuantityControls();
		bindValidation();
		configureKeyboardShortcuts();
		configureCancel();
		playSlideIn();
	}

	private void loadItems() {
		try {
			masterItems.setAll(service.getAllItems());
		} catch (Exception e) {
			masterItems.clear();
			notify("Failed to load items", NotificationController.popUpType.error);
		}
		itemCombo.getItems().setAll(masterItems);
	}

	private void configureComboBox() {
		itemCombo.setConverter(new StringConverter<>() {
			@Override public String toString(Item item) {
				return (item == null) ? "" : item.getItemName();
			}
			@Override public Item fromString(String string) {
				return masterItems.stream()
						.filter(i -> i.getItemName().equalsIgnoreCase(string))
						.findFirst()
						.orElse(null);
			}
		});

		// Filter items on ENTER key pressed
		itemCombo.getEditor().setOnKeyPressed(ev -> {
			if (ev.getCode() == KeyCode.ENTER) {
				String filterText = itemCombo.getEditor().getText().toLowerCase().trim();
				ObservableList<Item> filtered = masterItems.filtered(
						i -> i.getItemName().toLowerCase().contains(filterText));
				itemCombo.setItems(filtered);
				itemCombo.show();
			}
		});

		itemCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				itemCombo.setStyle(""); // Clear any error styling
			}
		});
	}

	// Enhanced quantity field setup with additional validation
	private void configureQuantityControls() {
		decBtn.setOnAction(e -> adjustQuantity(-1));
		incBtn.setOnAction(e -> adjustQuantity(+1));

		// Set initial value to 1 if empty
		if (qtyField.getText() == null || qtyField.getText().trim().isEmpty()) {
			qtyField.setText("1");
		}

		// Add input restriction to only allow numbers
		qtyField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*")) {
				qtyField.setText(newValue.replaceAll("[^\\d]", ""));
			}

			// Prevent leading zeros (except for single 0)
			if (newValue != null && newValue.length() > 1 && newValue.startsWith("0")) {
				qtyField.setText(newValue.replaceFirst("^0+", ""));
			}
		});
	}

	private void adjustQuantity(int delta) {
		int currentQty = parseQuantity();
		int newQty = Math.max(1, Math.min(currentQty + delta, 9999)); // Keep within 1-9999 range
		qtyField.setText(String.valueOf(newQty));
	}

	private int parseQuantity() {
		try {
			String text = qtyField.getText().trim();
			if (text.isEmpty()) return 0;

			int quantity = Integer.parseInt(text);
			// Ensure quantity is within valid range
			return Math.max(0, Math.min(quantity, 9999));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void bindValidation() {
		qtyField.textProperty().addListener((observable, oldValue, newValue) -> {
			// Allow empty field while typing
			if (newValue == null || newValue.trim().isEmpty()) {
				qtyField.setStyle("");
				submitBtn.setDisable(true);
				return;
			}

			// Check for valid sales quantity (1-9999)
			if (!Validation.isValidSalesQuantity(newValue)) {
				qtyField.setStyle("-fx-border-color: red;");
				submitBtn.setDisable(true);

				// Show specific error message based on the issue
				if (!Validation.isValidQuantity(newValue)) {
					// Don't show notification on every keystroke, just set style
				} else {
					// Don't show notification on every keystroke, just set style
				}
			} else {
				qtyField.setStyle("");
				validateForm();
			}
		});

		// Item combo box validation
		itemCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			validateForm();
		});

		// Focus lost validation for quantity field
		qtyField.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) { // Focus lost
				String text = qtyField.getText();
				if (text != null && !text.trim().isEmpty()) {
					if (!Validation.isValidSalesQuantity(text)) {
						qtyField.setStyle("-fx-border-color: red;");
						notify("Please enter a valid quantity (1-9999)", NotificationController.popUpType.error);
					} else if (text.trim().equals("0")) {
						// Auto-correct 0 to 1
						qtyField.setText("1");
						notify("Quantity cannot be zero, changed to 1", NotificationController.popUpType.info);
					}
				} else {
					// Set default value if empty
					qtyField.setText("1");
				}
			}
		});

		// Combo box focus validation
		itemCombo.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue && itemCombo.getValue() == null) { // Focus lost and no selection
				itemCombo.setStyle("-fx-border-color: orange;");
			}
		});

		submitBtn.setOnAction(e -> onSubmit());
	}

	private void validateForm() {
		boolean isQuantityValid = Validation.isNotEmpty(qtyField.getText()) &&
				Validation.isValidSalesQuantity(qtyField.getText());
		boolean isItemSelected = itemCombo.getValue() != null;

		boolean isValid = isQuantityValid && isItemSelected;
		submitBtn.setDisable(!isValid);

		// Visual feedback for combo box
		if (!isItemSelected && itemCombo.isFocused()) {
			itemCombo.setStyle("-fx-border-color: orange;");
		} else if (isItemSelected) {
			itemCombo.setStyle("");
		}
	}

	private void onSubmit() {
		Item selectedItem = itemCombo.getValue();
		int quantity = parseQuantity();

		// Final validation before submission
		if (selectedItem == null) {
			notify("Please select an item", NotificationController.popUpType.error);
			itemCombo.setStyle("-fx-border-color: red;");
			return;
		}

		if (quantity <= 0 || quantity > 9999) {
			notify("Please enter a valid quantity (1-9999)", NotificationController.popUpType.error);
			qtyField.setStyle("-fx-border-color: red;");
			return;
		}

		try {
			// Handle different modes with proper null checks
			if (mode == AddNewDailyItemSalesView.Mode.FIRST) {
				// FIRST mode: Creating first entry from missing sales view
				// No table access needed - just record sale and navigate
				service.recordSale(selectedItem, quantity, selectedDate);
				notify("Sale entry added successfully", NotificationController.popUpType.success);
				DailyItemSalesController.setSelectedDate(selectedDate);
				Navigator.getInstance().navigate(Navigator.getInstance().getRouters("sales").getRoute("daily-sales"));
			} else if (mode == AddNewDailyItemSalesView.Mode.NEW) {
				// NEW mode: Adding to existing daily sales
				// Check if rootController is available
				if (AddNewDailyItemSalesView.getRootController() == null) {
					notify("Unable to access daily sales table", NotificationController.popUpType.error);
					return;
				}

				TableView<Transaction> table = AddNewDailyItemSalesView.getRootController().getTable();
				ObservableList<Transaction> txs = table.getItems();

				// Check if item already exists in today's sales
				Transaction existingTransaction = txs.stream()
						.filter(t -> t.getItemID().equals(selectedItem.getItemID()))
						.findFirst()
						.orElse(null);

				if (existingTransaction != null) {
					// Update existing transaction
					service.updateTransaction(existingTransaction, selectedItem,
							existingTransaction.getSoldQuantity() + quantity);
					notify("Sale entry updated successfully (added " + quantity + " to existing)", NotificationController.popUpType.success);
				} else {
					// Create new transaction
					service.recordSale(selectedItem, quantity, selectedDate);
					notify("Sale entry added successfully", NotificationController.popUpType.success);
				}

				AddNewDailyItemSalesView.getRootController().reload();
			} else if (mode == AddNewDailyItemSalesView.Mode.UPDATE) {
				// UPDATE mode: Editing existing transaction
				if (currentTx == null) {
					notify("No transaction to update", NotificationController.popUpType.error);
					return;
				}

				service.updateTransaction(currentTx, selectedItem, quantity);
				notify("Sale entry updated successfully", NotificationController.popUpType.success);

				// Reload the appropriate controller if available
				if (AddNewDailyItemSalesView.getRootController() != null) {
					AddNewDailyItemSalesView.getRootController().reload();
				}
			}

			close();
		} catch (Exception ex) {
			notify("Failed to save sale entry: " + ex.getMessage(), NotificationController.popUpType.error);
			System.out.println("Error in onSubmit: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void configureKeyboardShortcuts() {
		root.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE) {
				close();
			} else if (e.getCode() == KeyCode.ENTER && !submitBtn.isDisabled()) {
				onSubmit();
			} else if (e.getCode() == KeyCode.F1) {
				// Focus on item combo for quick item search
				itemCombo.requestFocus();
			} else if (e.getCode() == KeyCode.F2) {
				// Focus on quantity field
				qtyField.requestFocus();
				qtyField.selectAll();
			}
		});
	}

	private void configureCancel() {
		cancelBtn.setOnAction(e -> close());
	}

	private void playSlideIn() {
		TranslateTransition tt = new TranslateTransition(Duration.millis(300), container);
		tt.setFromY(400);
		tt.setToY(0);
		tt.play();
	}

	private void closeSlideOut(Runnable onFinish) {
		TranslateTransition tt = new TranslateTransition(Duration.millis(200), container);
		tt.setFromY(0);
		tt.setToY(400);
		tt.setOnFinished(e -> onFinish.run());
		tt.play();
	}

	private void close() {
		closeSlideOut(() -> {
			Layout layout = Layout.getInstance();
			layout.getRoot().getChildren().remove(this.root);

			// Re-enable appropriate controllers with null checks
			if (AddNewDailyItemSalesView.getRootController() != null) {
				AddNewDailyItemSalesView.getRootController().getRootPane().setDisable(false);
				AddNewDailyItemSalesView.getRootController().reload();
			}

			if (AddNewDailyItemSalesView.getMissingController() != null) {
				AddNewDailyItemSalesView.getMissingController().getRootPane().setDisable(false);
			}

			// Re-enable sidebar
			SidebarController.getSidebar().setDisable(false);
		});
	}

	private void notify(String msg, NotificationController.popUpType type) {
		try {
			new NotificationView(msg, type, NotificationController.popUpPos.BOTTOM_RIGHT).show();
		} catch (Exception ignore) {
			System.err.println("Failed to show notification: " + msg);
		}
	}

	public void initMode(AddNewDailyItemSalesView.Mode mode, Transaction tx, LocalDate date) {
		this.mode = mode;
		this.currentTx = tx;
		this.selectedDate = date;

		// Update UI based on mode
		switch (mode) {
			case FIRST:
			case NEW:
				titleLabel.setText("Add New Sale Entry");
				submitBtn.setText("Submit");
				break;
			case UPDATE:
				titleLabel.setText("Update Sale Entry");
				submitBtn.setText("Update");
				break;
		}

		// Populate fields if updating existing transaction
		if (tx != null) {
			masterItems.stream()
					.filter(i -> i.getItemID().equals(tx.getItemID()))
					.findFirst()
					.ifPresent(item -> {
						itemCombo.setValue(item);
						itemCombo.setStyle("");
					});

			qtyField.setText(String.valueOf(tx.getSoldQuantity()));
			qtyField.setStyle("");
		} else {
			if (qtyField.getText() == null || qtyField.getText().trim().isEmpty()) {
				qtyField.setText("1");
			}
		}

		validateForm();
	}

	public AddNewDailyItemSalesView.Mode getMode() {
		return this.mode;
	}

	public LocalDate getSelectedDate() {
		return this.selectedDate;
	}

	public boolean isFormValid() {
		return itemCombo.getValue() != null &&
				Validation.isValidSalesQuantity(qtyField.getText());
	}
}