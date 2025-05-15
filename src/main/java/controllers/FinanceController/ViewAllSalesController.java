package controllers.FinanceController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import models.DTO.SalesTransactionDTO;
import models.Datas.Item;
import models.Datas.Sales;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ViewAllSalesController extends ViewPageEssentials implements Initializable {

    @FXML
    private TableView<SalesTransactionDTO> salesTable;
    @FXML
    private TableColumn<SalesTransactionDTO, String> createdDateField;
    @FXML
    private TableColumn<SalesTransactionDTO, String> itemNameField;
    @FXML
    private TableColumn<SalesTransactionDTO, String> transactionIDField;
    @FXML
    private TableColumn<SalesTransactionDTO, String> userIDField;
    @FXML
    private TableColumn<SalesTransactionDTO, String> amountField;

    @FXML
    private TextField searchBar;
    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private Text totalSalesField;
    @FXML
    private Text totalTransactionsField;

    private FilteredList<SalesTransactionDTO> filteredSales;
    private FinanceMainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initTable();
            ObservableList<SalesTransactionDTO> sales = this.getAllSales();
            initBarChart(sales);
            filteredSales = new FilteredList<>(sales, predicate -> true);
            fillTable(sales);

            updateTotals();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> search(newValue));
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressBack() {
        mainController.goBack();
    }

    private void initTable() {
        transactionIDField.setCellValueFactory(new PropertyValueFactory<>("transactionID"));
        userIDField.setCellValueFactory(new PropertyValueFactory<>("userID"));
        itemNameField.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        amountField.setCellValueFactory(new PropertyValueFactory<>("salesAmount"));
        createdDateField.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
    }

    private void fillTable(ObservableList<SalesTransactionDTO> list) {
        salesTable.setItems(list);
    }

    public void search(String input) {
        String keyword = input.toLowerCase();
        filteredSales.setPredicate(data -> {
            if (input.isBlank()) {
                return true;
            }

            if (data.getTransactionID().toLowerCase().contains(keyword)) {
                return true;
            } else if (data.getItemName().toLowerCase().contains(keyword)) {
                return true;
            } else if (String.valueOf(data.getSalesAmount()).contains(keyword)) {
                return true;
            } else if (data.getCreatedDate().toLowerCase().contains(keyword)) {
                return true;
            } else return data.getUserID().toLowerCase().contains(keyword);
        });
        SortedList<SalesTransactionDTO> sortedSales = new SortedList<>(filteredSales);
        sortedSales.comparatorProperty().bind(salesTable.comparatorProperty());
        salesTable.setItems(sortedSales);
        updateTotals();
    }

    ObservableList<SalesTransactionDTO> getAllSales() throws IOException {
        Map<String, Item> priceMap = new HashMap<>();
        Map<String, Sales> salesMap = new HashMap<>();
        ArrayList<SalesTransactionDTO> list = new ArrayList<>();

        // getting sales
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Sales.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                salesMap.put(parts[0], new Sales(parts[0], parts[1], parts[2], parts[3]));
            }
        }

        // getting prices
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Item.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String itemId = parts[0].trim();
                Item item = new Item(itemId, parts[1].trim(), Double.parseDouble(parts[6].trim()));
                priceMap.put(itemId, item);
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Transaction.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String itemId = parts[3].trim();
                int quantity = Integer.parseInt(parts[2].trim());
                double amount = priceMap.get(itemId).getUnitPrice() * quantity;

                list.add(new SalesTransactionDTO(parts[0], priceMap.get(itemId).getItemName(), amount, salesMap.get(parts[4]).getCreatedAt(), salesMap.get(parts[4]).getUserID()));
            }
        }

        return FXCollections.observableArrayList(list);
    }

    private int getTotalTransactions() {
        return filteredSales.size(); // Total rows currently shown (after filtering)
    }

    private double getTotalSales() {
        return filteredSales.stream()
                .mapToDouble(SalesTransactionDTO::getSalesAmount)
                .sum();
    }

    private void updateTotals() {
        totalTransactionsField.setText(String.valueOf(getTotalTransactions()));
        totalSalesField.setText("RM " + String.format("%.2f", getTotalSales()));
    }

    private void initBarChart(ObservableList<SalesTransactionDTO> allSales) {
        Map<String, Double> monthlySales = new HashMap<>();
        for (SalesTransactionDTO sale : allSales) {
            String dateStr = sale.getCreatedDate(); // format 25-04-2025
            System.out.println("12312312" + dateStr);
            String[] parts = dateStr.split("-");
            String monthYear = parts[1] + "-" + parts[2];
            monthlySales.put(monthYear, monthlySales.getOrDefault(monthYear, 0.0) + sale.getSalesAmount());
        }

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Monthly Sales");

        monthlySales.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        barChart.getData().clear();
        barChart.getData().add(series);
    }

}
