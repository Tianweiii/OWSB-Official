package controllers.salesController;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import models.Datas.*;
import models.Utils.QueryBuilder;
import models.Utils.ThemeManager;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {
    // UI Components
    @FXML private HBox salesManagerDashboardPane;
    @FXML private VBox mainContainer;
    @FXML private HBox headerBox;
    @FXML private Text welcomeText;
    @FXML private Button btnNotifications;
    @FXML private Circle notificationBadge;
    @FXML private ToggleButton themeToggle;
    @FXML private Button themeIcon;

    // KPI Components
    @FXML private Label lblTotalSales;
    @FXML private Label lblPendingOrders;
    @FXML private Label lblLowStock;
    @FXML private Label lblNewSuppliers;
    @FXML private ProgressBar salesProgress;
    @FXML private ProgressBar ordersProgress;
    @FXML private ProgressBar stockProgress;
    @FXML private ProgressBar suppliersProgress;

    // Filter Components
    @FXML private TextField searchField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> trendPeriodFilter;
    @FXML private ComboBox<String> productMetricFilter;
    @FXML private ComboBox<String> tableViewFilter;

    // Chart Components
    @FXML private LineChart<String, Number> salesTrendChart;
    @FXML private PieChart topProductsChart;

    // Table Components
    @FXML private TableView<SalesRecord> salesTable;
    @FXML private TableColumn<SalesRecord, String> colDate;
    @FXML private TableColumn<SalesRecord, String> colOrderId;
    @FXML private TableColumn<SalesRecord, String> colProduct;
    @FXML private TableColumn<SalesRecord, String> colCategory;
    @FXML private TableColumn<SalesRecord, Integer> colQuantity;
    @FXML private TableColumn<SalesRecord, Double> colAmount;
    @FXML private TableColumn<SalesRecord, String> colStatus;
    @FXML private TableColumn<SalesRecord, Void> colActions;
    @FXML private Label totalRecordsLabel;
    @FXML private Pagination tablePagination;

    // Data Management
    private FilteredList<SalesRecord> filteredSales;
    private final IntegerProperty totalRecords = new SimpleIntegerProperty(0);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
    private final IntegerProperty itemsPerPage = new SimpleIntegerProperty(10);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<String> notifications = new ArrayList<>();
    private final BooleanProperty hasUnreadNotifications = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        initializeTheme();
//        setupFilters();
        setupTable();
        setupCharts();
        setupNotifications();
        loadDashboardData();
        setupAutoRefresh();

        // Register cleanup on platform shutdown
        Platform.runLater(() -> {
            Scene scene = salesManagerDashboardPane.getScene();
            if (scene != null) {
                scene.getWindow().setOnCloseRequest(event -> cleanup());
            }
        });
    }

    private void setupUI() {
        // Initialize date pickers
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());

        // Initialize filters
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "Completed", "Pending", "Cancelled"
        ));
        statusFilter.getSelectionModel().selectFirst();

        categoryFilter.setItems(FXCollections.observableArrayList(
            "All", "Electronics", "Clothing", "Food", "Others"
        ));
        categoryFilter.getSelectionModel().selectFirst();

        trendPeriodFilter.setItems(FXCollections.observableArrayList(
            "Last 7 Days", "Last 30 Days", "Last 90 Days", "This Year"
        ));
        trendPeriodFilter.getSelectionModel().selectFirst();

        productMetricFilter.setItems(FXCollections.observableArrayList(
            "Sales Volume", "Revenue", "Profit Margin"
        ));
        productMetricFilter.getSelectionModel().selectFirst();

        tableViewFilter.setItems(FXCollections.observableArrayList(
            "All Records", "Today", "This Week", "This Month"
        ));
        tableViewFilter.getSelectionModel().selectFirst();

        // Bind total records label
        totalRecordsLabel.textProperty().bind(
            Bindings.createStringBinding(() ->
                String.format("Total Records: %d", totalRecords.get()),
                totalRecords
            )
        );

        // Setup pagination
        tablePagination.currentPageIndexProperty().bindBidirectional(currentPage);
        currentPage.addListener((obs, oldVal, newVal) -> refreshData());
    }

    private void initializeTheme() {
        Scene scene = salesManagerDashboardPane.getScene();
        if (scene != null) {
        ThemeManager.applyTheme(scene, ThemeManager.Theme.LIGHT, "sales-manager");
            themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                ThemeManager.applyTheme(scene,
                newVal ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT,
                "sales-manager"
                );
                
                // Update theme icon - we're doing this with button graphics now
                // In a real app, you'd replace the ImageView source here
            });
        }
    }

    private void setupFilters() {
        // Add listeners to all filters
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterSalesTable());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        trendPeriodFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            List<Transaction> transactions = new ArrayList<>();
            try {
                transactions = loadTransactions();
            } catch (Exception e) {
                showError("Error loading transactions", e);
            }
            updateCharts(transactions);
        });
        productMetricFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            List<Transaction> transactions = new ArrayList<>();
            try {
                transactions = loadTransactions();
            } catch (Exception e) {
                showError("Error loading transactions", e);
            }
            updateCharts(transactions);
        });
        tableViewFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
    }

    private void setupTable() {
        // Setup table columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status column styling
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toUpperCase());
                    getStyleClass().setAll("status-label", getStatusStyle(status));
                }
            }
        });

        // Currency formatting
        colAmount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty ? "" : String.format("$%,.2f", amount));
            }
        });

        // Actions column
        colActions.setCellFactory(createActionsColumnCallback());
    }

    private Callback<TableColumn<SalesRecord, Void>, TableCell<SalesRecord, Void>> createActionsColumnCallback() {
        return new Callback<>() {
            @Override
            public TableCell<SalesRecord, Void> call(TableColumn<SalesRecord, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("👁");
                    private final Button editBtn = new Button("✏️");
                    
                    {
                        viewBtn.getStyleClass().add("action-button");
                        editBtn.getStyleClass().add("action-button");
                        
                        viewBtn.setOnAction(event -> {
                            SalesRecord record = getTableView().getItems().get(getIndex());
                            showSalesDetails(record);
                        });
                        
                        editBtn.setOnAction(event -> {
                            SalesRecord record = getTableView().getItems().get(getIndex());
                            editSalesRecord(record);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox container = new HBox(5);
                            container.setAlignment(Pos.CENTER);
                            container.getChildren().addAll(viewBtn, editBtn);
                            setGraphic(container);
                        }
                    }
                };
            }
        };
    }

    private void setupCharts() {
        // Configure sales trend chart
        salesTrendChart.setAnimated(false);
        salesTrendChart.getXAxis().setLabel("Date");
        salesTrendChart.getYAxis().setLabel("Amount ($)");

        // Configure product performance chart
        topProductsChart.setAnimated(false);
    }

    private void setupNotifications() {
        notificationBadge.visibleProperty().bind(hasUnreadNotifications);
        btnNotifications.setOnAction(e -> showNotifications());
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                List<Transaction> transactions = loadTransactions();
                List<PurchaseOrder> orders = loadPurchaseOrders();
                List<Item> items = loadItems();
                List<Supplier> suppliers = loadSuppliers();

                Platform.runLater(() -> {
                    updateMetrics(transactions, orders, items, suppliers);
                    updateCharts(transactions);
//                    updateRecentSales(transactions);
                    checkForNotifications(transactions, items);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading data", e));
            }
        }).start();
    }

    private void setupAutoRefresh() {
        scheduler.scheduleAtFixedRate(
            this::loadDashboardData,
            5, 5, TimeUnit.MINUTES
        );
    }

    private void updateMetrics(List<Transaction> transactions,
                               List<PurchaseOrder> orders,
                               List<Item> items,
                               List<Supplier> suppliers) {
        // Calculate KPIs
        double totalSales = transactions.stream()
                .mapToDouble(t -> t.getSoldQuantity() * t.getUnitPrice())
                .sum();

        long pendingOrders = orders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getPOStatus()))
                .count();

        long lowStock = items.stream()
                .filter(i -> i.getQuantity() < 10)
                .count();

        long activeSuppliers = suppliers.size(); // All suppliers are considered active

        // Update UI
        lblTotalSales.setText(String.format("$%,.2f", totalSales));
        lblPendingOrders.setText(String.valueOf(pendingOrders));
        lblLowStock.setText(String.valueOf(lowStock));
        lblNewSuppliers.setText(String.valueOf(activeSuppliers));

        // Update progress bars
        double targetSales = 100000.0; // Example target
        salesProgress.setProgress(Math.min(totalSales / targetSales, 1.0));
        ordersProgress.setProgress(pendingOrders / 100.0);
        stockProgress.setProgress(1.0 - (lowStock / (double) items.size()));
        suppliersProgress.setProgress(1.0); // All suppliers are active
    }

    private void updateCharts(List<Transaction> transactions) {
        updateSalesTrend(transactions);
        updateTopProducts(transactions);
    }

    private void updateSalesTrend(List<Transaction> transactions) {
        try {
            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            ArrayList<HashMap<String, String>> transactionsArr = qb
                    .select()
                    .from("db/Transaction.txt")
                    .joins(DailySalesHistory.class, "dailySalesHistoryID")
                    .joins(Item.class, "itemID")
                    .get();
            Map<LocalDate, Double> dailySales = transactionsArr.stream()
                    .collect(Collectors.groupingBy(
                            t -> LocalDate.parse(t.get("createdAt").split(" ")[0]),
                            TreeMap::new,
                            Collectors.summingDouble(t -> Double.parseDouble(t.get("soldQuantity")) * Double.parseDouble(t.get("unitPrice")))
                    ));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Sales Trend");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                double amount = dailySales.getOrDefault(date, 0.0);
                series.getData().add(new XYChart.Data<>(date.format(fmt), amount));
            }

            salesTrendChart.getData().setAll(series);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateTopProducts(List<Transaction> transactions) {
        Map<String, Double> productSales = new HashMap<>();
        
        for (Transaction t : transactions) {
            Item item = getItem(t.getItemID());
            String productName = item != null ? item.getItemName() : "Unknown Product";
            double amount = t.getSoldQuantity() * t.getUnitPrice();
            productSales.merge(productName, amount, Double::sum);
        }

        ObservableList<PieChart.Data> pieData = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        topProductsChart.setData(pieData);
    }
//
//    private void updateRecentSales(List<Transaction> transactions) {
//        ObservableList<SalesRecord> salesData = FXCollections.observableArrayList();
//
//        for (Transaction t : transactions) {
//            Item item = getItem(t.getItemID());
//            salesData.add(new SalesRecord(
//                    LocalDateTime.parse(t.getTransactionDate().replace(" ", "T")),
//                    t.getTransactionID(),
//                item != null ? item.getItemName() : "Unknown",
//                item != null ? item.getDescription() : "Unknown", // Using description as category
//                    t.getSoldQuantity(),
//                    t.getSoldQuantity() * t.getUnitPrice()
//            ));
//        }
//
//        filteredSales = new FilteredList<>(salesData);
//        SortedList<SalesRecord> sortedData = new SortedList<>(filteredSales);
//        sortedData.comparatorProperty().bind(salesTable.comparatorProperty());
//        salesTable.setItems(sortedData);
//
//        // Update pagination
//        totalRecords.set(salesData.size());
//        int pageCount = (totalRecords.get() + itemsPerPage.get() - 1) / itemsPerPage.get();
//        tablePagination.setPageCount(pageCount);
//    }

    private void checkForNotifications(List<Transaction> transactions, List<Item> items) {
        notifications.clear();

        // Check for low stock items
        items.stream()
            .filter(i -> i.getQuantity() < 10)
            .forEach(i -> notifications.add(
                String.format("Low stock alert: %s (Qty: %d)", i.getItemName(), i.getQuantity())
            ));

        // Check for high-value transactions
        transactions.stream()
            .filter(t -> t.getSoldQuantity() * t.getUnitPrice() > 1000)
            .forEach(t -> notifications.add(
                String.format("High-value sale: $%.2f (Order: %s)",
                    t.getSoldQuantity() * t.getUnitPrice(), t.getTransactionID())
            ));

        hasUnreadNotifications.set(!notifications.isEmpty());
    }

    private List<Transaction> loadTransactions() throws Exception {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        System.out.println("transasc");
        return new QueryBuilder<>(Transaction.class)
                .select()
                .from("db/Transaction.txt")
                .where("transactionDate", ">=", start.toString())
                .and("transactionDate", "<=", end.plusDays(1).toString())
                .getAsObjects();
    }

    private List<PurchaseOrder> loadPurchaseOrders() throws Exception {
        System.out.println("po");
        return new QueryBuilder<>(PurchaseOrder.class)
                .select()
                .from("db/PurchaseOrder.txt")
                .getAsObjects();
    }

    private List<Item> loadItems() throws Exception {
        return new QueryBuilder<>(Item.class)
                .select()
                .from("db/Item.txt")
                .getAsObjects();
    }

    private List<Supplier> loadSuppliers() throws Exception {
        return new QueryBuilder<>(Supplier.class)
                .select()
                .from("db/Supplier.txt")
                .getAsObjects();
    }

    private Item getItem(String itemId) {
        try {
            return new QueryBuilder<>(Item.class)
                .select()
                .from("db/Item.txt")
                .where("itemID", "=", itemId)
                .getAsObjects()
                .stream()
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void handleRefresh() {
        refreshData();
    }

    @FXML
    private void handleExport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Sales Data");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            File file = fileChooser.showSaveDialog(salesTable.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("Date,Order ID,Product,Category,Quantity,Amount,Status");
                    for (SalesRecord record : salesTable.getItems()) {
                        writer.printf("%s,%s,%s,%s,%d,%.2f,%s%n",
                            record.getFormattedDate(),
                            record.getOrderId(),
                            record.getProductName(),
                            record.getCategory(),
                            record.getQuantity(),
                            record.getAmount(),
                            record.getStatus()
                        );
                    }
                }
                showInfo("Export Successful", "Sales data has been exported to " + file.getName());
            }
        } catch (Exception e) {
            showError("Export Failed", e);
        }
    }

    private void filterSalesTable() {
        String searchText = searchField.getText().toLowerCase();
        String statusText = statusFilter.getValue();
        String categoryText = categoryFilter.getValue();

        filteredSales.setPredicate(record ->
            (searchText == null || searchText.isEmpty() ||
                record.getOrderId().toLowerCase().contains(searchText) ||
                record.getProductName().toLowerCase().contains(searchText) ||
                record.getStatus().toLowerCase().contains(searchText)) &&
            (statusText == null || statusText.equals("All") ||
                record.getStatus().equalsIgnoreCase(statusText)) &&
            (categoryText == null || categoryText.equals("All") ||
                record.getCategory().equalsIgnoreCase(categoryText))
        );

        refreshPagination();
    }

    private void refreshData() {
        loadDashboardData();
    }

    private void refreshPagination() {
        int pageCount = (filteredSales.size() + itemsPerPage.get() - 1) / itemsPerPage.get();
        tablePagination.setPageCount(pageCount);
        if (currentPage.get() >= pageCount) {
            currentPage.set(pageCount - 1);
        }
    }

    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "completed" -> "status-completed";
            case "pending" -> "status-pending";
            case "cancelled" -> "status-cancelled";
            default -> "status-default";
        };
    }

    private void showNotifications() {
        if (notifications.isEmpty()) {
            showInfo("Notifications", "No new notifications");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("You have " + notifications.size() + " notification(s)");
        
        VBox content = new VBox(10);
        notifications.forEach(notification -> {
            Label label = new Label(notification);
            label.setWrapText(true);
            content.getChildren().add(label);
        });
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
        
        hasUnreadNotifications.set(false);
    }

    private void showSalesDetails(SalesRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sales Details");
        alert.setHeaderText("Order: " + record.getOrderId());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Date:"), 0, 0);
        grid.add(new Label(record.getFormattedDate()), 1, 0);
        grid.add(new Label("Product:"), 0, 1);
        grid.add(new Label(record.getProductName()), 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(new Label(record.getCategory()), 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(new Label(String.valueOf(record.getQuantity())), 1, 3);
        grid.add(new Label("Amount:"), 0, 4);
        grid.add(new Label(String.format("$%,.2f", record.getAmount())), 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(new Label(record.getStatus()), 1, 5);

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    private void editSalesRecord(SalesRecord record) {
        // Implement edit functionality
        showInfo("Edit Record", "Edit functionality to be implemented");
    }

    private void showError(String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public static class SalesRecord {
        private final LocalDateTime date;
        private final String orderId;
        private final String productName;
        private final String category;
        private final int quantity;
        private final double amount;
        private final String status;

        public SalesRecord(LocalDateTime date, String orderId,
                          String productName, String category,
                          int quantity, double amount, String status) {
            this.date = date;
            this.orderId = "TX-" + orderId;
            this.productName = productName;
            this.category = category;
            this.quantity = quantity;
            this.amount = amount;
            this.status = status;
        }

        public String getFormattedDate() {
            return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        }
        public String getOrderId() { return orderId; }
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
    }
}