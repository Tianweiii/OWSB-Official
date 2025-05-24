package controllers.salesController;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import models.Datas.*;
import models.Utils.QueryBuilder;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import service.DailySalesService;
import service.ItemService;
import service.SupplierService;
import controllers.NotificationController;
import views.NotificationView;

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

/**
 * Controller for the Sales Manager Dashboard
 * Handles displaying sales metrics, charts, and forecasting
 */
public class DashboardController implements Initializable {
    // Constants
    private static final int ITEMS_PER_PAGE = 10;
    private static final int AUTO_REFRESH_MINUTES = 5;
    private static final String DATE_FORMAT = "MMM dd";
    private static final double PROFIT_MARGIN = 0.15; // 15% profit margin

    // FXML Components
    @FXML private HBox salesManagerDashboardPane;
    @FXML private VBox mainContainer;
    @FXML private HBox headerBox;
    @FXML private Text welcomeText;
    @FXML private Button btnNotifications;
    @FXML private Circle notificationBadge;
    @FXML private BarChart<String, Number> lowStockChart;

    // KPI Components
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalProfit;
    @FXML private Label lblTotalOrders;
    @FXML private Label lblProfitMargin;
    @FXML private ProgressBar revenueProgress;
    @FXML private ProgressBar profitProgress;
    @FXML private ProgressBar ordersProgress;
    @FXML private ProgressBar marginProgress;
    @FXML private Label lblSupplierCount;
    @FXML private ProgressBar supplierProgress;

    // Filter Components
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> analyticsViewFilter;

    // Chart Components
    @FXML private StackedAreaChart<String, Number> trendChart;
    @FXML private PieChart ordersChart;
    @FXML private ScatterChart<String, Number> salesVelocityChart;
    @FXML private StackedBarChart<String, Number> categoryPerformanceChart;
    @FXML private LineChart<String, Number> revenueAnalysisChart;
    @FXML private BubbleChart<Number, Number> inventoryHealthMap;

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
    @FXML private Label stockTurnoverLabel;
    @FXML private Label avgInventoryLabel;
    @FXML private Label stockoutRiskLabel;

    // Forecast Components
    @FXML private StackPane forecastOverlay;
    @FXML private VBox forecastPane;
    @FXML private ComboBox<String> forecastPeriod;
    @FXML private ComboBox<String> forecastMethod;
    @FXML private LineChart<String, Number> forecastChart;
    @FXML private Label predictedRevenueLabel;
    @FXML private Label growthRateLabel;
    @FXML private Label confidenceLabel;

    // Services
    private final DailySalesService salesService;
    private final SupplierService supplierService;
    private final ItemService itemService;

    // Data Management
    private FilteredList<SalesRecord> filteredSales;
    private final IntegerProperty totalRecords = new SimpleIntegerProperty(0);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
    private final IntegerProperty itemsPerPage = new SimpleIntegerProperty(ITEMS_PER_PAGE);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<String> notifications = new ArrayList<>();
    private final BooleanProperty hasUnreadNotifications = new SimpleBooleanProperty(false);
    
    @FXML private Label lowStockCountLabel;
    @FXML private StackPane notificationOverlay;
    @FXML private VBox notificationPanel;
    
    public DashboardController() {
        this.salesService = new DailySalesService();
        this.supplierService = new SupplierService();
        this.itemService = new ItemService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("Initializing dashboard...");
        setupUI();
        setupFilters();
        setupCharts();
        setupNotifications();
        updateWelcomeMessage();
        loadDashboardData();
        setupAutoRefresh();
        setupCleanup();
        System.out.println("Dashboard initialization complete");
        } catch (Exception e) {
            e.printStackTrace();
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
        // Initialize date pickers
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());

