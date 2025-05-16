package controllers.FinanceController;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

public class FinanceHomeController implements Initializable {

    @FXML
    private BarChart<String, Number> monthlySalesChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initChart();
    }

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

        monthlySalesChart.setLegendVisible(false);
        monthlySalesChart.getData().add(series);
        monthlySalesChart.setTitle("Monthly payments");

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

        monthlySalesChart.layout();
    }
}
