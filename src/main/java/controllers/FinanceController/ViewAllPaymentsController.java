package controllers.FinanceController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import models.Datas.Payment;
import models.Utils.FileIO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ViewAllPaymentsController extends ViewPageEssentials implements Initializable {

    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, String> PO_IDField;
    @FXML
    private TableColumn<Payment, String> amountField;
    @FXML
    private TableColumn<Payment, String> createdAtField;
    @FXML
    private TableColumn<Payment, String> paymentIDField;
    @FXML
    private TableColumn<Payment, String> paymentMethodField;
    @FXML
    private TableColumn<Payment, String> paymentReferenceField;
    @FXML
    private TableColumn<Payment, String> userIDField;

    @FXML
    private TextField searchBar;
    @FXML
    private PieChart pieChart;

    private ObservableList<Payment> allPayments;
    private FilteredList<Payment> filteredPayments;
    private FinanceMainController mainController;

    @FXML
    private Text totalPaidField;
    @FXML
    private Text totalUnpaidField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initTable();
            allPayments = this.getAllPayments();
            filteredPayments = new FilteredList<>(allPayments, predicate -> true);
            fillTable(allPayments);
            updateSummaryFields();

            initPieChart();
        } catch (IOException | ReflectiveOperationException e) {
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
        paymentIDField.setCellValueFactory(new PropertyValueFactory<>("paymentID"));
        PO_IDField.setCellValueFactory(new PropertyValueFactory<>("PO_ID"));
        userIDField.setCellValueFactory(new PropertyValueFactory<>("userID"));
        amountField.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentMethodField.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        createdAtField.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        paymentReferenceField.setCellValueFactory(new PropertyValueFactory<>("paymentReference"));
    }

    private void fillTable(ObservableList<Payment> list) {
        paymentTable.setItems(list);
    }

    ObservableList<Payment> getAllPayments() throws IOException, ReflectiveOperationException {
        ArrayList<Payment> Payments = FileIO.getAllLines(Payment.class, "Payment");
        return FXCollections.observableArrayList(Payments);
    }

    public void search(String input) {
        String keyword = input.toLowerCase();
        filteredPayments.setPredicate(data -> {
            if (input.isBlank()) {
                return true;
            }

            if (data.getPaymentID().toLowerCase().contains(keyword)) {
                return true;
            } else if (data.getPO_ID().toLowerCase().contains(keyword)) {
                return true;
            } else if (data.getUserID().toLowerCase().contains(keyword)) {
                return true;
            } else if (String.valueOf(data.getAmount()).toLowerCase().contains(keyword)) {
                return true;
            } else if (data.getCreatedAt().toLowerCase().contains(keyword)) {
                return true;
            } else return data.getPaymentReference().toLowerCase().contains(keyword);
        });
        SortedList<Payment> sortedData = new SortedList<>(filteredPayments);
        sortedData.comparatorProperty().bind(paymentTable.comparatorProperty());
        paymentTable.setItems(sortedData);
        updateSummaryFields();
    }

    // sum of all payments
    private double getTotalPaid() {
        return filteredPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    // sum of all verified POs
    private double getTotalUnpaid() throws IOException {
        double total = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/PurchaseOrder.txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                if (parts[5].trim().equalsIgnoreCase("approved")) {
                    total += Double.parseDouble(parts[4]);
                }
            }
        }
        totalUnpaidField.setText("RM " + String.format("%.2f", total));
        return total;
    }

    private void initPieChart() throws IOException {
        double paid = getTotalPaid();
        double unpaid = getTotalUnpaid();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Total Paid", paid),
                new PieChart.Data("Total Unpaid", unpaid)
        );

        pieChart.setData(pieData);
        pieChart.setTitle("Total Paid vs Unpaid");
    }

    private void updateSummaryFields() {
        totalPaidField.setText("RM " + String.format("%.2f", getTotalPaid()));
    }

}
