package controllers.InventoryController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import models.Datas.InventoryUpdateLog;
import models.Datas.Item;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class StockReportGenerationController implements Initializable {

    @FXML
    private Button btnGenerateReport;

    @FXML
    private VBox jasperViewerContainer;

    private String username = "TestingUser";

    public void initialize(URL url, ResourceBundle resourceBundle) {

        btnGenerateReport.setOnAction(event -> {
            generateStockReport();
        });

    }

    public void generateStockReport() {
        try {
//            HashMap<Item, String> items = Item.getItemsWithSupplier();
//
//            List<Map<String, Object>> itemList = new ArrayList<>();
//
//            for (Map.Entry<Item, String> entry : items.entrySet()) {
//                Map<String, Object> row = new HashMap<>();
//                Item item = entry.getKey();
//
//                row.put("itemID", item.getItemID());
//                row.put("itemName", item.getItemName());
//                row.put("createdAt", item.getCreatedAt());
//                row.put("updatedAt", item.getUpdatedAt());
//                row.put("alertSetting", item.getAlertSetting());
//                row.put("quantity", item.getQuantity());
//                row.put("supplierName", entry.getValue());
//
//                itemList.add(row);
//            }

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


