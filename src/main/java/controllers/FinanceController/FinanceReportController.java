package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.DTO.SalesItemDTO;
import models.Datas.FinanceReportHeaderData;
import models.Datas.Item;
import models.Datas.Payment;
import models.Datas.Transaction;
import models.Utils.FileIO;
import models.Utils.Helper;
import models.Utils.Navigator;
import models.Utils.SessionManager;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class FinanceReportController implements Initializable {

    @FXML
    private VBox paymentsContainer;
    @FXML
    private VBox salesContainer;
    @FXML
    private Text totalCostField;
    @FXML
    private Text totalPendingField;
    @FXML
    private Text totalSalesField;
    @FXML
    private Text totalTransactionsField;

    @FXML
    private LineChart<String, Number> lineChart;

    Navigator navigator = Navigator.getInstance();
    private FinanceMainController mainController;

    private TreeSet<String> allMonths = new TreeSet<>(Comparator.comparing(m -> LocalDate.parse(m + "-01", DateTimeFormatter.ofPattern("yyyy MMM-dd"))));
    private Map<String, Double> revenueMap = new HashMap<>();
    private Map<String, Double> costMap = new HashMap<>();
    private Map<String, Double> priceMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            populatePaymentsContainer();
            populateSalesContainer();
            initLineChart();

            totalSalesField.setText("RM " + String.format("%.2f", getTotalSales()));
            totalTransactionsField.setText(String.valueOf(getTotalTransactions()));
            totalPendingField.setText("RM " + String.format("%.2f", getTotalPendingPayments()));
            totalCostField.setText("RM " + String.format("%.2f", getTotalCost()));

        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void viewAllPayments() {
        navigator.navigate(navigator.getRouters("finance").getRoute("viewAllPayments"));
    }

    public void viewAllSales() {
        navigator.navigate(navigator.getRouters("finance").getRoute("viewAllSales"));
    }

    private void populatePaymentsContainer() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ArrayList<Payment> list = FileIO.getObjectsFromXLines(Payment.class, "Payment", 3);
        for (Payment i : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/ReportPaymentItem.fxml"));
                Parent card = loader.load();

                ReportPaymentItemController controller = loader.getController();
                controller.setData(i);

                paymentsContainer.getChildren().add(card);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void populateSalesContainer() throws IOException, ReflectiveOperationException {
        double salesMult = 1.15;
        ArrayList<SalesItemDTO> salesItemDTOs = new ArrayList<>();
        ArrayList<Transaction> transactions = FileIO.getObjectsFromXLines(Transaction.class, "Transaction", 3);

        for (Transaction i : transactions) {
            Item item = FileIO.getIDsAsObject(Item.class, "Item", i.getItemID());

            double amount = Math.round((i.getSoldQuantity() * item.getUnitPrice()) * salesMult * 100.0) / 100.0;

            salesItemDTOs.add(new SalesItemDTO(item.getItemName(), i.getSoldQuantity(), amount));
        }

        // get sales item
        for (SalesItemDTO i : salesItemDTOs) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/ReportPaymentItem.fxml"));
                Parent card = loader.load();

                ReportPaymentItemController controller = loader.getController();
                controller.setData(i);

                salesContainer.getChildren().add(card);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public double getTotalSales() throws IOException {
        Map<String, Double> priceMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Item.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String itemId = parts[0].trim();
                double price = Double.parseDouble(parts[6].trim());
                priceMap.put(itemId, price);
            }
        }

        double totalSales = 0.0;
        try (BufferedReader transReader = new BufferedReader(new FileReader("src/main/java/db/Transaction.txt"))) {
            String line;
            while ((line = transReader.readLine()) != null) {
                String[] parts = line.split(",");
                String itemId = parts[3].trim();
                int quantity = Integer.parseInt(parts[2].trim());

                Double price = priceMap.get(itemId);
                totalSales += price * quantity;
            }
        }

        return totalSales;
    }

    public int getTotalTransactions() throws IOException {
        return FileIO.getRowCount("Transaction");
    }

    public double getTotalPendingPayments() throws IOException {
        return FileIO.getCountOfX("PurchaseOrder", 5, 4, "verified");
    }

    public double getTotalCost() throws IOException {
        return FileIO.getCountOfX("Payment", 3);
    }

    public void initLineChart() {
        double priceMultiplier = 1.15;
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE;
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy MMM");

//            // Revenue: Month -> Total Sales
//            revenueMap = new HashMap<>();
//
//            // Cost: Month -> Total Payments
//            costMap = new HashMap<>();

            // Parse item prices
//            Map<String, Double> priceMap = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Item.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    double rawPrice = Double.parseDouble(parts[6].trim()) * priceMultiplier;
                    double roundedPrice = Math.round(rawPrice * 100.0) / 100.0;
                    priceMap.put(parts[0].trim(), roundedPrice);
                }
            }

            // Aggregate revenue
            try (BufferedReader transReader = new BufferedReader(new FileReader("src/main/java/db/Transaction.txt"));
                 BufferedReader salesReader = new BufferedReader(new FileReader("src/main/java/db/Sales.txt"))) {

                Map<String, String> salesDateMap = new HashMap<>(); // SalesID -> CreatedDate

                String line;
                while ((line = salesReader.readLine()) != null) {
                    String[] parts = line.split(",");
                    salesDateMap.put(parts[0], parts[1]);
                }

                while ((line = transReader.readLine()) != null) {
                    String[] parts = line.split(",");
                    String salesId = parts[4];
                    String itemId = parts[3];
                    int quantity = Integer.parseInt(parts[2].trim());

                    String rawDate = salesDateMap.get(salesId).trim();
                    LocalDate date = LocalDate.parse(rawDate, inputFormatter);
                    String month = date.format(monthFormatter);

                    double amount = priceMap.get(itemId) * quantity;
                    BigDecimal newRevenue = BigDecimal.valueOf(revenueMap.getOrDefault(month, 0.0))
                            .add(BigDecimal.valueOf(amount))
                            .setScale(2, RoundingMode.HALF_UP);
                    revenueMap.put(month, newRevenue.doubleValue());

                }
            }

            // Aggregate cost
            try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Payment.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    LocalDate date = LocalDate.parse(parts[5].trim(), inputFormatter);
                    String month = date.format(monthFormatter);
                    double amount = Double.parseDouble(parts[3].trim());
                    BigDecimal newCost = BigDecimal.valueOf(costMap.getOrDefault(month, 0.0))
                            .add(BigDecimal.valueOf(amount))
                            .setScale(2, RoundingMode.HALF_UP);
                    costMap.put(month, newCost.doubleValue());

                }
            }

            // Collect all months from both maps
