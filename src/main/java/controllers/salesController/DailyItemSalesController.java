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
import models.Datas.Supplier;
import models.Datas.Transaction;
import org.start.owsb.Layout;
import service.DailySalesService;
import controllers.NotificationController;
import views.NotificationView;
import views.salesViews.AddNewDailyItemSalesView;
import views.salesViews.CompleteSalesReportView;
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
	public DailyItemSalesController controller = this;
	@FXML private DatePicker datePicker;
	@FXML private TextField txtSearch;
	@FXML private Button clearSearchButton, completeSalesReportBtn, addBtn,editBtn,delBtn,deleteAllButton;
	@FXML private Label totalLabel,subHeadingLabel;
	@FXML private StackPane contentStack;
	@FXML private BorderPane rootPane;
	@FXML private TableView<Transaction> table;
	@FXML private Label totalDailyItemSalesLabel;
	@FXML private ProgressIndicator loadingIndicator;

	private static LocalDate selectedDate;
	private Pagination pagination;
	private final DailySalesService service = new DailySalesService();
	private final Preferences prefs = Preferences.userNodeForPackage(DailyItemSalesController.class);
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private Pane overlayBackground;
	private Pane deleteDialogPane;
	private Pane completeSalesReportPane;
	private ObservableList<Supplier> masterList;

	@Override
	public void initialize(URL loc, ResourceBundle res) {
		contentStack.sceneProperty().addListener((o, old, newScene) -> {
			if (newScene != null) setupControls();
		});

		setupControls();
		setupSearchControls();
		pagination = new Pagination(1, 0);
		pagination.setPageFactory(this::createPage);
		refreshData();
	}

	private void setupControls() {
		datePicker.setValue(selectedDate != null ? selectedDate : LocalDate.now());
		datePicker.setOnAction(e -> refreshData());

		completeSalesReportBtn.setOnAction(e -> showCompleteSalesReportDialog());
		addBtn.setOnAction(e -> openDialog(null));
		editBtn.setOnAction(e -> openDialog(table.getSelectionModel().getSelectedItem()));
		delBtn.setOnAction(e -> showDeleteDialog(false));
		deleteAllButton.setOnAction(e -> showDeleteDialog(true));

		editBtn.setDisable(true);
		delBtn.setDisable(true);
	}

	public static void setSelectedDate(LocalDate date) {
		DailyItemSalesController.selectedDate = date;
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
			updateSalesStatus();
		});
		task.setOnFailed(e -> {
			loadingIndicator.setVisible(false);
			notifyUser("Failed to load data", NotificationController.popUpType.error);
		});
		new Thread(task).start();
	}

	private void updateSalesStatus() {
		LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
		String status = service.getSalesHistoryStatus(date);
		
		if (status != null && status.equals("Completed")) {
			completeSalesReportBtn.setVisible(true);
			completeSalesReportBtn.setDisable(true);
			addBtn.setVisible(false);
			deleteAllButton.setVisible(false);
			delBtn.setVisible(false);
			editBtn.setVisible(false);
			completeSalesReportBtn.setText("✓ Sales Report Completed");
			completeSalesReportBtn.setTooltip(new Tooltip("Sales report has been completed for this date"));
		} else if (status != null && status.equals("Pending")) {
			completeSalesReportBtn.setVisible(true);
			completeSalesReportBtn.setDisable(false);
			completeSalesReportBtn.setText("Complete Sales Report");
			completeSalesReportBtn.setTooltip(new Tooltip("Generate and finalize sales report for this date"));
		} else{
			completeSalesReportBtn.setVisible(false);
		}
	}

	private void displayData(List<Transaction> transactions) {
		if (table == null) buildTable();

		if (transactions.isEmpty()) {
			loadEmptyView();
			totalLabel.setText("$0.00");
			totalLabel.setTooltip(null);
			subHeadingLabel.setText("0 Items, 0 Sales");
			addBtn.setVisible(false);
			editBtn.setVisible(false);
			delBtn.setVisible(false);
			deleteAllButton.setVisible(false);
			completeSalesReportBtn.setDisable(true);
			return;
		}

		// Count unique items
		long uniqueItems = transactions.stream()
			.map(Transaction::getItemID)
			.distinct()
			.count();

		subHeadingLabel.setText(uniqueItems + " Items, " + transactions.size() + " Sales");

		addBtn.setVisible(true);
		editBtn.setVisible(true);
		delBtn.setVisible(true);
		deleteAllButton.setVisible(true);
		completeSalesReportBtn.setDisable(false);

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
		table.getStyleClass().add("sales-table");

		TableColumn<Transaction, String> itemCol = createCenteredColumn("Item", "itemName");
		TableColumn<Transaction, Integer> qtyCol = createCenteredColumn("Quantity", "soldQuantity");
		TableColumn<Transaction, Double> priceCol = createCenteredColumn("Unit Price", "markedUpPrice");
		TableColumn<Transaction, Double> subtotalCol = createCenteredColumn("Subtotal", "markedUpSubtotal");
		TableColumn<Transaction, String> statusCol = createStatusColumn();

		table.getColumns().addAll(itemCol, qtyCol, priceCol, subtotalCol, statusCol);

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
							// Special handling for unit price - show original and markup
							if (propertyName.equals("markedUpPrice") && item instanceof Double price) {
                                setText(String.format("$%.2f\n(+15%%)", price));
								setTooltip(new Tooltip(String.format("Original: $%.2f, Markup: 15%%", price/1.15)));

							} else {
								setText(item.toString());
							}
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

	private TableColumn<Transaction, String> createStatusColumn() {
		TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
		statusCol.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
				} else {
					Transaction transaction = getTableView().getItems().get(getIndex());
					LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
					String historyStatus = service.getSalesHistoryStatus(date);
					
					Label statusLabel = new Label();
					statusLabel.setMaxWidth(Double.MAX_VALUE);
					statusLabel.setAlignment(Pos.CENTER);
					statusLabel.setPrefHeight(24);
					statusLabel.getStyleClass().add("status-label");
					
					if (historyStatus != null && historyStatus.equals("Completed")) {
						statusLabel.setText("Completed");
						statusLabel.getStyleClass().add("status-completed");
						statusLabel.setTooltip(new Tooltip("This item has been processed by inventory"));
					} else {
						statusLabel.setText("Pending");
						statusLabel.getStyleClass().add("status-pending");
						statusLabel.setTooltip(new Tooltip("Waiting for inventory processing"));
					}
					
					setGraphic(statusLabel);
					setAlignment(Pos.CENTER);
				}
			}
		});
		statusCol.setPrefWidth(120);
		return statusCol;
	}

	private void loadEmptyView() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalesManager/EmptyDailySales.fxml"));
			Node emptyNode = loader.load();

			contentStack.getChildren().setAll(emptyNode);

			MissingDailySalesController missingCtrl = loader.getController();
			missingCtrl.setSalesDate(datePicker.getValue());
			missingCtrl.setOnCreateCallback(this::reload);
			missingCtrl.getCreateButton().setOnAction(e -> missingCtrl.onCreateNewSalesEntryButtonClick());
		} catch (Exception e) {
			e.printStackTrace();
			notifyUser("Failed to load empty view", NotificationController.popUpType.error);
		}
	}

	private void openDialog(Transaction transactionToEdit) {
		try {
			AddNewDailyItemSalesView.Mode mode = transactionToEdit == null ? AddNewDailyItemSalesView.Mode.NEW : AddNewDailyItemSalesView.Mode.UPDATE;
			LocalDate selectedDate = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
			AddNewDailyItemSalesView view = new AddNewDailyItemSalesView(this, mode);
			view.getAddNewDailyItemSalesController().initMode(mode, transactionToEdit, selectedDate);
			view.show();
		} catch (Exception e) {
			e.printStackTrace();
			notifyUser("Failed to open entry form", NotificationController.popUpType.error);
		}
	}

	@FXML private void onSearch() {
		if (table == null || txtSearch == null) return;
		
		String searchText = txtSearch.getText().toLowerCase().trim();
		ObservableList<Transaction> allItems = table.getItems();
		
		if (searchText.isEmpty()) {
			refreshData();
			return;
		}
		
		ObservableList<Transaction> filteredItems = allItems.filtered(transaction -> {
			if (searchText.matches("\\d+")) {
				// Search by quantity
				return transaction.getSoldQuantity() == Integer.parseInt(searchText);
			}
			
			// Search by item name or other text fields
			return transaction.getItemName().toLowerCase().contains(searchText) ||
				   transaction.getItemID().toLowerCase().contains(searchText);
		});
		
		table.setItems(filteredItems);
		updateTotalLabel(filteredItems);
	}
	
	private void updateTotalLabel(ObservableList<Transaction> items) {
		double total = items.stream()
			.mapToDouble(tx -> tx.getSoldQuantity() * tx.getUnitPrice())
			.sum();
		totalLabel.setText(String.format("$%.2f", total));
	}

	@FXML private void onClear() {
		if (txtSearch != null) {
			txtSearch.clear();
			refreshData();
		}
	}

	private void setupSearchControls() {
		if (txtSearch != null) {
			txtSearch.setPromptText("Search by item name or quantity...");
			txtSearch.textProperty().addListener((obs, old, newVal) -> {
				if (newVal.isEmpty()) {
					refreshData();
				}
			});
		}
		
		if (clearSearchButton != null) {
			clearSearchButton.setOnAction(e -> onClear());
		}
	}

	private void updateSupplierCount(int count) {
		if (totalDailyItemSalesLabel != null) {
			Platform.runLater(() -> totalDailyItemSalesLabel.setText(String.format("Total Daily Item Sales: %d", count)));
		}
	}

	/**
	 * Shows the Complete Sales Report dialog to confirm report generation
	 */
	private void showCompleteSalesReportDialog() {
		LocalDate selectedDate = datePicker.getValue();
		if (selectedDate == null) {
			notifyUser("Please select a date first", NotificationController.popUpType.warning);
			return;
		}
		
		List<Transaction> transactions = service.getTransactionsFor(selectedDate);
		if (transactions.isEmpty()) {
			notifyUser("No sales data available for selected date", NotificationController.popUpType.warning);
			return;
		}
		
		try {
			CompleteSalesReportView view = new CompleteSalesReportView(this);
			view.setSelectedDate(selectedDate);
			this.completeSalesReportPane = view.getCompleteSalesReportPane();
			view.show();
			
			CompleteSalesReportController controller = view.getCompleteSalesReportController();
			
			controller.setOnConfirm(() -> {
				// Generate the sales report
				String reportPath = service.completeSalesReport(selectedDate);
				if (reportPath != null) {
					notifyUser("Sales report completed: " + reportPath, NotificationController.popUpType.success);

					try {
						Desktop.getDesktop().open(new File(reportPath).getParentFile());
					} catch (Exception ex) {
						// Silently ignore if can't open
					}
					updateSalesStatus();
				} else {
					notifyUser("Failed to generate sales report", NotificationController.popUpType.error);
				}
				hideCompleteSalesReportDialog();
				reload();
			});
			
			controller.setOnCancel(() -> {
				hideCompleteSalesReportDialog();
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			notifyUser("Failed to show complete sales report dialog", NotificationController.popUpType.error);
		}
	}


	private void hideCompleteSalesReportDialog() {
		Layout layout = Layout.getInstance();
		BorderPane root = layout.getRoot();
		root.getChildren().remove(this.completeSalesReportPane);
		
		this.rootPane.setDisable(false);
		SidebarController.getSidebar().setDisable(false);
	}

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
				dctrl.setDeleteLabel("Are you sure you want to delete all sales records for " + 
					datePicker.getValue().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "?");
				dctrl.setOnDelete(() -> {
					service.deleteAllTransactions(histID, transactionIDs);
					notifyUser("All records deleted successfully", NotificationController.popUpType.success);
					hideDeleteDialog();
					reload();
				});
			} else {
				dctrl.setOnDelete(() -> {
					service.deleteTransaction(sel.getTransactionID());
					notifyUser("Item deleted successfully", NotificationController.popUpType.success);
					hideDeleteDialog();
					reload();
				});

				dctrl.setDeleteLabel("Are you sure you want to delete \"" + sel.getItemName() + 
					"\" with quantity " + sel.getSoldQuantity() + "?");
			}
			dctrl.setOnCancel(this::hideDeleteDialog);

			playFadeInAnimation(deleteDialogPane);

		} catch (Exception ex) {
			ex.printStackTrace();
			notifyUser("Failed to show delete dialog", NotificationController.popUpType.error);
		}
	}

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