            // Initialize filters with default values
            if (statusFilter != null) {
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "Completed", "Pending", "Cancelled"
        ));
        statusFilter.getSelectionModel().selectFirst();
            }

            if (analyticsViewFilter != null) {
                analyticsViewFilter.setItems(FXCollections.observableArrayList(
                    "All Records", "Today", "This Week", "This Month"
                ));
                analyticsViewFilter.getSelectionModel().selectFirst();
            }

            currentPage.addListener((obs, oldVal, newVal) -> refreshData());
        } catch (Exception e) {
            System.err.println("Error in setupUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Completed", "Pending", "Cancelled"
            ));
            statusFilter.getSelectionModel().selectFirst();
        }

        if (analyticsViewFilter != null) {
        analyticsViewFilter.setItems(FXCollections.observableArrayList(
            "All Records", "Today", "This Week", "This Month"
        ));
        analyticsViewFilter.getSelectionModel().selectFirst();
        }

        if (startDatePicker != null) {
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        }
        if (endDatePicker != null) {
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        }
        if (statusFilter != null) {
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        }
        if (analyticsViewFilter != null) {
        analyticsViewFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
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
            // Configure sales trend chart
            if (trendChart != null) {
                trendChart.setAnimated(false);
                trendChart.getXAxis().setLabel("Date");
                trendChart.getYAxis().setLabel("Amount ($)");
            }

            // Configure low stock chart
            if (lowStockChart != null) {
                lowStockChart.setAnimated(false);
                lowStockChart.setTitle("Inventory Alerts");
                if (lowStockChart.getYAxis() instanceof NumberAxis) {
                    NumberAxis yAxis = (NumberAxis) lowStockChart.getYAxis();
                    yAxis.setTickUnit(1);
                    yAxis.setMinorTickCount(0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in setupCharts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNotifications() {
        // Initialize the notification badge
        if (notificationBadge != null) {
            notificationBadge.setVisible(false);
            hasUnreadNotifications.addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> notificationBadge.setVisible(newVal));
            });
        }
        
        if (btnNotifications != null) {
            btnNotifications.setOnAction(e -> showNotifications());
        }
    }

    private void loadDashboardData() {
        System.out.println("Loading dashboard data...");
        new Thread(() -> {
            try {
                // Initialize empty lists to prevent null pointer exceptions
                List<Transaction> transactions = new ArrayList<>();
                List<Item> items = new ArrayList<>();
                
                try {
                    System.out.println("Loading transactions...");
                    transactions = loadTransactions();
                } catch (Exception e) {
                    System.err.println("Error loading transactions: " + e.getMessage());
                    e.printStackTrace();
                }
                
                try {
                    System.out.println("Loading items...");
                    items = loadItems();
                    System.out.println("Loaded " + items.size() + " items");
                    // Debug print items with low stock
                    items.stream()
                        .filter(item -> {
                            int alertThreshold = item.getAlertSetting() > 0 ? item.getAlertSetting() : 10;
                            return item.getQuantity() < alertThreshold;
                        })
                        .forEach(item -> System.out.println("Low stock item: " + item.getItemName() + 
                            " (Qty: " + item.getQuantity() + "/" + item.getAlertSetting() + ")"));
                } catch (Exception e) {
                    System.err.println("Error loading items: " + e.getMessage());
                    e.printStackTrace();
                }

                // Store final references for lambda
                final List<Transaction> finalTransactions = transactions;
                final List<Item> finalItems = items;
                final Map<String, Item> itemMap = finalItems.stream()
                    .collect(Collectors.toMap(Item::getItemID, item -> item));

                Platform.runLater(() -> {
                    try {
                        System.out.println("Updating UI components...");
                        updateKPIs(finalTransactions, itemMap);
                        updateCharts(finalTransactions, itemMap);
                        updateLowStockChart(finalItems);
                        System.out.println("UI update complete");
                    } catch (Exception e) {
                        System.err.println("Error updating UI: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Critical error loading dashboard data: " + e.getMessage());
                    e.printStackTrace();
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
                // Revenue is calculated from the marked-up price in daily sales
                double revenue = tx.getSoldQuantity() * tx.getUnitPrice(); // This is the marked-up price
                totalRevenue += revenue;
                
                // Profit is calculated as (marked-up price - original item price)
                Item item = itemMap.get(tx.getItemID());
                if (item != null) {
                    double originalCost = item.getUnitPrice() * tx.getSoldQuantity(); // Original cost from item table
                    double profit = revenue - originalCost; // Actual profit
                    totalProfit += profit;
                }
            }

            double profitMargin = totalRevenue > 0 ? (totalProfit / totalRevenue) * 100 : 0;

            // Update KPI labels with null checks
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

        // Update progress bars
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
            e.printStackTrace();
        }
    }

    private void updateCharts(List<Transaction> transactions, Map<String, Item> itemMap) {
        updateTrendChart(transactions, itemMap);
        updateProductMix(transactions);
        updateLowStockChart(itemMap.values());
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
            LocalDate date = salesService.getDailySalesHistory(tx.getDailySalesHistoryID()).getCreatedAt();
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
        
        // First pass: calculate total revenue and product metrics
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
            .limit(8)  // Limit to top 8 products for better visibility
            .map(entry -> {
                double percentage = (entry.getValue() / finalTotalRevenue) * 100;
                return new PieChart.Data(
                    String.format("%s\n%.1f%%", entry.getKey(), percentage),
                    entry.getValue()
                );
            })
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
            
        // Update the pie chart
        ordersChart.setData(pieData);
        ordersChart.setTitle(String.format("Total Revenue: $%.2f", totalRevenue));
        
        // Style the pie chart
        String[] colors = {
            "#2ecc71", "#3498db", "#9b59b6", "#e74c3c", 
            "#f1c40f", "#1abc9c", "#e67e22", "#34495e"
        };
        
        for (int i = 0; i < pieData.size(); i++) {
            PieChart.Data data = pieData.get(i);
            data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
            
            // Add hover effect and tooltip
            Tooltip tooltip = new Tooltip(String.format(
                "%s\nRevenue: $%.2f\nShare: %.1f%%",
                data.getName().split("\n")[0],
                data.getPieValue(),
                (data.getPieValue() / finalTotalRevenue) * 100
            ));
            Tooltip.install(data.getNode(), tooltip);
            
            // Add hover effect
            data.getNode().setOnMouseEntered(e -> 
                data.getNode().setStyle("-fx-pie-color: derive(" + colors[pieData.indexOf(data) % colors.length] + ", 30%);")
            );
            data.getNode().setOnMouseExited(e -> 
                data.getNode().setStyle("-fx-pie-color: " + colors[pieData.indexOf(data) % colors.length] + ";")
            );
        }
    }

    private void updateLowStockChart(Collection<Item> items) {
        try {
            if (lowStockChart == null) {
                System.err.println("Low stock chart is null");
                return;
            }

            lowStockChart.getData().clear();
            
            // Create series for different alert levels
            XYChart.Series<String, Number> criticalSeries = new XYChart.Series<>();
            criticalSeries.setName("Critical Stock");
            
            XYChart.Series<String, Number> lowSeries = new XYChart.Series<>();
            lowSeries.setName("Low Stock");
            
            // Get items with low stock and categorize them
            List<Item> stockAlerts = items.stream()
                .filter(item -> {
                    int alertThreshold = item.getAlertSetting() > 0 ? item.getAlertSetting() : 10;
                    return item.getQuantity() < alertThreshold;
                })
                .sorted(Comparator.comparingInt(Item::getQuantity))
                .collect(Collectors.toList());
                
            System.out.println("Found " + stockAlerts.size() + " items with low stock");
                
            // Show only top 5 items in chart
            List<Item> topAlerts = stockAlerts.stream()
                .limit(5)
                .collect(Collectors.toList());

            for (Item item : topAlerts) {
                int quantity = item.getQuantity();
                int alertThreshold = item.getAlertSetting() > 0 ? item.getAlertSetting() : 10;
                
                XYChart.Data<String, Number> data = new XYChart.Data<>(
                    item.getItemName() + "\n(" + quantity + "/" + alertThreshold + ")", 
                    quantity
                );
                
                if (quantity <= alertThreshold * 0.5) {
                    criticalSeries.getData().add(data);
                    System.out.println("Critical: " + item.getItemName() + " (" + quantity + "/" + alertThreshold + ")");
                } else {
                    lowSeries.getData().add(data);
                    System.out.println("Low: " + item.getItemName() + " (" + quantity + "/" + alertThreshold + ")");
                }
            }
            
            // Add series to chart if they have data
            if (!criticalSeries.getData().isEmpty()) lowStockChart.getData().add(criticalSeries);
            if (!lowSeries.getData().isEmpty()) lowStockChart.getData().add(lowSeries);
            
            // Style the series
            criticalSeries.getData().forEach(data -> {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #ff4444;"); // Red for critical
                    setupTooltip(data, "Critical");
                }
            });
            
            lowSeries.getData().forEach(data -> {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #ffa726;"); // Orange for low
                    setupTooltip(data, "Low");
                }
            });
            
            // Update alert count label and notification badge
            Platform.runLater(() -> {
                if (lowStockCountLabel != null) {
                    lowStockCountLabel.setText(stockAlerts.size() + " items need attention");
                    lowStockCountLabel.setStyle("-fx-text-fill: " + 
                        (!criticalSeries.getData().isEmpty() ? "#ff4444" : "#ffa726") + ";");
                }

                // Update notifications list
                notifications.clear();
                for (Item item : stockAlerts) {
                    int quantity = item.getQuantity();
                    int alertThreshold = item.getAlertSetting() > 0 ? item.getAlertSetting() : 10;
                    
                    String alertLevel = quantity <= alertThreshold * 0.5 ? "CRITICAL" : "LOW";
                    
                    notifications.add(String.format(
                        "[%s] Low stock alert: %s (Qty: %d/%d)",
                        alertLevel,
                        item.getItemName(),
                        quantity,
                        alertThreshold
                    ));
                }
                
                // Update notification badge
                hasUnreadNotifications.set(!stockAlerts.isEmpty());
            });
            
        } catch (Exception e) {
            System.err.println("Error updating low stock chart: " + e.getMessage());
            e.printStackTrace();
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

        // Group by date and calculate metrics
        Map<LocalDate, RevenueMetrics> dailyMetrics = new TreeMap<>();

        try {
            QueryBuilder<DailySalesHistory> qb = new QueryBuilder<>(DailySalesHistory.class);
            for (Transaction t : transactions) {
                ArrayList<DailySalesHistory> historyData = qb
                        .select()
                        .from("db/DailySalesHistory.txt")
                        .where("dailySalesHistoryID", "=", t.getDailySalesHistoryID())
                        .getAsObjects();
                LocalDate date = historyData.getFirst().getCreatedAt();
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

    private void updateInventoryHealth(List<Transaction> transactions) {
        inventoryHealthMap.getData().clear();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Inventory Health");

        // Calculate metrics for each item
        for (Item item : loadItems()) {
            double stockLevel = item.getQuantity();
            double demand = calculateDemand(item.getItemID(), transactions);
            double turnover = calculateStockTurnover(item, transactions);
            
            // Create bubble - X: Stock Level, Y: Demand, Size: Turnover
            series.getData().add(new XYChart.Data<>(stockLevel, demand, turnover));
        }

        inventoryHealthMap.getData().add(series);
        
        // Update summary labels
        updateInventoryMetricLabels();
    }

    private void updateInventoryMetricLabels() {
        try {
            List<Item> items = loadItems();
            
            // Calculate overall metrics
            double avgTurnover = items.stream()
                .mapToDouble(item -> {
                    try {
                        return calculateStockTurnover(item, loadTransactionsForCharting());
                    } catch (Exception e) {
                        System.err.println("Error calculating turnover for item " + item.getItemID() + ": " + e.getMessage());
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
                
            double avgDays = items.stream()
                .mapToDouble(item -> {
                    try {
                        return calculateInventoryDays(item, loadTransactionsForCharting());
        } catch (Exception e) {
                        System.err.println("Error calculating inventory days for item " + item.getItemID() + ": " + e.getMessage());
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
                
            double avgRisk = items.stream()
                .mapToDouble(item -> {
                    try {
                        return calculateStockoutRisk(item, loadTransactionsForCharting());
                    } catch (Exception e) {
                        System.err.println("Error calculating stockout risk for item " + item.getItemID() + ": " + e.getMessage());
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
            
            stockTurnoverLabel.setText(String.format("%.1f", avgTurnover));
            avgInventoryLabel.setText(String.format("%.0f", avgDays));
            stockoutRiskLabel.setText(String.format("%.1f%%", avgRisk));
        } catch (Exception e) {
            System.err.println("Error updating inventory metrics: " + e.getMessage());
            stockTurnoverLabel.setText("N/A");
            avgInventoryLabel.setText("N/A");
            stockoutRiskLabel.setText("N/A");
        }
    }

    private double calculateDemand(String itemId, List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getItemID().equals(itemId))
            .mapToInt(Transaction::getSoldQuantity)
            .sum();
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

    private void checkForNotifications(List<Transaction> transactions, List<Item> items) {
        notifications.clear();

        // Check for low stock items
        items.stream()
            .filter(i -> i.getQuantity() < 10)
            .forEach(i -> {
                String alertLevel = i.getQuantity() <= 5 ? "CRITICAL" : "LOW";
                notifications.add(String.format(
                    "[%s] Low stock alert: %s (Qty: %d)",
                    alertLevel,
                    i.getItemName(),
                    i.getQuantity()
                ));
            });

        // Check for items that need reordering based on sales velocity
        Map<String, Integer> salesVelocity = new HashMap<>();
        transactions.forEach(tx -> 
            salesVelocity.merge(tx.getItemID(), tx.getSoldQuantity(), Integer::sum)
        );

        items.forEach(item -> {
            int avgDailySales = salesVelocity.getOrDefault(item.getItemID(), 0) / 30; // Average over 30 days
            if (avgDailySales > 0) {
                int daysOfStock = item.getQuantity() / avgDailySales;
                if (daysOfStock < 7) { // Less than a week of stock
                    notifications.add(String.format(
                        "[REORDER] %s - Only %d days of stock remaining",
                        item.getItemName(),
                        daysOfStock
                    ));
                }
            }
        });

        hasUnreadNotifications.set(!notifications.isEmpty());
        notificationBadge.setVisible(hasUnreadNotifications.get());
    }

    private List<Transaction> loadTransactions() throws Exception {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        return new QueryBuilder<>(Transaction.class)
                .select()
                .from("db/Transaction.txt")
                .where("transactionDate", ">=", start.toString())
                .and("transactionDate", "<=", end.plusDays(1).toString())
                .getAsObjects();
    }

    private List<PurchaseOrder> loadPurchaseOrders() throws Exception {
        return new QueryBuilder<>(PurchaseOrder.class)
                .select()
                .from("db/PurchaseOrder.txt")
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
                        item.setUnitPrice(Double.parseDouble(data.getOrDefault("price", "0")));
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
    private void handleExport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Performance Data");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            File file = fileChooser.showSaveDialog(salesManagerDashboardPane.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("Date,Revenue,Profit,Orders,Margin %");

                    LocalDate start = startDatePicker.getValue();
                    LocalDate end = endDatePicker.getValue();
                    List<Transaction> transactions = loadTransactionsForCharting();
                    Map<String, Item> itemMap = loadItems().stream()
                        .collect(Collectors.toMap(Item::getItemID, item -> item));

                    Map<String, DailyMetrics> dailyMetrics = new TreeMap<>();

                    for (Transaction tx : transactions) {
                        String date = tx.getDailySalesHistoryID();
                        double revenue = tx.getSoldQuantity() * tx.getUnitPrice();
                        double profit = 0;

                        Item item = itemMap.get(tx.getItemID());
                        if (item != null) {
                            double cost = item.getUnitPrice() * tx.getSoldQuantity();
                            profit = revenue - cost;
                        }

                        dailyMetrics.computeIfAbsent(date, k -> new DailyMetrics())
                            .add(revenue, profit);
                    }

                    dailyMetrics.forEach((date, metrics) -> {
                        double margin = metrics.revenue > 0 ? (metrics.profit / metrics.revenue) * 100 : 0;
                        writer.printf("%s,%.2f,%.2f,%d,%.1f%n",
                            date, metrics.revenue, metrics.profit, metrics.orders, margin);
                    });

                    showNotification("Data exported successfully", NotificationController.popUpType.success);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error exporting data", NotificationController.popUpType.error);
        }
    }

    private void filterSalesTable() {
        System.out.println("filter sales table");
        String statusText = statusFilter.getValue();

        filteredSales.setPredicate(record ->
            (statusText == null || statusText.equals("All") ||
                record.getStatus().equalsIgnoreCase(statusText))
        );

    }

    private void refreshData() {
        loadDashboardData();
    }

    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "completed" -> "status-completed";
            case "pending" -> "status-pending";
            case "cancelled" -> "status-cancelled";
            default -> "status-default";
        };
    }

    @FXML
    private void showNotifications() {
        if (notifications.isEmpty()) {
            showNotification("No new notifications", NotificationController.popUpType.info);
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
                
                // Position the panel on the right side with proper margins
                StackPane.setAlignment(notificationPanel, Pos.CENTER_RIGHT);
                StackPane.setMargin(notificationPanel, new Insets(20, 20, 20, 20));
                notificationOverlay.getChildren().add(notificationPanel);
                
                // Add to scene if not already added
                if (!salesManagerDashboardPane.getChildren().contains(notificationOverlay)) {
                    salesManagerDashboardPane.getChildren().add(notificationOverlay);
                }
            }
            
            // Clear previous content
            notificationPanel.getChildren().clear();
            
            // Header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label titleLabel = new Label("Inventory Alerts");
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
            
            VBox alertsContainer = new VBox(10);
            alertsContainer.setStyle("-fx-padding: 10 0;");
            
            for (String notification : notifications) {
                HBox alertBox = createAlertBox(notification);
                alertsContainer.getChildren().add(alertBox);
            }
            
            scrollPane.setContent(alertsContainer);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
            // Footer with actions
            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: transparent #ddd transparent transparent; -fx-border-width: 1;");
            
            Button exportButton = new Button("Export Alerts");
            exportButton.setStyle(
                "-fx-background-color: #2196F3;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 4;"
            );
            exportButton.setOnAction(e -> exportAlerts());
            
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
            
            // Add all components to panel
            notificationPanel.getChildren().addAll(header, scrollPane, footer);
            
            // Show with slide animation
            notificationOverlay.setVisible(true);
            notificationPanel.setTranslateX(notificationPanel.getWidth());
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationPanel);
            slideIn.setFromX(notificationPanel.getWidth());
            slideIn.setToX(0);
            slideIn.play();
            
        } catch (Exception e) {
            System.err.println("Error showing notifications dialog: " + e.getMessage());
            e.printStackTrace();
            showNotification("Error showing notifications", NotificationController.popUpType.error);
        }
    }

    private HBox createAlertBox(String notification) {
        HBox alertBox = new HBox(10);
        alertBox.setAlignment(Pos.CENTER_LEFT);
        alertBox.setPadding(new Insets(10));
        alertBox.setStyle(
            "-fx-background-color: " + 
            (notification.contains("CRITICAL") ? "#fff5f5" : 
             notification.contains("REORDER") ? "#fff8e1" : "#f5f5f5") +
            ";" +
            "-fx-background-radius: 4;"
        );
        
        Circle icon = new Circle(8);
        icon.setFill(javafx.scene.paint.Color.web(
            notification.contains("CRITICAL") ? "#ff4444" :
            notification.contains("REORDER") ? "#ffa726" : "#666666"
        ));
        
        Label label = new Label(notification);
        label.setWrapText(true);
        label.setStyle(
            "-fx-text-fill: " +
            (notification.contains("CRITICAL") ? "#ff4444" :
             notification.contains("REORDER") ? "#f57c00" : "#333333") +
            ";" +
            (notification.contains("CRITICAL") ? "-fx-font-weight: bold;" : "")
        );
        
        alertBox.getChildren().addAll(icon, label);
        return alertBox;
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

    private void exportAlerts() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Stock Alerts");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            File file = fileChooser.showSaveDialog(salesManagerDashboardPane.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("Alert Type,Item Name,Details,Date");
                    LocalDateTime now = LocalDateTime.now();
                    
                    for (String notification : notifications) {
                        String type = notification.contains("CRITICAL") ? "CRITICAL" :
                                    notification.contains("REORDER") ? "REORDER" : "LOW";
                        
                        String[] parts = notification.split(":");
                        if (parts.length >= 2) {
                            writer.printf("%s,%s,%s,%s%n",
                                type,
                                parts[1].trim().split("\\(")[0].trim(),
                                parts[1].trim(),
                                now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            );
                        }
                    }
                    showNotification("Alerts exported successfully", NotificationController.popUpType.success);
                }
            }
        } catch (Exception e) {
            showNotification("Error exporting alerts", NotificationController.popUpType.error);
        }
    }

    private void showNotification(String message, NotificationController.popUpType type) {
        try {
            new NotificationView(message, type, NotificationController.popUpPos.BOTTOM_RIGHT).show();
        } catch (Exception e) {
            e.printStackTrace();
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

    private void updateWelcomeMessage() {
        try {
            // Get username from SessionManager if available
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

    @FXML
    private void handleForecast() {
        try {
            // Create custom notification-style overlay
            StackPane overlay = new StackPane();
            overlay.setId("forecastOverlay");
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            overlay.prefWidthProperty().bind(salesManagerDashboardPane.widthProperty());
            overlay.prefHeightProperty().bind(salesManagerDashboardPane.heightProperty());

            // Create forecast box
            VBox forecastBox = new VBox(15);
            forecastBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-padding: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 4);");
            forecastBox.setMaxWidth(800);
            forecastBox.setMaxHeight(600);

            // Header with icon and title
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/chart.png")));
            icon.setFitHeight(32);
            icon.setFitWidth(32);

            Label title = new Label("Sales Forecast");
            title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

            Button closeButton = new Button();
            closeButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/close.png"))));
            closeButton.setStyle("-fx-background-color: transparent;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            header.getChildren().addAll(icon, title, spacer, closeButton);

            // Controls
            HBox controls = new HBox(10);
            controls.setAlignment(Pos.CENTER_LEFT);
            
            ComboBox<String> periodCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Next 7 Days", "Next 30 Days", "Next 90 Days", "Next Year"
            ));
            periodCombo.setValue("Next 30 Days");
            
            ComboBox<String> methodCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Linear Regression", "Moving Average", "Exponential Smoothing"
            ));
            methodCombo.setValue("Linear Regression");
            
            Button generateBtn = new Button("Generate Forecast");
            generateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            
            controls.getChildren().addAll(periodCombo, methodCombo, generateBtn);

            // Chart
            LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
            chart.setTitle("Sales Forecast");
            chart.setAnimated(false);
            
            // Add components to forecast box
            forecastBox.getChildren().addAll(header, controls, chart);

            // Add to overlay
            overlay.getChildren().add(forecastBox);
            StackPane.setAlignment(forecastBox, Pos.CENTER);

            // Event handlers
            closeButton.setOnAction(e -> salesManagerDashboardPane.getChildren().remove(overlay));
            generateBtn.setOnAction(e -> generateForecast(chart, periodCombo.getValue(), methodCombo.getValue()));

            // Show overlay
            if (!salesManagerDashboardPane.getChildren().contains(overlay)) {
                salesManagerDashboardPane.getChildren().add(overlay);
            }
        } catch (Exception e) {
            System.err.println("Error showing forecast: " + e.getMessage());
            e.printStackTrace();
            showNotification("Error", NotificationController.popUpType.error);
        }
    }


    private void generateForecast(LineChart<String, Number> chart, String period, String method) {
        try {
            // Clear previous data
            chart.getData().clear();

            // Load historical data
            List<Transaction> transactions = loadTransactionsForCharting();
            Map<LocalDate, Double> historicalData = new TreeMap<>();
            
            // Process historical data
            for (Transaction t : transactions) {
                LocalDate date = LocalDate.parse(t.getDailySalesHistoryID());
                double amount = t.getSoldQuantity() * t.getUnitPrice();
                historicalData.merge(date, amount, Double::sum);
            }

            // Generate forecast data
            XYChart.Series<String, Number> historicalSeries = new XYChart.Series<>();
            historicalSeries.setName("Historical");
            
            XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
            forecastSeries.setName("Forecast");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
            
            // Add historical data
            historicalData.forEach((date, value) -> 
                historicalSeries.getData().add(new XYChart.Data<>(date.format(fmt), value))
            );

            // Calculate forecast period
            int days = switch (period) {
                case "Next 7 Days" -> 7;
                case "Next 30 Days" -> 30;
                case "Next 90 Days" -> 90;
                default -> 365;
            };

            // Generate forecast data
            LocalDate lastDate = historicalData.keySet().stream()
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

            Map<LocalDate, Double> forecastData = new TreeMap<>();
            
            for (int i = 1; i <= days; i++) {
                LocalDate forecastDate = lastDate.plusDays(i);
                double forecastValue = calculateForecastValue(historicalData, forecastDate, method);
                forecastData.put(forecastDate, forecastValue);
                forecastSeries.getData().add(new XYChart.Data<>(forecastDate.format(fmt), forecastValue));
            }

            chart.getData().addAll(historicalSeries, forecastSeries);

            // Update forecast metrics
            updateForecastMetrics(historicalData, forecastData);
            
            // Enable export functionality
            Button exportBtn = new Button("Export Forecast");
            exportBtn.setOnAction(e -> exportForecast(historicalData, forecastData));
            
            // Add export button to chart
            VBox chartControls = (VBox) chart.getParent();
            if (!chartControls.getChildren().contains(exportBtn)) {
                chartControls.getChildren().add(exportBtn);
            }

        } catch (Exception e) {
            System.err.println("Error generating forecast: " + e.getMessage());
            e.printStackTrace();
            showNotification("Error", NotificationController.popUpType.error);
        }
    }

    private void updateForecastMetrics(Map<LocalDate, Double> historicalData, Map<LocalDate, Double> forecastData) {
        try {
            // Calculate predicted revenue (sum of forecast values)
            double predictedRevenue = forecastData.values().stream().mapToDouble(Double::doubleValue).sum();
            
            // Calculate growth rate
            double historicalAvg = historicalData.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double forecastAvg = forecastData.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double growthRate = ((forecastAvg - historicalAvg) / historicalAvg) * 100;
            
            // Calculate confidence score (simplified)
            double confidence = calculateConfidenceScore(historicalData, forecastData);
            
            // Update labels
            if (predictedRevenueLabel != null) {
                predictedRevenueLabel.setText(String.format("$%,.2f", predictedRevenue));
            }
            if (growthRateLabel != null) {
                growthRateLabel.setText(String.format("%.1f%%", growthRate));
            }
            if (confidenceLabel != null) {
                confidenceLabel.setText(String.format("%.0f%%", confidence * 100));
            }
        } catch (Exception e) {
            System.err.println("Error updating forecast metrics: " + e.getMessage());
        }
    }

    private double calculateConfidenceScore(Map<LocalDate, Double> historicalData, Map<LocalDate, Double> forecastData) {
        try {
            // Calculate mean absolute percentage error (MAPE)
            double totalError = 0.0;
            int count = 0;
            
            // Use last 7 days of historical data to compare with first 7 days of forecast
            List<Double> historicalValues = new ArrayList<>(historicalData.values());
            List<Double> forecastValues = new ArrayList<>(forecastData.values());
            
            int compareSize = Math.min(7, Math.min(historicalValues.size(), forecastValues.size()));
            
            for (int i = 0; i < compareSize; i++) {
                double actual = historicalValues.get(historicalValues.size() - compareSize + i);
                double forecast = forecastValues.get(i);
                
                if (actual != 0) {
                    totalError += Math.abs((actual - forecast) / actual);
                    count++;
                }
            }
            
            double mape = count > 0 ? totalError / count : 1.0;
            return Math.max(0.0, Math.min(1.0, 1.0 - mape));
            
        } catch (Exception e) {
            System.err.println("Error calculating confidence score: " + e.getMessage());
            return 0.5; // Return medium confidence in case of error
        }
    }

    private void exportForecast(Map<LocalDate, Double> historicalData, Map<LocalDate, Double> forecastData) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Forecast Data");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            File file = fileChooser.showSaveDialog(salesManagerDashboardPane.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("Date,Type,Amount");
                    
                    // Write historical data
                    historicalData.forEach((date, amount) -> 
                        writer.printf("%s,Historical,%.2f%n", date, amount)
                    );
                    
                    // Write forecast data
                    forecastData.forEach((date, amount) -> 
                        writer.printf("%s,Forecast,%.2f%n", date, amount)
                    );
                    
                    showNotification("Forecast data exported successfully", NotificationController.popUpType.success);
                }
            }
        } catch (Exception e) {
            showNotification("Error exporting forecast data", NotificationController.popUpType.error);
        }
    }

    private double calculateForecastValue(Map<LocalDate, Double> historicalData, LocalDate forecastDate, String method) {
        // Implement different forecasting methods
        switch (method) {
            case "Linear Regression":
                return calculateLinearRegression(historicalData, forecastDate);
            case "Moving Average":
                return calculateMovingAverage(historicalData);
            case "Exponential Smoothing":
                return calculateExponentialSmoothing(historicalData);
            default:
                return 0.0;
        }
    }

    private double calculateLinearRegression(Map<LocalDate, Double> historicalData, LocalDate forecastDate) {
        // Simple linear regression implementation
        List<Double> values = new ArrayList<>(historicalData.values());
        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return avg * (1 + 0.1); // Simple 10% growth projection
    }

    private double calculateMovingAverage(Map<LocalDate, Double> historicalData) {
        return historicalData.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }

    private double calculateExponentialSmoothing(Map<LocalDate, Double> historicalData) {
        double alpha = 0.3; // Smoothing factor
        double lastValue = historicalData.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        return lastValue * (1 + alpha);
    }

    private static class SalesVelocityMetric {
        private int count = 0;
        private double totalAmount = 0;
        
        void addSale(double amount) {
            count++;
            totalAmount += amount;
        }
        
        int getCount() { return count; }
        double getAverageAmount() { return count > 0 ? totalAmount / count : 0; }
    }

    private static class CategoryPerformanceMetric {
        private double revenue = 0;
        private int unitsSold = 0;
        private static final double MARGIN_RATE = 0.3;
        
        void addSale(double amount, int quantity) {
            revenue += amount;
            unitsSold += quantity;
        }
        
        double getRevenue() { return revenue; }
        int getUnitsSold() { return unitsSold; }
        double getMargin() { return revenue * MARGIN_RATE; }
    }

    private double calculateStockTurnover(Item item, List<Transaction> transactions) {
        // Calculate stock turnover rate (COGS / Average Inventory)
        double soldQuantity = transactions.stream()
            .filter(t -> t.getItemID().equals(item.getItemID()))
            .mapToInt(Transaction::getSoldQuantity)
            .sum();
            
        double averageInventory = item.getQuantity() / 2.0; // Simplified calculation
        return averageInventory > 0 ? soldQuantity / averageInventory : 0;
    }

    private double calculateInventoryDays(Item item, List<Transaction> transactions) {
        // Calculate days of inventory on hand
        double dailySales = transactions.stream()
            .filter(t -> t.getItemID().equals(item.getItemID()))
            .mapToInt(Transaction::getSoldQuantity)
            .average()
            .orElse(0.0);
            
        return dailySales > 0 ? item.getQuantity() / dailySales : 0;
    }

    private double calculateStockoutRisk(Item item, List<Transaction> transactions) {
        // Calculate stockout risk based on inventory level and sales velocity
        double avgDailySales = transactions.stream()
            .filter(t -> t.getItemID().equals(item.getItemID()))
            .mapToInt(Transaction::getSoldQuantity)
            .average()
            .orElse(0.0);
            
        double daysOfStock = avgDailySales > 0 ? item.getQuantity() / avgDailySales : 0;
        double riskThreshold = 7.0; // 7 days of stock
        
        // Risk increases as days of stock decreases
        return Math.max(0, Math.min(100, (1 - (daysOfStock / riskThreshold)) * 100));
    }

    private String getCategoryFromItem(Item item) {
        try {
            QueryBuilder<Item> qb = new QueryBuilder<>(Item.class);
            ArrayList<HashMap<String, String>> result = qb
                .select(new String[]{"category"})
                .from("db/Item.txt")
                .where("itemID", "=", item.getItemID())
                .get();
                
            if (!result.isEmpty()) {
                return result.get(0).get("category");
            }
        } catch (Exception e) {
            System.err.println("Error getting category for item " + item.getItemID() + ": " + e.getMessage());
        }
        return "Uncategorized";
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

    public static class ProductPerformance {
        private final String productName;
        private double revenue = 0;
        private double profit = 0;
        private double margin = 0;

        public ProductPerformance(String productName) {
            this.productName = productName;
        }

        public void addSale(double revenue, double profit) {
            this.revenue += revenue;
            this.profit += profit;
            this.margin = this.revenue > 0 ? this.profit / this.revenue : 0;
        }

        public String getProductName() { return productName; }
        public double getRevenue() { return revenue; }
        public double getProfit() { return profit; }
        public double getMargin() { return margin; }
    }
}