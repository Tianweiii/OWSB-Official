package controllers.InventoryController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import models.Datas.InventoryUpdateLog;
import models.Datas.Item;
import models.Utils.QueryBuilder;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import javafx.geometry.Insets;

public class InventoryViewController implements Initializable {

    @FXML
    private Button btnGenerateReport;

    @FXML
    private VBox lowStockItems;

    @FXML
    private VBox pendingPurchaseOrder;

    @FXML
    private BarChart<String, Number> recentStockUpdates;

    @FXML
    private Label txtTotalQtyInStock;

    @FXML
    private Label txtTotalValueOfStock;

    @FXML
    private Label txtUser;

    private String username = "TestingUser";

    @FXML

    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtUser.setText(username);
        ArrayList<HashMap<String, String>> itemList = null;
        try {
            itemList = Item.getItems();
            loadTotalQuantityInStock(itemList);
            loadTotalValueOfStock(itemList);
            loadLowStockItems(itemList);
            loadRecentStockUpdates(itemList);

        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        loadPendingPurchaseOrders();

        btnGenerateReport.setOnAction(e -> generateStockReport());
    }

    private void loadTotalQuantityInStock(ArrayList<HashMap<String, String>> itemList) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        int totalQty = 0;
        for (HashMap<String, String> item : itemList) {
            totalQty += Integer.parseInt(item.get("quantity"));
        }
        txtTotalQtyInStock.setText(String.valueOf(totalQty));
    }

    private void loadTotalValueOfStock(ArrayList<HashMap<String, String>> itemList) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        double totalValue = 0;

        for (HashMap<String, String> item : itemList) {
            try {
                totalValue += Double.parseDouble(item.get("unitPrice")) * Integer.parseInt(item.get("quantity"));
            } catch (NumberFormatException | NullPointerException e) {
                System.err.println("Error" + e);
            }
        }
        txtTotalValueOfStock.setText(String.format("RM %.2f", totalValue));
    }

    private void loadLowStockItems(ArrayList<HashMap<String, String>> itemList) {
        lowStockItems.getChildren().clear();

        for (HashMap<String, String> item : itemList) {
            if (Integer.parseInt(item.get("quantity")) < Integer.parseInt(item.get("alertSetting"))) {
                VBox tile = new VBox();
                tile.setPadding(new Insets(10, 10, 10, 10));
                tile.setSpacing(5);
                tile.setStyle(
                        "-fx-background-color: #f8d7da;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-radius: 10;" +
                                "-fx-border-color: #f5c2c7;" +
                                "-fx-border-width: 1;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
                );

                Label nameLabel = new Label(item.get("itemID") + " " + item.get("itemName"));
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                Label qtyLabel = new Label("Quantity: " + item.get("quantity"));
                qtyLabel.setStyle("-fx-font-size: 12;");

                tile.getChildren().addAll(nameLabel, qtyLabel);
                lowStockItems.getChildren().add(tile);
//                Label lowStockItem = new Label(item.get("itemID") + item.get("itemName") + " - Qty: " + item.get("quantity"));
//                lowStockItem.setStyle("-fx-font-size: 14; -fx-padding: 5;");
//                lowStockItems.getChildren().add(lowStockItem);
            }
        }

        lowStockItems.setSpacing(10);
        lowStockItems.setPadding(new Insets(10));
    }

    private void loadPendingPurchaseOrders() {
        pendingPurchaseOrder.getChildren().clear();
    }

    private void loadRecentStockUpdates(ArrayList<HashMap<String, String>> itemList) {
        recentStockUpdates.getData().clear();

        List<HashMap<String, String>> sortedItems = itemList.stream()
                .filter(item -> item.get("updatedAt") != null && !item.get("updatedAt").isEmpty())
                .sorted((a, b) -> {
                    LocalDateTime dateA = Item.formatDateTime(a.get("updatedAt"));
                    LocalDateTime dateB = Item.formatDateTime(b.get("updatedAt"));
                    return dateB.compareTo(dateA);
                })
                .limit(10)
                .toList();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Recent Stock Updates");
        int index = 0;

        for (HashMap<String, String> item : sortedItems) {
            String itemLabel = item.get("itemName");
            int quantity = Integer.parseInt(item.get("quantity"));
            XYChart.Data<String, Number> data = new XYChart.Data<>(itemLabel, quantity);
//            series.getData().add(new XYChart.Data<>(itemLabel, quantity));

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if(newNode != null) {
                    newNode.setStyle("-fx-bar-fill: lightblue;");
                }
            });

            series.getData().add(data);
            index++;

        }

        recentStockUpdates.getData().add(series);
    }

    public void generateStockReport() {
        try {
            ArrayList<HashMap<String, String>> items = Item.getItems();
            ArrayList<HashMap<String, String>> inventoryUpdateLog = InventoryUpdateLog.getInventoryUpdateLog();

            List<Map<String, Object>> reportData = new ArrayList<>();

            for (HashMap<String, String> item : items) {
                String itemID = item.get("itemID");
                int currentQty = Integer.parseInt(item.get("quantity"));
                int alertSetting = Integer.parseInt(item.get("alertSetting"));
                Double unitPrice = Double.parseDouble(item.get("unitPrice"));

                int stockIn = 0;
                int stockOut = 0;

                for (HashMap<String, String> log : inventoryUpdateLog) {
                    if (Objects.equals(log.get("itemID"), itemID)) {
                        int prev = Integer.parseInt(log.get("prevQuantity"));
                        int now = Integer.parseInt(log.get("newQuantity"));
                        int diff = now - prev;

                        if (diff > 0) stockIn += diff;
                        else stockOut += Math.abs(diff);
                    }
                }

                String status = (currentQty < alertSetting) ? "LOW STOCK" : (currentQty == 0) ? "OUT OF STOCK" : "IN STOCK";

                Map<String, Object> row = new HashMap<>();
                row.put("itemID", itemID);
                row.put("itemName", item.get("itemName"));
                row.put("quantity", currentQty);
                row.put("stockIn", stockIn);
                row.put("stockOut", stockOut);
                row.put("alertSetting", alertSetting);
                row.put("status", status);
                row.put("unitPrice", unitPrice);

                reportData.add(row);
            }

            long numLowStock = reportData.stream().filter(row -> {
                String status = (String) row.get("status");
                return "LOW STOCK".equals(status) || "OUT OF STOCK".equals(status);
            }).count();
            int totalAvailableQty = reportData.stream().mapToInt(row -> row.get("quantity") == null ? 0 : (Integer) row.get("quantity")).sum();
            double totalInventoryValue = reportData.stream().mapToDouble(row -> {
                Integer qty = (Integer) row.get("quantity");
                Double price = (Double) row.get("unitPrice");
                return (qty == null ? 0 : qty) * (price == null ? 0.00 : price);
            }).sum();

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ITEM_DATA_SOURCE", dataSource); // dataset name
            parameters.put("IMG_PATH", getClass().getResource("/Assets/image/inventoryPic.png").toString()); // image
            parameters.put("CREATED_USER", username);
            parameters.put("NUM_LOW_STOCK", numLowStock);
            parameters.put("TOTAL_AVAILABLE_QTY", totalAvailableQty);
            parameters.put("TOTAL_INVENTORY_VALUE", totalInventoryValue);

            InputStream reportStream = getClass().getResourceAsStream("/Jasper/StockReport.jasper");
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, new JREmptyDataSource());

            Path tmpDir = Paths.get(System.getProperty("user.dir"), "tmp");
            Files.createDirectories(tmpDir);
            Path pdfPath = tmpDir.resolve("Stock_Report.pdf");
            Files.createDirectories(pdfPath.getParent());
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath.toString());

            System.out.println("File path" + pdfPath);
            System.out.println("Report generated successfully!");

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfPath.toFile());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
