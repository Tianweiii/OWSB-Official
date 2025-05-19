package controllers.salesController;

import controllers.SidebarController;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Duration;
import models.Datas.Transaction;
import org.start.owsb.Layout;
import service.DailySalesService;
import controllers.NotificationController;
import views.NotificationView;
import views.salesViews.AddNewDailyItemSalesView;
import views.salesViews.DeleteDailyItemSalesView;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class DailyItemSalesController implements Initializable {

	private static final int ROWS_PER_PAGE = 50;

	@FXML private DatePicker datePicker;
	@FXML private TextField searchField;
	@FXML private ProgressIndicator loadingIndicator;
	@FXML private ToggleButton themeToggle;
	@FXML private Button exportBtn;
	@FXML private Button addBtn;
	@FXML private Button editBtn;
	@FXML private Button delBtn;
	@FXML private Button deleteAllButton;
	@FXML private Label totalLabel;
	@FXML private StackPane contentStack;
	@FXML private BorderPane rootPane;
	@FXML private TableView<Transaction> table;

	private Pagination pagination;
	private final DailySalesService service = new DailySalesService();
	private final Preferences prefs = Preferences.userNodeForPackage(DailyItemSalesController.class);
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private Pane overlayBackground;
	private Pane deleteDialogPane;

	@Override
	public void initialize(URL loc, ResourceBundle res) {
		contentStack.sceneProperty().addListener((o, old, newScene) -> {
			if (newScene != null) restoreTheme();
		});

		setupControls();
		pagination = new Pagination(1, 0);
		pagination.setPageFactory(this::createPage);
		refreshData();

	}

	private void restoreTheme() {
		boolean dark = prefs.getBoolean("darkMode", false);
		Platform.runLater(() -> {
			var styles = contentStack.getScene().getRoot().getStyleClass();
			styles.remove("dark-mode");
			if (dark) {
				styles.add("dark-mode");
				themeToggle.setText("☀️");
			} else {
				themeToggle.setText("🌙");
			}
		});
	}

	private void setupControls() {
		datePicker.setValue(LocalDate.now());
		datePicker.setOnAction(e -> refreshData());

		searchField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) debounceSearch();
		});

		themeToggle.setOnAction(e -> toggleTheme());
		exportBtn.setOnAction(e -> exportCsv());
		addBtn.setOnAction(e -> openDialog(null));
		editBtn.setOnAction(e -> openDialog(table.getSelectionModel().getSelectedItem()));
		delBtn.setOnAction(e -> showDeleteDialog(false));
		deleteAllButton.setOnAction(e -> showDeleteDialog(true));

		editBtn.setDisable(true);
		delBtn.setDisable(true);
	}

	private void refreshData() {
		loadingIndicator.setVisible(true);
		Task<List<Transaction>> task = new Task<>() {
			@Override protected List<Transaction> call() {
				LocalDate d = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
				return service.getTransactionsFor(d);
			}
		};
		task.setOnSucceeded(e -> {
			loadingIndicator.setVisible(false);
			displayData(task.getValue());
		});
		task.setOnFailed(e -> {
			loadingIndicator.setVisible(false);
			notifyUser("Failed to load data", NotificationController.popUpType.error);
		});
		new Thread(task).start();
	}

	private void displayData(List<Transaction> transactions) {
		if (table == null) buildTable();

		if (transactions.isEmpty()) {
			loadEmptyView();
			totalLabel.setText("$0.00");
			totalLabel.setTooltip(null);
			addBtn.setVisible(false);
			editBtn.setVisible(false);
			delBtn.setVisible(false);
			deleteAllButton.setVisible(false);
			return;
		}

		addBtn.setVisible(true);
		editBtn.setVisible(true);
		delBtn.setVisible(true);
		deleteAllButton.setVisible(true);

		int totalPages = (transactions.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
		pagination.setPageCount(Math.max(totalPages, 1));
		pagination.setUserData(transactions);

		if (transactions.size() > ROWS_PER_PAGE) {
			contentStack.getChildren().setAll(pagination);
		} else {
			table.setItems(FXCollections.observableList(transactions));
			contentStack.getChildren().setAll(table);
		}

		double total = service.calculateTotalFor(datePicker.getValue());
		totalLabel.setText(String.format("$%.2f", total));
		totalLabel.setTooltip(new Tooltip("Total sales for " + dateFormatter.format(datePicker.getValue())));
	}

	private Node createPage(int pageIndex) {
		@SuppressWarnings("unchecked")
		List<Transaction> transactions = (List<Transaction>) pagination.getUserData();
		int from = pageIndex * ROWS_PER_PAGE;
		int to = Math.min(from + ROWS_PER_PAGE, transactions.size());
		table.setItems(FXCollections.observableList(transactions.subList(from, to)));
		return table;
	}

	private void buildTable() {
		table = new TableView<>();
		table.setMaxWidth(Double.MAX_VALUE);

		TableColumn<Transaction, String> itemCol = createCenteredColumn("Item", "itemName");
		TableColumn<Transaction, Integer> qtyCol = createCenteredColumn("Quantity", "soldQuantity");
		TableColumn<Transaction, Double> subtotalCol = createCenteredColumn("Subtotal", "subtotal");

		table.getColumns().addAll(itemCol, qtyCol, subtotalCol);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			boolean isSelected = newSelection != null;
			editBtn.setDisable(!isSelected);
			delBtn.setDisable(!isSelected);
		});

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private <T> TableColumn<Transaction, T> createCenteredColumn(String title, String propertyName) {
		TableColumn<Transaction, T> col = new TableColumn<>(title);
		col.setCellValueFactory(new PropertyValueFactory<>(propertyName));
		col.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
		col.setCellFactory(new Callback<>() {
			@Override
			public TableCell<Transaction, T> call(TableColumn<Transaction, T> param) {
				return new TableCell<>() {
					@Override
					protected void updateItem(T item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(null);
						} else {
							setText(item.toString());
							setAlignment(Pos.CENTER);
							setWrapText(true);
							setTextAlignment(TextAlignment.CENTER);
						}
					}
				};
			}
		});
		col.setPrefWidth(150);
		return col;
	}

	private void loadEmptyView() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalesManager/EmptyDailySales.fxml"));
			Node emptyNode = loader.load();

			contentStack.getChildren().setAll(emptyNode);

			MissingDailySalesController missingCtrl = loader.getController();
			missingCtrl.setOnCreateCallback(this::reload);
			missingCtrl.getCreateButton().setOnAction(e -> missingCtrl.onCreateNewSalesEntryButtonClick());
		} catch (Exception e) {
			e.printStackTrace();
			notifyUser("Failed to load empty view", NotificationController.popUpType.error);
		}
	}


	private void debounceSearch() {
		if (table == null) return;

		String searchTerm = searchField.getText().trim().toLowerCase();
		table.getItems().removeIf(tx -> !tx.getItemName().toLowerCase().contains(searchTerm));
	}

	private void openDialog(Transaction transactionToEdit) {
		try {
			AddNewDailyItemSalesView view = new AddNewDailyItemSalesView(this,
					transactionToEdit == null ?
							AddNewDailyItemSalesView.Mode.NEW :
							AddNewDailyItemSalesView.Mode.UPDATE);
			view.getAddNewDailyItemSalesController().setCurrentTx(transactionToEdit);

			view.show();

		} catch (Exception e) {
			e.printStackTrace();
			notifyUser("Failed to open entry form", NotificationController.popUpType.error);
		}
	}

	/**
	 * Shows a modal delete dialog popup overlay to confirm deletion of the selected transaction.
	 */
	private void showDeleteDialog(boolean deleteAll) {
		Transaction sel;
		ObservableList<Transaction> allItems = FXCollections.observableArrayList();

		if (deleteAll) {
            sel = null;
            allItems.addAll(table.getItems());
		} else {
			sel = table.getSelectionModel().getSelectedItem();
		}

		try {
			DeleteDailyItemSalesView view = new DeleteDailyItemSalesView(this);
			this.deleteDialogPane = view.getDeleteDialogPane();
			view.show();

			DeleteDailyItemSalesController dctrl = view.getDeleteDailyItemSalesController();

			if (deleteAll) {
				String histID = allItems.get(0).getDailySalesHistoryID();
				String[] transactionIDs = new String[allItems.size()];
				for (int i = 0; i < allItems.size(); i++) {
					transactionIDs[i] = allItems.get(i).getTransactionID();
				}
				dctrl.setDeleteLabel("Delete all transactions?");
				dctrl.setOnDelete(() -> {
					service.deleteAllTransactions(histID, transactionIDs);
					notifyUser("Deleted successfully", NotificationController.popUpType.success);
					hideDeleteDialog();
					reload();
				});
			} else {
				dctrl.setOnDelete(() -> {
					service.deleteTransaction(sel.getTransactionID());
					notifyUser("Deleted successfully", NotificationController.popUpType.success);
					hideDeleteDialog();
					reload();
				});

				dctrl.setDeleteLabel(sel.getItemName());

			}
			dctrl.setOnCancel(this::hideDeleteDialog);

			playFadeInAnimation(deleteDialogPane);

		} catch (Exception ex) {
			ex.printStackTrace();
			notifyUser("Failed to show delete dialog", NotificationController.popUpType.error);
		}
	}

	/**
	 * Hides and cleans up the delete dialog and overlay.
	 */
	private void hideDeleteDialog() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.deleteDialogPane);

		this.rootPane.setDisable(false);
		SidebarController.getSidebar().setDisable(false);

	}

	private void playFadeInAnimation(Node node) {
		FadeTransition fadeIn = new FadeTransition(Duration.millis(280), node);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.play();
	}

	private void playFadeOutAnimation(Node node, Runnable onFinish) {
		FadeTransition fadeOut = new FadeTransition(Duration.millis(200), node);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		fadeOut.setOnFinished(e -> onFinish.run());
		fadeOut.play();
	}

	private void exportCsv() {
		try {
			String csvPath = service.exportCsv(datePicker.getValue());
			notifyUser("CSV exported: " + csvPath, NotificationController.popUpType.success);
			Desktop.getDesktop().open(new File(csvPath).getParentFile());
		} catch (Exception e) {
			notifyUser("Failed to export CSV", NotificationController.popUpType.error);
		}
	}

	private void toggleTheme() {
		var root = contentStack.getScene().getRoot();
		List<String> styles = root.getStyleClass();

		boolean switchingToDark = !styles.contains("dark-mode");
		if (switchingToDark) {
			styles.add("dark-mode");
			themeToggle.setText("☀️");
		} else {
			styles.remove("dark-mode");
			themeToggle.setText("🌙");
		}

		prefs.putBoolean("darkMode", switchingToDark);
	}

	private void notifyUser(String message, NotificationController.popUpType type) {
		try {
			new NotificationView(message, type, NotificationController.popUpPos.BOTTOM_RIGHT).show();
		} catch (Exception ignored) {
		}
	}

	public void reload() {
		contentStack.getChildren().clear();
		refreshData();
	}

	public BorderPane getRootPane() {
		return this.rootPane;
	}
}
