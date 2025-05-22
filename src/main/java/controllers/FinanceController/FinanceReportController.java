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
import models.Datas.Item;
import models.Datas.Payment;
import models.Datas.Transaction;
import models.Utils.FileIO;
import models.Utils.Helper;
import models.Utils.Navigator;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
//        mainController.viewAllPayments();
        navigator.navigate(navigator.getRouters("finance").getRoute("viewAllPayments"));
    }

    public void viewAllSales() {
//        mainController.viewAllSales();
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
        ArrayList<SalesItemDTO> salesItemDTOs = new ArrayList<>();
        ArrayList<Transaction> transactions = FileIO.getObjectsFromXLines(Transaction.class, "Transaction", 3);

        for (Transaction i : transactions) {
            Item item = FileIO.getIDsAsObject(Item.class, "Item", i.getItemID());

            salesItemDTOs.add(new SalesItemDTO(item.getItemName(), i.getSoldQuantity(), (i.getSoldQuantity() * item.getUnitPrice())));
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
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

            // Revenue: Month -> Total Sales
            Map<String, Double> revenueMap = new HashMap<>();

            // Cost: Month -> Total Payments
            Map<String, Double> costMap = new HashMap<>();

            // Parse item prices
            Map<String, Double> priceMap = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Item.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    priceMap.put(parts[0].trim(), Double.parseDouble(parts[6].trim()) * priceMultiplier);
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
                    revenueMap.put(month, revenueMap.getOrDefault(month, 0.0) + amount);
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
                    costMap.put(month, costMap.getOrDefault(month, 0.0) + amount);
                }
            }

            // Collect all months from both maps
            TreeSet<String> allMonths = new TreeSet<>(Comparator.comparing(m -> LocalDate.parse("01-" + m, DateTimeFormatter.ofPattern("dd-MMM yyyy"))));
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
                double profit = revenue - cost;

                revenueSeries.getData().add(new XYChart.Data<>(month, revenue));
                costSeries.getData().add(new XYChart.Data<>(month, cost));
                profitSeries.getData().add(new XYChart.Data<>(month, profit));
            }

            lineChart.getData().clear();
            lineChart.getData().addAll(revenueSeries, costSeries, profitSeries);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 2 tables, 1 for sales, 1 for payments, summary of the datas,
    public void generateFinanceReport() throws JRException {


        JasperReport report = (JasperReport) JRLoader.loadObjectFromFile("src/main/resource/Jasper/FinanceReport.jasper");


    }


}
