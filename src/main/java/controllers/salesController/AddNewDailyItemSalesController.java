package controllers.salesController;

import controllers.SidebarController;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
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
	}

	private void configureQuantityControls() {
		decBtn.setOnAction(e -> adjustQuantity(-1));
		incBtn.setOnAction(e -> adjustQuantity(+1));
	}

	private void adjustQuantity(int delta) {
		int currentQty = parseQuantity();
		int newQty = Math.max(0, currentQty + delta);
		qtyField.setText(String.valueOf(newQty));
	}

	private int parseQuantity() {
		try {
			return Integer.parseInt(qtyField.getText().trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void bindValidation() {
		submitBtn.disableProperty().bind(
				Bindings.createBooleanBinding(
						() -> itemCombo.getValue() == null || parseQuantity() <= 0,
						itemCombo.valueProperty(),
						qtyField.textProperty()
				)
		);

		submitBtn.setOnAction(e -> onSubmit());
	}

	private void onSubmit() {
		Item selectedItem = itemCombo.getValue();
		int quantity = parseQuantity();

		try {
			if (mode == AddNewDailyItemSalesView.Mode.FIRST) {
				service.recordSale(selectedItem, quantity, selectedDate);
				notify("Sale entry added successfully", NotificationController.popUpType.success);
				Navigator.getInstance().navigate(Navigator.getInstance().getRouters("sales").getRoute("daily-sales"));
			} else if (mode == AddNewDailyItemSalesView.Mode.NEW) {
				service.recordSale(selectedItem, quantity, selectedDate);
				notify("Sale entry added successfully", NotificationController.popUpType.success);
				AddNewDailyItemSalesView.getRootController().reload();
			} else if (mode == AddNewDailyItemSalesView.Mode.UPDATE) {
				service.updateTransaction(currentTx, selectedItem, quantity);
				notify("Sale entry updated successfully", NotificationController.popUpType.success);
			}
			close();
		} catch (Exception ex) {
			notify("Failed to save sale entry", NotificationController.popUpType.error);
			System.out.println(ex.getMessage());
		}
	}

	private void configureKeyboardShortcuts() {
		root.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE) close();
			else if (e.getCode() == KeyCode.ENTER && !submitBtn.isDisabled()) onSubmit();
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

			if (AddNewDailyItemSalesView.getRootController() != null) {
				AddNewDailyItemSalesView.getRootController().getRootPane().setDisable(false);
				SidebarController.getSidebar().setDisable(false);

				AddNewDailyItemSalesView.getRootController().reload();
			}

			if (AddNewDailyItemSalesView.getMissingController() != null) {
				AddNewDailyItemSalesView.getMissingController().getRootPane().setDisable(false);
				SidebarController.getSidebar().setDisable(false);

			}
		});
	}

	private void notify(String msg, NotificationController.popUpType type) {
		try {
			new NotificationView(msg, type, NotificationController.popUpPos.BOTTOM_RIGHT).show();
		} catch (Exception ignore) {}
	}

	/** Called externally to initialize dialog mode and data */
	public void initMode(AddNewDailyItemSalesView.Mode mode, Transaction tx, LocalDate date) {
		this.mode = mode;
		this.currentTx = tx;
		this.selectedDate = date;

		titleLabel.setText(mode == AddNewDailyItemSalesView.Mode.NEW | mode == AddNewDailyItemSalesView.Mode.FIRST ? "Add New Sale Entry" : "Update Sale Entry");
		submitBtn.setText(mode == AddNewDailyItemSalesView.Mode.NEW | mode == AddNewDailyItemSalesView.Mode.FIRST ? "Submit" : "Update");

		if (tx != null) {
			masterItems.stream()
					.filter(i -> i.getItemID().equals(tx.getItemID()))
					.findFirst()
					.ifPresent(itemCombo::setValue);

			qtyField.setText(String.valueOf(tx.getSoldQuantity()));
		}
	}

	public void setCurrentTx(Transaction tx) {
		this.currentTx = tx;
	}
}
