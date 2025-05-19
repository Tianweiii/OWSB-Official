package controllers.FinanceController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Datas.PurchaseOrder;
import models.Utils.FileIO;
import models.Utils.Helper;
import models.Utils.Navigator;
import models.Utils.SessionManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

public class FinancePaymentsController implements Initializable, IdkWhatToNameThis {

    private FinanceMainController mainController;
    @FXML
    private VBox makePaymentButton;

    @FXML
    private Text totalPaidField;
    @FXML
    private Text totalUnpaidField;

    @FXML
    private BarChart<String, Number> barChart;

    // table ids
    @FXML private TableView<PurchaseOrder> POTable;
    @FXML private TableColumn<PurchaseOrder, String> PO_IDField;
    @FXML private TableColumn<PurchaseOrder, String> titleField;
    @FXML private TableColumn<PurchaseOrder, Double> amountField;
    @FXML private TableColumn<PurchaseOrder, String> openedByField;
    @FXML private TableColumn<PurchaseOrder, String> statusField;
    @FXML private TableColumn<PurchaseOrder, Void> actionField;

    Navigator navigator = Navigator.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            updateSummary();
            initTable();
            initChart();
            ObservableList<PurchaseOrder> POs = this.getAllVerifiedPO();
            fillTable(POs);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    ObservableList<PurchaseOrder> getAllVerifiedPO() throws IOException, ReflectiveOperationException {
        ArrayList<PurchaseOrder> POs = FileIO.getIDsAsObjects(PurchaseOrder.class, "PurchaseOrder", "verified", 5);
        return FXCollections.observableArrayList(POs);
    }

    private void initTable() {
        PO_IDField.setCellValueFactory(new PropertyValueFactory<>("PO_ID"));
        titleField.setCellValueFactory(new PropertyValueFactory<>("title"));
        amountField.setCellValueFactory(new PropertyValueFactory<>("payableAmount"));
        openedByField.setCellValueFactory(new PropertyValueFactory<>("userID"));
        statusField.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionField.setCellFactory(column -> {
            return new TableCell<PurchaseOrder, Void>() {
                private final Button payButton = new Button("Pay");

                {
//                    payButton.set
                    // Button action
                    payButton.setOnAction(event -> {
                        PurchaseOrder po = getTableView().getItems().get(getIndex());
                        SessionManager.setCurrentPaymentPO(po);
//                        mainController.onPressPayment();
                        navigator.navigate(navigator.getRouters("finance").getRoute("makePayments"));
                    });
                }

                // idk why this is needed, if removed then button wont show
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(payButton);
                    }
                }
            };
        });
    }

    private void fillTable(ObservableList<PurchaseOrder> list) {
        POTable.setItems(list);
    }

    public void onPressRow(MouseEvent e) {
        if (e.getClickCount() == 1) {
            PurchaseOrder selection = POTable.getSelectionModel().getSelectedItem();
            System.out.println(selection.toString());
        }
    }

    private double getTotalPendingPayments() throws IOException {
        return Helper.toFixed2(FileIO.getCountOfX("PurchaseOrder", 5, 4, "verified"));
    }

    private double getTotalPaid() throws IOException {
        double totalAmount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Payment.txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                totalAmount += Double.parseDouble(parts[3].trim());
            }
        }
        return totalAmount;
    }

    private void updateSummary() throws IOException {
        totalPaidField.setText("RM " + String.format("%.2f", getTotalPaid()));
        totalUnpaidField.setText("RM " + String.format("%.2f", getTotalPendingPayments()));
    }

//    public void initChart() {
//        Map<YearMonth, Double> monthTotals = new HashMap<>();
//
//        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Payment.txt"))) {
//            String line;
//            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
//
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(",");
//                if (parts.length >= 6) {
//                    double amount = Double.parseDouble(parts[3].trim());
//                    String dateStr = parts[5].trim();
//                    YearMonth ym = YearMonth.from(sdf.parse(dateStr).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
//
//                    monthTotals.put(ym, monthTotals.getOrDefault(ym, 0.0) + amount);
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        XYChart.Series<String, Number> series = new XYChart.Series<>();
//        series.setName("Monthly Payments");
//
//        monthTotals.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .forEach(entry -> {
//                    String monthLabel = entry.getKey().getMonth().name().substring(0, 3) + " " + entry.getKey().getYear();
//                    series.getData().add(new XYChart.Data<>(monthLabel, entry.getValue()));
//                });
//
//        barChart.getData().clear();
//        barChart.getData().add(series);
//    }

    public void initChart() {
        Map<YearMonth, Double> monthTotals = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Payment.txt"))) {
            String line;
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    double amount = Double.parseDouble(parts[3].trim());
                    String dateStr = parts[5].trim();
                    YearMonth ym = YearMonth.from(
                            sdf.parse(dateStr).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    );

                    monthTotals.put(ym, monthTotals.getOrDefault(ym, 0.0) + amount);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<String> colorPalette = Arrays.asList(
                "#4CAF50", "#2196F3", "#FFC107", "#FF5722", "#9C27B0",
                "#3F51B5", "#009688", "#FF9800", "#673AB7", "#E91E63"
        );

        barChart.setLegendVisible(false);
        barChart.getData().add(series);
        barChart.setTitle("Monthly payments");

        monthTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String label = entry.getKey().getMonth().name().substring(0, 3) + " " + entry.getKey().getYear();
                    XYChart.Data<String, Number> data = new XYChart.Data<>(label, entry.getValue());
                    series.getData().add(data);
                });

        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                final XYChart.Data<String, Number> data = series.getData().get(i);
                final String color = colorPalette.get(i % colorPalette.size());

                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");

                    Tooltip tooltip = new Tooltip("RM " + String.format("%.2f", data.getYValue().doubleValue()));
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
        });

        barChart.layout();
    }

}