//            TreeSet<String> allMonths = new TreeSet<>(Comparator.comparing(m -> LocalDate.parse(m + "-01", DateTimeFormatter.ofPattern("yyyy MMM-dd"))));
            allMonths.addAll(revenueMap.keySet());
            allMonths.addAll(costMap.keySet());

            // Create data series
            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenue");

            XYChart.Series<String, Number> costSeries = new XYChart.Series<>();
            costSeries.setName("Cost");

            XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
            profitSeries.setName("Profit");

            for (String month : allMonths) {
                double revenue = revenueMap.getOrDefault(month, 0.0);
                double cost = costMap.getOrDefault(month, 0.0);
                double profit = BigDecimal.valueOf(revenue - cost).setScale(2, RoundingMode.HALF_UP).doubleValue();

                revenueSeries.getData().add(new XYChart.Data<>(month, revenue));
                costSeries.getData().add(new XYChart.Data<>(month, cost));
                profitSeries.getData().add(new XYChart.Data<>(month, profit));
            }

            lineChart.getData().addAll(revenueSeries, costSeries, profitSeries);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateFinanceReport() throws JRException {
        LocalDateTime now = LocalDateTime.now();  // Get current date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        List<Map<String, ?>> rows = new ArrayList<>();

        for (String month : allMonths) {
            Map<String, Object> row = new HashMap<>();
            double revenue = revenueMap.getOrDefault(month, 0.0);
            double cost = costMap.getOrDefault(month, 0.0);
            double profit = revenue - cost;
            row.put("monthField", month);
            row.put("revenueField", revenue);
            row.put("costField", cost);
            row.put("profitField", profit);

            rows.add(row);
        }

        JasperReport report = (JasperReport) JRLoader.loadObjectFromFile("src/main/resources/Jasper/FinanceReport.jasper");

        List<FinanceReportHeaderData> headerDataList = new ArrayList<>();
        headerDataList.add(new FinanceReportHeaderData(formattedNow, SessionManager.getInstance().getFinanceManagerData().getName()));
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(headerDataList);

        JRBeanCollectionDataSource financeReportDataSource = new JRBeanCollectionDataSource(rows);
        Map<String, Object> params = new HashMap<>();
        params.put("TABLE_DATA_SOURCE", financeReportDataSource);

        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

        String pdfPath = "financeReport" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
        JasperExportManager.exportReportToPdfFile(print, pdfPath);

        File pdfFile = new File(pdfPath);
        if (pdfFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("AWT Desktop is not supported on this platform.");
            }
        } else {
            System.out.println("PDF file was not generated.");
        }
    }


}
