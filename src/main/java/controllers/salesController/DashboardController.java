package controllers.salesController;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import models.Datas.*;
import models.Utils.QueryBuilder;
import service.DailySalesService;
import service.PurchaseRequisitionCreationRequestService;
import service.SupplierService;
import controllers.NotificationController;
import views.NotificationView;

import java.awt.Desktop;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    private static final int ITEMS_PER_PAGE = 10;
    private static final String REPORTS_DIR = "tmp";

    @FXML private HBox salesManagerDashboardPane;
    @FXML private VBox mainContainer;
    @FXML private HBox headerBox;
    @FXML private Text welcomeText;
    @FXML private Button btnNotifications;
    @FXML private Circle notificationBadge;
    @FXML private BarChart<String, Number> prRequestsChart;
    @FXML private Label prRequestsCountLabel;

    @FXML private Label lblTotalRevenue,lblTotalProfit, lblTotalOrders, lblProfitMargin, lblSupplierCount;
    @FXML private ProgressBar revenueProgress, profitProgress, ordersProgress, marginProgress, supplierProgress;

    @FXML private StackedAreaChart<String, Number> trendChart;
    @FXML private PieChart ordersChart;
    @FXML private LineChart<String, Number> revenueAnalysisChart;

    private final DailySalesService salesService;
    private final SupplierService supplierService;

    private final IntegerProperty totalRecords = new SimpleIntegerProperty(0);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
    private final IntegerProperty itemsPerPage = new SimpleIntegerProperty(ITEMS_PER_PAGE);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<String> notifications = new ArrayList<>();
    private final BooleanProperty hasUnreadNotifications = new SimpleBooleanProperty(false);

    @FXML private StackPane notificationOverlay;
    @FXML private VBox notificationPanel;

    public DashboardController() {
        this.salesService = new DailySalesService();
        this.supplierService = new SupplierService();
        ensureReportsDirectoryExists();
    }

    private void ensureReportsDirectoryExists() {
        try {
            Path reportsPath = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create reports directory: " + e.getMessage());
        }
    }

    private void openFileLocation(String filePath) {
        try {
            File file = new File(filePath);
            if (Desktop.isDesktopSupported() && file.exists()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file.getParentFile()); // Open the directory containing the file
                } else {
                    System.out.println("Desktop open action not supported");
                }
            } else {
                System.out.println("Desktop not supported or file doesn't exist: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error opening file location: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupUI();
            setupCharts();
            setupNotifications();
            updateWelcomeMessage();
            loadDashboardData();
            setupAutoRefresh();
            setupCleanup();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.err.println("Error initializing dashboard: " + e.getMessage());
        }
    }

    private void setupCleanup() {
        Platform.runLater(() -> {
            Scene scene = salesManagerDashboardPane.getScene();
            if (scene != null) {
                scene.getWindow().setOnCloseRequest(event -> cleanup());
            }
        });
    }

    private void setupUI() {
        try {
            currentPage.addListener((obs, oldVal, newVal) -> refreshData());
        } catch (Exception e) {
            System.err.println("Error in setupUI: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private List<Transaction> loadTransactionsForCharting() {
        try {
            QueryBuilder<Transaction> qb = new QueryBuilder<>(Transaction.class);
            ArrayList<HashMap<String, String>> transactionData = qb
                    .select()
                    .from("db/Transaction.txt")
                    .joins(DailySalesHistory.class, "dailySalesHistoryID")
                    .joins(Item.class, "itemID")
                    .get();

            List<Transaction> transactions = new ArrayList<>();
            for (HashMap<String, String> data : transactionData) {
                Transaction t = new Transaction();
                t.initialize(data);
                transactions.add(t);
            }
            return transactions;
        } catch (Exception e) {
            System.out.println("Error loading transactions for charting: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void setupCharts() {
        try {
            if (trendChart != null) {
                trendChart.setAnimated(false);
                trendChart.getXAxis().setLabel("Date");
                trendChart.getYAxis().setLabel("Amount ($)");
            }

            if (prRequestsChart != null) {
                prRequestsChart.setAnimated(false);
                prRequestsChart.getXAxis().setLabel("Item Name");
                prRequestsChart.getYAxis().setLabel("Minimum Purchase Quantity");
            }
        } catch (Exception e) {
            System.err.println("Error in setupCharts: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private void setupNotifications() {
        // Initialize the notification badge
        if (notificationBadge != null) {
            notificationBadge.setVisible(false);
            hasUnreadNotifications.addListener((obs, oldVal, newVal) -> Platform.runLater(() -> notificationBadge.setVisible(newVal)));
        }

        if (btnNotifications != null) {
            btnNotifications.setOnAction(e -> showNotifications());
        }
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                List<Supplier> suppliers = supplierService.getAll();

                Platform.runLater(() -> {
                    if (lblSupplierCount != null) {
                        lblSupplierCount.setText(String.valueOf(suppliers.size()));
                    }
                    if (supplierProgress != null) {
                        supplierProgress.setProgress(Math.min(suppliers.size() / 100.0, 1.0));
                    }
                });

                List<Transaction> transactions = new ArrayList<>();
                List<Item> items = new ArrayList<>();

                try {
                    transactions = loadTransactions();
                } catch (Exception e) {
                    System.err.println("Error loading transactions: " + e.getMessage());
                    System.out.println(e.getMessage());
                }

                try {
                    items = loadItems();
                } catch (Exception e) {
                    System.err.println("Error loading items: " + e.getMessage());
                    System.out.println(e.getMessage());
                }

                final List<Transaction> finalTransactions = transactions;
                final List<Item> finalItems = items;
                final Map<String, Item> itemMap = finalItems.stream()
                        .collect(Collectors.toMap(Item::getItemID, item -> item));
                List<PurchaseRequisitionCreationRequest> prRequests = new PurchaseRequisitionCreationRequestService().getAll();

                Platform.runLater(() -> {
                    try {
                        updateKPIs(finalTransactions, itemMap);
                        updateCharts(finalTransactions, itemMap);
                        updatePRRequestsChart(prRequests, itemMap);
                        if (prRequestsCountLabel != null) {
                            prRequestsCountLabel.setText(prRequests.size() + " PRs to be created");
                            prRequestsCountLabel.setStyle("-fx-text-fill: " + (!prRequests.isEmpty() ? "#2196F3" : "#aaa") + ";");
                        }
                    } catch (Exception e) {
                        System.err.println("Error updating UI: " + e.getMessage());
                        System.out.println(e.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Critical error loading dashboard data: " + e.getMessage());
                    System.out.println(e.getMessage());
                    showNotification("Error loading data", NotificationController.popUpType.error);
                });
            }
        }).start();
    }

    private void setupAutoRefresh() {
        scheduler.scheduleAtFixedRate(
                this::loadDashboardData,
                5, 5, TimeUnit.MINUTES
        );
    }

    /**
     * Updates all dashboard metrics with the provided data
     */
    private void updateKPIs(List<Transaction> transactions, Map<String, Item> itemMap) {
        try {
            double totalRevenue = 0;
            double totalProfit = 0;
            int totalOrders = transactions.size();

            for (Transaction tx : transactions) {
                // Revenue from marked-up price in Transaction
                DailySalesHistory history = salesService.getDailySalesHistory(tx.getDailySalesHistoryID());
                if (history != null) {
                    double revenue = tx.getSoldQuantity() * tx.getMarkedUpPrice();
                    totalRevenue += revenue;

                    // Cost from original price in Item
                    Item item = itemMap.get(tx.getItemID());
                    if (item != null) {
                        double originalCost = item.getUnitPrice() * tx.getSoldQuantity();
                        double profit = revenue - originalCost;
                        totalProfit += profit;
                    }
                }

            }

            double profitMargin = totalRevenue > 0 ? (totalProfit / totalRevenue) * 100 : 0;

            double finalTotalRevenue = totalRevenue;
            double finalTotalProfit = totalProfit;
            Platform.runLater(() -> {
                if (lblTotalRevenue != null) {
                    lblTotalRevenue.setText(String.format("$%.2f", finalTotalRevenue));
                }
                if (lblTotalProfit != null) {
                    lblTotalProfit.setText(String.format("$%.2f", finalTotalProfit));
                }
                if (lblTotalOrders != null) {
                    lblTotalOrders.setText(String.valueOf(totalOrders));
                }
                if (lblProfitMargin != null) {
                    lblProfitMargin.setText(String.format("%.1f%%", profitMargin));
                }

                if (revenueProgress != null) {
                    revenueProgress.setProgress(Math.min(finalTotalRevenue / 10000.0, 1.0));
                }
                if (profitProgress != null) {
                    profitProgress.setProgress(Math.min(finalTotalProfit / 2000.0, 1.0));
                }
                if (ordersProgress != null) {
                    ordersProgress.setProgress(Math.min(totalOrders / 100.0, 1.0));
                }
                if (marginProgress != null) {
                    marginProgress.setProgress(profitMargin / 100.0);
                }
            });
        } catch (Exception e) {
            System.err.println("Error updating KPIs: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private void updateCharts(List<Transaction> transactions, Map<String, Item> itemMap) {
        updateTrendChart(transactions, itemMap);
        updateProductMix(transactions);
        updateRevenueAnalysis(transactions);
    }

    private void updateTrendChart(List<Transaction> transactions, Map<String, Item> itemMap) {
        trendChart.getData().clear();

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        profitSeries.setName("Profit");

        // Group by date
        Map<String, DailyMetrics> dailyMetrics = new TreeMap<>();

        for (Transaction tx : transactions) {
            DailySalesHistory history = salesService.getDailySalesHistory(tx.getDailySalesHistoryID());
            if (history != null) {
                LocalDate date = history.getCreatedAt();
                String formattedDate = date.format(DateTimeFormatter.ISO_DATE);
                double revenue = tx.getSoldQuantity() * tx.getMarkedUpPrice();
                double profit = 0;

                Item item = itemMap.get(tx.getItemID());
                if (item != null) {
                    double cost = item.getUnitPrice() * tx.getSoldQuantity();
                    profit = revenue - cost;
                }

                dailyMetrics.computeIfAbsent(formattedDate, k -> new DailyMetrics())
                        .add(revenue, profit);
            }

        }

        // Add to chart
        dailyMetrics.forEach((date, metrics) -> {
            revenueSeries.getData().add(new XYChart.Data<>(date, metrics.revenue));
            profitSeries.getData().add(new XYChart.Data<>(date, metrics.profit));
        });

        trendChart.getData().addAll(revenueSeries, profitSeries);
    }

    private void updateProductMix(List<Transaction> transactions) {
        Map<String, Double> productMetrics = new HashMap<>();
        double totalRevenue = 0;

        for (Transaction t : transactions) {
            Item item = getItem(t.getItemID());
            if (item == null) continue;

            String product = item.getItemName();
            double revenue = t.getSoldQuantity() * t.getUnitPrice();
            productMetrics.merge(product, revenue, Double::sum);
            totalRevenue += revenue;
        }

        // Create pie chart data with percentages
        double finalTotalRevenue = totalRevenue;
        ObservableList<PieChart.Data> pieData = productMetrics.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)  // Limit to top 5 products for better visibility
                .map(entry -> {
                    double percentage = (entry.getValue() / finalTotalRevenue) * 100;
                    return new PieChart.Data(
                            String.format("%s\n%.1f%%", entry.getKey(), percentage),
                            entry.getValue()
                    );
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        ordersChart.setData(pieData);
        ordersChart.setTitle(String.format("Total Revenue: $%.2f", totalRevenue));

        String[] colors = {
                "#2ecc71", "#3498db", "#9b59b6", "#e74c3c",
                "#f1c40f", "#1abc9c", "#e67e22", "#34495e"
        };

        for (int i = 0; i < pieData.size(); i++) {
            PieChart.Data data = pieData.get(i);
            data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");

            Tooltip tooltip = new Tooltip(String.format(
                    "%s\nRevenue: $%.2f\nShare: %.1f%%",
                    data.getName().split("\n")[0],
                    data.getPieValue(),
                    (data.getPieValue() / finalTotalRevenue) * 100
            ));
            Tooltip.install(data.getNode(), tooltip);

            data.getNode().setOnMouseEntered(e ->
                    data.getNode().setStyle("-fx-pie-color: derive(" + colors[pieData.indexOf(data) % colors.length] + ", 30%);")
            );
            data.getNode().setOnMouseExited(e ->
                    data.getNode().setStyle("-fx-pie-color: " + colors[pieData.indexOf(data) % colors.length] + ";")
            );
        }
    }

    private void addUnreadNotifications(List<PurchaseRequisitionCreationRequest> prRequests) {
        try {
            Platform.runLater(() -> {
                if (prRequestsCountLabel != null) {
                    prRequestsCountLabel.setText(prRequests.size() + " purchase requests need to be made");
                    prRequestsCountLabel.setStyle("-fx-text-fill: " + (!prRequests.isEmpty() ? "#2196F3" : "#aaa") + ";");
                }

                notifications.clear();
                Map<String, Item> itemMap = loadItems().stream().collect(Collectors.toMap(Item::getItemID, item -> item));
                for (PurchaseRequisitionCreationRequest prRequest : prRequests) {
                    int minimumPurchaseQuantity = prRequest.getMinimumPurchaseQuantity();
                    String itemName = prRequest.getItemID();
                    if (itemMap.containsKey(prRequest.getItemID())) {
                        itemName = itemMap.get(prRequest.getItemID()).getItemName();
                    }
                    notifications.add(String.format(
                            "Please create a purchase request for: %s (Minimum Purchase Quantity: %s)",
                            itemName,
                            minimumPurchaseQuantity
                    ));
                }
                hasUnreadNotifications.set(!prRequests.isEmpty());
            });

        } catch (Exception e) {
            System.err.println("Error updating purchase requests notifications: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private void setupTooltip(XYChart.Data<String, Number> data, String level) {
        Tooltip tooltip = new Tooltip(String.format(
                "Item: %s\nQuantity: %d\nStatus: %s",
                data.getXValue(),
                data.getYValue().intValue(),
                level
        ));
        Tooltip.install(data.getNode(), tooltip);
    }

    private void updateRevenueAnalysis(List<Transaction> transactions) {
        revenueAnalysisChart.getData().clear();

        // Create series for different revenue metrics
        XYChart.Series<String, Number> totalRevenue = new XYChart.Series<>();
        totalRevenue.setName("Total Revenue");

        XYChart.Series<String, Number> avgOrderValue = new XYChart.Series<>();
        avgOrderValue.setName("Average Order Value");

        XYChart.Series<String, Number> profitMargin = new XYChart.Series<>();
        profitMargin.setName("Profit Margin");

        Map<LocalDate, RevenueMetrics> dailyMetrics = new TreeMap<>();

        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            for (Transaction t : transactions) {
                ArrayList<DailySalesHistory> historyData = qb
                        .select()
                        .from("db/DailySalesHistory.txt")
                        .where("dailySalesHistoryID", "=", t.getDailySalesHistoryID())
                        .getAsObjects();
                LocalDate date = historyData.get(0).getCreatedAt();
                double amount = t.getSoldQuantity() * t.getUnitPrice();

                dailyMetrics.computeIfAbsent(date, k -> new RevenueMetrics())
                        .addTransaction(amount);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Add data points
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
        dailyMetrics.forEach((date, metrics) -> {
            String dateStr = date.format(fmt);
            totalRevenue.getData().add(new XYChart.Data<>(dateStr, metrics.getTotalRevenue()));
            avgOrderValue.getData().add(new XYChart.Data<>(dateStr, metrics.getAverageOrderValue()));
            profitMargin.getData().add(new XYChart.Data<>(dateStr, metrics.getProfitMargin()));
        });

        revenueAnalysisChart.getData().addAll(totalRevenue, avgOrderValue, profitMargin);
    }

    private static class RevenueMetrics {
        private double totalRevenue = 0;
        private int transactionCount = 0;
        private static final double TARGET_MARGIN = 0.3;

        void addTransaction(double amount) {
            totalRevenue += amount;
            transactionCount++;
        }

        double getTotalRevenue() { return totalRevenue; }
        double getAverageOrderValue() { return transactionCount > 0 ? totalRevenue / transactionCount : 0; }
        double getProfitMargin() { return totalRevenue * TARGET_MARGIN; }
    }

    private List<Transaction> loadTransactions() throws Exception {
        return new QueryBuilder<>(Transaction.class)
                .select()
                .from("db/Transaction.txt")
                .getAsObjects();
    }

    private List<Item> loadItems() {
        try {
            QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
            ArrayList<HashMap<String, String>> itemData = qb
                    .select()
                    .from("db/Item.txt")
                    .get();

            List<Item> items = new ArrayList<>();
            for (HashMap<String, String> data : itemData) {
                try {
                    if (data != null && !data.isEmpty()) {
                        Item item = new Item();
                        item.setItemID(data.getOrDefault("itemID", ""));
                        item.setItemName(data.getOrDefault("itemName", "Unknown Item"));
                        item.setQuantity(Integer.parseInt(data.getOrDefault("quantity", "0")));
                        item.setAlertSetting(Integer.parseInt(data.getOrDefault("alertSetting", "10")));
                        item.setUnitPrice(Double.parseDouble(data.getOrDefault("unitPrice", "0")));
                        items.add(item);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing item: " + data + " - " + e.getMessage());
                }
            }
            return items;
        } catch (Exception e) {
            System.err.println("Error loading items: " + e.getMessage());
            return new ArrayList<>();
        }
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

    /**
     * Auto-generate and export performance data to CSV in tmp directory
     * Opens the file location automatically after creation
     */
    @FXML
    private void handleExport() {
        new Thread(() -> {
            try {
                String filePath = generatePerformanceReport();
                if (filePath != null) {
                    Platform.runLater(() -> {
                        showNotification("Performance report generated successfully", NotificationController.popUpType.success);
                        // Open file location automatically
                        openFileLocation(filePath);
                    });
                } else {
                    Platform.runLater(() -> showNotification("Error generating performance report", NotificationController.popUpType.error));
                }
            } catch (Exception e) {
                System.err.println("Error in handleExport: " + e.getMessage());
                Platform.runLater(() -> showNotification("Error exporting data", NotificationController.popUpType.error));
            }
        }).start();
    }

    private String generatePerformanceReport() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = REPORTS_DIR + File.separator + "performance_report_" + timestamp + ".csv";

            try (PrintWriter writer = new PrintWriter(filename)) {
                writer.write('\ufeff');

                writer.println("OWSB Sales Performance Dashboard Report");
                writer.println("==========================================");
                writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();

                writer.println("1. EXECUTIVE SUMMARY");
                writer.println("------------------");
                writer.println("Total Revenue (Period)," + lblTotalRevenue.getText().replace("$", ""));
                writer.println("Total Profit (Period)," + lblTotalProfit.getText().replace("$", ""));
                writer.println("Total Orders (Period)," + lblTotalOrders.getText());
                writer.println("Overall Profit Margin," + lblProfitMargin.getText());
                writer.println("Active Suppliers," + lblSupplierCount.getText());
                writer.println();

                writer.println("2. DAILY PERFORMANCE METRICS");
                writer.println("-------------------------");
                writer.println("Date,Revenue,Profit,Orders,Average Order Value,Profit Margin %,Target Achievement %,Growth Rate %");

                List<Transaction> transactions = loadTransactionsForCharting();
                Map<String, Item> itemMap = loadItems().stream()
                        .collect(Collectors.toMap(Item::getItemID, item -> item));

                Map<LocalDate, DailyMetrics> dailyMetrics = new TreeMap<>();
                double previousDayRevenue = 0;

                for (Transaction tx : transactions) {
                    DailySalesHistory history = salesService.getDailySalesHistory(tx.getDailySalesHistoryID());
                    if (history != null) {
                        LocalDate date = history.getCreatedAt();
                        double revenue = tx.getSoldQuantity() * tx.getMarkedUpPrice();
                        double profit = 0;

                        Item item = itemMap.get(tx.getItemID());
                        if (item != null) {
                            double cost = item.getUnitPrice() * tx.getSoldQuantity();
                            profit = revenue - cost;
                        }

                        dailyMetrics.computeIfAbsent(date, k -> new DailyMetrics())
                                .add(revenue, profit);
                    }
                }

                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                for (Map.Entry<LocalDate, DailyMetrics> entry : dailyMetrics.entrySet()) {
                    DailyMetrics metrics = entry.getValue();
                    double margin = metrics.revenue > 0 ? (metrics.profit / metrics.revenue) * 100 : 0;
                    double avgOrderValue = metrics.orders > 0 ? metrics.revenue / metrics.orders : 0;
                    double targetAchievement = (metrics.revenue / 10000.0) * 100;
                    double growthRate = previousDayRevenue > 0 ?
                            ((metrics.revenue - previousDayRevenue) / previousDayRevenue) * 100 : 0;

                    writer.printf("%s,%.2f,%.2f,%d,%.2f,%.1f,%.1f,%.1f%n",
                            entry.getKey().format(dateFormat),
                            metrics.revenue,
                            metrics.profit,
                            metrics.orders,
                            avgOrderValue,
                            margin,
                            targetAchievement,
                            growthRate
                    );

                    previousDayRevenue = metrics.revenue;
                }
                writer.println();

                writer.println("3. PRODUCT PERFORMANCE ANALYSIS");
                writer.println("-----------------------------");
                writer.println("Product Name,Total Revenue,Total Units Sold,Profit Contribution %,Average Price,Profit per Unit,Performance Status");

                Map<String, ProductMetrics> productPerformance = new HashMap<>();
                double finalTotalRevenue = 0;

                for (Transaction tx : transactions) {
                    Item item = itemMap.get(tx.getItemID());
                    if (item != null) {
                        String productName = item.getItemName();
                        double revenue = tx.getSoldQuantity() * tx.getMarkedUpPrice();
                        double cost = item.getUnitPrice() * tx.getSoldQuantity();
                        productPerformance.computeIfAbsent(productName, k -> new ProductMetrics())
                                .add(revenue, tx.getSoldQuantity(), cost);
                        finalTotalRevenue += revenue;
                    }
                }

                double finalTotalRevenue1 = finalTotalRevenue;
                productPerformance.entrySet().stream()
                        .sorted((e1, e2) -> Double.compare(e2.getValue().revenue, e1.getValue().revenue))
                        .forEach(entry -> {
                            ProductMetrics metrics = entry.getValue();
                            double contribution = (metrics.revenue / finalTotalRevenue1) * 100;
                            double avgPrice = metrics.unitsSold > 0 ? metrics.revenue / metrics.unitsSold : 0;
                            double profitPerUnit = metrics.unitsSold > 0 ? (metrics.revenue - metrics.cost) / metrics.unitsSold : 0;
                            String status = getPerformanceStatus(contribution);

                            writer.printf("%s,%.2f,%d,%.1f,%.2f,%.2f,%s%n",
                                    entry.getKey(),
                                    metrics.revenue,
                                    metrics.unitsSold,
                                    contribution,
                                    avgPrice,
                                    profitPerUnit,
                                    status
                            );
                        });

                writer.println();
                writer.println("4. PERFORMANCE INDICATORS");
                writer.println("------------------------");
                writer.println("Metric,Value,Status");
                writer.printf("Average Daily Revenue,%.2f,%s%n",
                        finalTotalRevenue / Math.max(dailyMetrics.size(), 1),
                        getMetricStatus(finalTotalRevenue / Math.max(dailyMetrics.size(), 1), 5000));

                int totalOrders = Integer.parseInt(lblTotalOrders.getText());
                if (totalOrders > 0) {
                    writer.printf("Average Order Value,%.2f,%s%n",
                            finalTotalRevenue / totalOrders,
                            getMetricStatus(finalTotalRevenue / totalOrders, 100));
                }

                String profitMarginText = lblProfitMargin.getText().replace("%", "");
                if (!profitMarginText.isEmpty()) {
                    writer.printf("Profit Margin,%.1f%%,%s%n",
                            Double.parseDouble(profitMarginText),
                            getMetricStatus(Double.parseDouble(profitMarginText), 30));
                }

                return filename;
            }
        } catch (Exception e) {
            System.err.println("Error generating performance report: " + e.getMessage());
            return null;
        }
    }

    private String getPerformanceStatus(double contribution) {
        if (contribution >= 20) return "Top Performer";
        if (contribution >= 10) return "Strong Performer";
        if (contribution >= 5) return "Moderate Performer";
        return "Under Performer";
    }

    private String getMetricStatus(double value, double target) {
        double percentage = (value / target) * 100;
        if (percentage >= 120) return "Exceptional";
        if (percentage >= 100) return "On Target";
        if (percentage >= 80) return "Near Target";
        return "Below Target";
    }

    private static class ProductMetrics {
        double revenue = 0;
        int unitsSold = 0;
        double cost = 0;

        void add(double revenue, int units, double cost) {
            this.revenue += revenue;
            this.unitsSold += units;
            this.cost += cost;
        }
    }

    private void refreshData() {
        loadDashboardData();
    }

    @FXML
    private void showNotifications() {
        if (notifications.isEmpty()) {
            showNotification("No new purchase requests", NotificationController.popUpType.info);
            return;
        }

        try {
            if (notificationOverlay == null) {
                notificationOverlay = new StackPane();
                notificationOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
                notificationOverlay.setVisible(false);
                notificationOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                notificationOverlay.setPickOnBounds(false);

                notificationPanel = new VBox(20);
                notificationPanel.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-padding: 20;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);" +
                                "-fx-background-radius: 8;"
                );
                notificationPanel.setMaxWidth(1000);
                notificationPanel.setPrefWidth(1000);
                notificationPanel.setMaxHeight(600);
                notificationPanel.setPickOnBounds(true);

                StackPane.setAlignment(notificationPanel, Pos.CENTER_RIGHT);
                StackPane.setMargin(notificationPanel, new Insets(20, 20, 20, 20));
                notificationOverlay.getChildren().add(notificationPanel);

                if (!salesManagerDashboardPane.getChildren().contains(notificationOverlay)) {
                    salesManagerDashboardPane.getChildren().add(notificationOverlay);
                }
            }

            notificationPanel.getChildren().clear();

            // Header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label titleLabel = new Label("Purchase Requests");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button closeButton = new Button("×");
            closeButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-font-size: 20;" +
                            "-fx-padding: 0 5;"
            );
            closeButton.setOnAction(e -> hideNotificationPanel());

            header.getChildren().addAll(titleLabel, spacer, closeButton);

            // Content
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setStyle("-fx-background-color: white;");

            VBox requestsContainer = new VBox(10);
            requestsContainer.setStyle("-fx-padding: 10 0;");

            for (String notification : notifications) {
                HBox requestBox = createRequestBox(notification);
                requestsContainer.getChildren().add(requestBox);
            }

            scrollPane.setContent(requestsContainer);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            // Footer with actions
            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: transparent #ddd transparent transparent; -fx-border-width: 1;");

            Button exportButton = new Button("Export Purchase Requests");
            exportButton.setStyle(
                    "-fx-background-color: #2196F3;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 8 15;" +
                            "-fx-background-radius: 4;"
            );
            exportButton.setOnAction(e -> exportPurchaseRequests());

            Button markReadButton = new Button("Mark All Read");
            markReadButton.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 8 15;" +
                            "-fx-background-radius: 4;"
            );
            markReadButton.setOnAction(e -> {
                notifications.clear();
                hasUnreadNotifications.set(false);
                if (notificationBadge != null) {
                    notificationBadge.setVisible(false);
                }
                hideNotificationPanel();
            });

            footer.getChildren().addAll(exportButton, markReadButton);

            notificationPanel.getChildren().addAll(header, scrollPane, footer);

            notificationOverlay.setVisible(true);
            notificationPanel.setTranslateX(notificationPanel.getWidth());

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationPanel);
            slideIn.setFromX(notificationPanel.getWidth());
            slideIn.setToX(0);
            slideIn.play();

        } catch (Exception e) {
            System.err.println("Error showing purchase requests dialog: " + e.getMessage());
            System.out.println(e.getMessage());
            showNotification("Error showing purchase requests", NotificationController.popUpType.error);
        }
    }

    private HBox createRequestBox(String notification) {
        HBox requestBox = new HBox(10);
        requestBox.setAlignment(Pos.CENTER_LEFT);
        requestBox.setPadding(new Insets(10));
        requestBox.setStyle(
                "-fx-background-color: #f5f5f5;" +
                "-fx-background-radius: 4;"
        );

        Circle icon = new Circle(8);
        icon.setFill(javafx.scene.paint.Color.web("#2196F3"));

        Label label = new Label(notification);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #333333;");

        requestBox.getChildren().addAll(icon, label);
        return requestBox;
    }

    private void hideNotificationPanel() {
        if (notificationOverlay != null && notificationPanel != null) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notificationPanel);
            slideOut.setFromX(0);
            slideOut.setToX(notificationPanel.getWidth());
            slideOut.setOnFinished(e -> {
                notificationOverlay.setVisible(false);
                salesManagerDashboardPane.getChildren().remove(notificationOverlay);
                notificationOverlay = null;
                notificationPanel = null;
            });
            slideOut.play();
        }
    }

    private void exportPurchaseRequests() {
        new Thread(() -> {
            try {
                String filePath = generatePurchaseRequestsReport();
                if (filePath != null) {
                    Platform.runLater(() -> {
                        showNotification("Purchase requests report generated successfully", NotificationController.popUpType.success);
                        openFileLocation(filePath);
                    });
                } else {
                    Platform.runLater(() -> showNotification("Error generating purchase requests report", NotificationController.popUpType.error));
                }
            } catch (Exception e) {
                System.err.println("Error in exportPurchaseRequests: " + e.getMessage());
                Platform.runLater(() -> showNotification("Error exporting purchase requests", NotificationController.popUpType.error));
            }
        }).start();
    }

    private String generatePurchaseRequestsReport() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = REPORTS_DIR + File.separator + "purchase_requests_" + timestamp + ".csv";

            try (PrintWriter writer = new PrintWriter(filename)) {
                writer.write('\ufeff');

                // Company Header
                writer.println("OWSB Corporation");
                writer.println("Sales Management System");
                writer.println("Purchase Requests Report");
                writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();

                writer.println("SECTION 1: EXECUTIVE SUMMARY");
                writer.println("----------------------------");
                List<PurchaseRequisitionCreationRequest> prRequests = new PurchaseRequisitionCreationRequestService().getAll();
                int totalPRs = prRequests.size();
                int totalQuantity = prRequests.stream().mapToInt(PurchaseRequisitionCreationRequest::getMinimumPurchaseQuantity).sum();
                writer.println("Total Pending Purchase Requests," + totalPRs);
                writer.println("Total Minimum Quantity Required," + totalQuantity);
                writer.println();

                writer.println("SECTION 2: DETAILED PURCHASE REQUESTS");
                writer.println("-------------------------------------");
                writer.println("Item Name,Item ID,Minimum Purchase Quantity (Units),Request Status,Date Generated");
                Map<String, Item> itemMap = loadItems().stream().collect(Collectors.toMap(Item::getItemID, item -> item));
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                for (PurchaseRequisitionCreationRequest pr : prRequests) {
                    String itemName = pr.getItemID();
                    if (itemMap.containsKey(pr.getItemID())) {
                        itemName = itemMap.get(pr.getItemID()).getItemName();
                    }
                    writer.printf("%s,%s,%d,Pending,%s%n",
                            itemName,
                            pr.getItemID(),
                            pr.getMinimumPurchaseQuantity(),
                            now
                    );
                }
                writer.println();

                writer.println("SECTION 3: NOTES & CONTACT");
                writer.println("--------------------------");
                writer.println("This report lists all pending purchase requisition requests as of the generation date.");
                writer.println("For questions or clarifications, please contact: sales@owsb-corp.com");
                writer.println();

                // Footer/Disclaimer
                writer.println("--------------------------------------------------");
                writer.println("This is a system-generated report. For internal use only.");
                writer.println("OWSB Corporation © " + LocalDate.now().getYear());

                return filename;
            }
        } catch (Exception e) {
            System.err.println("Error generating purchase requests report: " + e.getMessage());
            return null;
        }
    }

    private void showNotification(String message, NotificationController.popUpType type) {
        try {
            new NotificationView(message, type, NotificationController.popUpPos.BOTTOM_RIGHT).show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void cleanup() {
        if (!scheduler.isShutdown()) {
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

    private void updateWelcomeMessage() {
        try {
            String username = "User";
            try {
                username = models.Utils.SessionManager.getInstance().getUserData().get("username");
                if (username == null || username.isEmpty()) {
                    username = "User";
                }
            } catch (Exception e) {
                // Fall back to "User" if cannot get the username
            }

            welcomeText.setText("Welcome back, " + username);
        } catch (Exception e) {
            System.out.println("Error setting welcome message: " + e.getMessage());
        }
    }

    private static class DailyMetrics {
        double revenue = 0;
        double profit = 0;
        int orders = 0;

        void add(double revenue, double profit) {
            this.revenue += revenue;
            this.profit += profit;
            this.orders++;
        }
    }

    private void updatePRRequestsChart(List<PurchaseRequisitionCreationRequest> prRequests, Map<String, Item> itemMap) {
        if (prRequestsChart == null) return;

        this.addUnreadNotifications(prRequests);

        prRequestsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (PurchaseRequisitionCreationRequest pr : prRequests) {
            String itemName = pr.getItemID();
            if (itemMap.containsKey(pr.getItemID())) {
                itemName = itemMap.get(pr.getItemID()).getItemName();
            }
            int qty = pr.getMinimumPurchaseQuantity();
            series.getData().add(new XYChart.Data<>(itemName, qty));
        }
        prRequestsChart.getData().add(series);
    }
}