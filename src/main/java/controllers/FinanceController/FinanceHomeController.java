package controllers.FinanceController;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import models.Datas.Payment;
import models.Utils.FileIO;
import models.Utils.Navigator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import java.util.List;

public class FinanceHomeController implements Initializable {

    private FinanceMainController mainController;
    Navigator navigator = Navigator.getInstance();

    @FXML
    private BarChart<String, Number> monthlySalesChart;
    @FXML
    private VBox recentTransactionContainer;
    @FXML
    private VBox secondContainer;
    @FXML
    private VBox firstContainer;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private VBox chartContainer;
    @FXML
    private Text totalCostField;
    @FXML
    private Text totalNetProfitField;
    @FXML
    private Text totalRevenueField;
    @FXML
    private Text profitMarginField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init3DChart(chartContainer);

        try {
            renderRecentTransactions();
            getMonthlyGPM();
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void init3DChart(VBox chartContainer) {
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

        VBox mainContainer = new VBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setSpacing(20);

        Label title = new Label("Monthly Payments");
        title.setFont(Font.font("October Tamil Black", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#333333"));

        StackPane chartPane = new StackPane();
        chartPane.prefWidthProperty().bind(
                Bindings.when(chartContainer.widthProperty().greaterThan(800))
                        .then(chartContainer.widthProperty().subtract(50))
                        .otherwise(chartContainer.widthProperty().subtract(20))
        );
        chartPane.prefHeightProperty().bind(
                Bindings.when(chartContainer.heightProperty().greaterThan(400))
                        .then(chartContainer.heightProperty().multiply(0.7))
                        .otherwise(chartContainer.heightProperty().multiply(0.6))
        );

        chartPane.setMinWidth(300);
        chartPane.setMaxWidth(1200);
        chartPane.setMinHeight(200);
        chartPane.setMaxHeight(600);

        Group root3D = new Group();

        Color[] colors = {
                Color.web("#5650dc"), Color.web("#7c4dff"), Color.web("#536dfe"),
                Color.web("#448aff"), Color.web("#40c4ff"), Color.web("#18ffff"),
                Color.web("#64ffda"), Color.web("#69f0ae"), Color.web("#b2ff59"), Color.web("#eeff41")
        };

        List<Map.Entry<YearMonth, Double>> sortedEntries = monthTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        if (sortedEntries.isEmpty()) {
            Label noDataLabel = new Label("No payment data available");
            noDataLabel.setFont(Font.font("October Tamil Black", 18));
            noDataLabel.setTextFill(Color.web("#c1bfc4"));
            chartContainer.getChildren().clear();
            chartContainer.getChildren().add(noDataLabel);
            return;
        }

        double maxValue = sortedEntries.stream()
                .mapToDouble(Map.Entry::getValue)
                .max()
                .orElse(1.0);

        double barWidth = Math.max(20, Math.min(40, 600.0 / sortedEntries.size()));
        double barSpacing = Math.max(25, Math.min(60, 800.0 / sortedEntries.size()));
        double maxHeight = 200;

        for (int i = 0; i < sortedEntries.size(); i++) {
            Map.Entry<YearMonth, Double> entry = sortedEntries.get(i);
            double value = entry.getValue();
            double height = Math.max(5, (value / maxValue) * maxHeight);

            Box bar = new Box(barWidth, height, barWidth);

            PhongMaterial material = new PhongMaterial();
            Color barColor = colors[i % colors.length];
            material.setDiffuseColor(barColor);
            material.setSpecularColor(barColor.brighter().brighter());
            material.setSpecularPower(32);
            bar.setMaterial(material);

            // bar position
            double totalWidth = (sortedEntries.size() - 1) * barSpacing;
            bar.setTranslateX(i * barSpacing - totalWidth / 2.0);
            bar.setTranslateY(-height / 2);
            bar.setTranslateZ(0);

            // month
            String monthLabel = entry.getKey().getMonth().name().substring(0, 3);
            Text monthText = new Text(monthLabel);
            monthText.setFont(Font.font("October Tamil Black", FontWeight.BOLD, 14));
            monthText.setFill(Color.web("#555555"));
            monthText.setTranslateX(i * barSpacing - totalWidth / 2.0);
            monthText.setTranslateY(25);
            monthText.setTranslateZ(barWidth + 5);

            // year
            String yearLabel = String.valueOf(entry.getKey().getYear());
            Text yearText = new Text(yearLabel);
            yearText.setFont(Font.font("October Tamil Black", 12));
            yearText.setFill(Color.web("#888888"));
            yearText.setTranslateX(i * barSpacing - totalWidth / 2.0);
            yearText.setTranslateY(40);
            yearText.setTranslateZ(barWidth + 5);

            // values of bars
            if (height > 30) {
                Text valueText = new Text(String.format("%.0f", value));
                valueText.setFont(Font.font("October Tamil Black", FontWeight.BOLD, 11));
                valueText.setFill(Color.web("#333333"));
                valueText.setTranslateX(i * barSpacing - totalWidth / 2.0);
                valueText.setTranslateY(-height - 15);
                valueText.setTranslateZ(0);
                root3D.getChildren().add(valueText);
            }

            Tooltip tooltip = new Tooltip(String.format("%s %d\nRM %.2f",
                    entry.getKey().getMonth().name(),
                    entry.getKey().getYear(),
                    value));
            tooltip.setStyle("-fx-background-color: #5650dc; -fx-text-fill: white; -fx-font-size: 14px; " +
                    "-fx-padding: 8px; -fx-background-radius: 6px;");
            Tooltip.install(bar, tooltip);

            root3D.getChildren().addAll(bar, monthText, yearText);
        }

        if (!sortedEntries.isEmpty()) {
            double totalWidth = (sortedEntries.size() - 1) * barSpacing + barWidth * 2;
            Box base = new Box(Math.max(totalWidth + 40, 100), 3, barWidth + 20);
            PhongMaterial baseMaterial = new PhongMaterial();
            baseMaterial.setDiffuseColor(Color.web("#ededf6"));
            baseMaterial.setSpecularColor(Color.web("#f8f8f8"));
            base.setMaterial(baseMaterial);
            base.setTranslateY(5);
            root3D.getChildren().add(base);

            addGridLines(root3D, totalWidth + 40, barWidth + 20, maxHeight);
        }

        SubScene subScene = new SubScene(root3D, 800, 500, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);

        // no overflow type shi
        subScene.widthProperty().bind(
                Bindings.min(chartPane.widthProperty(), 1200)
        );
        subScene.heightProperty().bind(
                Bindings.min(chartPane.heightProperty(), 600)
        );

        // for window resizing
        chartContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.widthProperty().addListener((obsWidth, oldWidth, newWidth) ->
                                Platform.runLater(() -> {
                                    chartPane.autosize();
                                    subScene.autosize();
                                })
                        );
                        newWin.heightProperty().addListener((obsHeight, oldHeight, newHeight) ->
                                Platform.runLater(() -> {
                                    chartPane.autosize();
                                    subScene.autosize();
                                })
                        );
                    }
                });
            }
        });

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-450);
        camera.setTranslateY(-100);
        camera.setFieldOfView(45);
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);
        subScene.setCamera(camera);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE.deriveColor(0, 1, 0.5, 1));

        PointLight mainLight = new PointLight(Color.WHITE);
        mainLight.setTranslateX(-100);
        mainLight.setTranslateY(-150);
        mainLight.setTranslateZ(-200);

        PointLight fillLight = new PointLight(Color.WHITE.deriveColor(0, 1, 0.3, 1));
        fillLight.setTranslateX(100);
        fillLight.setTranslateY(-100);
        fillLight.setTranslateZ(-150);

        root3D.getChildren().addAll(ambientLight, mainLight, fillLight);

        Rotate initialRotateX = new Rotate(-15, Rotate.X_AXIS);
        Rotate initialRotateY = new Rotate(10, Rotate.Y_AXIS);
        root3D.getTransforms().addAll(initialRotateX, initialRotateY);

        addFullscreenMouseInteraction(subScene, root3D);

        chartPane.getChildren().add(subScene);
        mainContainer.getChildren().addAll(title, chartPane);

        VBox summaryBox = new VBox(8);
        summaryBox.setAlignment(Pos.CENTER);

        double totalAmount = sortedEntries.stream().mapToDouble(Map.Entry::getValue).sum();
        double avgAmount = totalAmount / sortedEntries.size();

        Label totalLabel = new Label(String.format("Total: RM %.2f", totalAmount));
        totalLabel.setFont(Font.font("October Tamil Black", FontWeight.BOLD, 16));
        totalLabel.setTextFill(Color.web("#5650dc"));

        Label avgLabel = new Label(String.format("Average: RM %.2f", avgAmount));
        avgLabel.setFont(Font.font("October Tamil Black", FontWeight.NORMAL, 14));
        avgLabel.setTextFill(Color.web("#666666"));

        Label instructionLabel = new Label("Drag to rotate • Scroll to zoom  • Hover for 1 second for details");
        instructionLabel.setFont(Font.font("October Tamil Black", 12));
        instructionLabel.setTextFill(Color.web("#adabb0"));

        summaryBox.getChildren().addAll(totalLabel, avgLabel, instructionLabel);
        mainContainer.getChildren().add(summaryBox);

        Platform.runLater(() -> {
            chartContainer.getChildren().clear();
            chartContainer.getChildren().add(mainContainer);

            mainContainer.autosize();
            chartPane.autosize();

            Platform.runLater(() -> {
                chartContainer.requestLayout();
                mainContainer.requestLayout();
            });
        });
    }

    private void addGridLines(Group root3D, double width, double depth, double height) {
        PhongMaterial gridMaterial = new PhongMaterial();
        gridMaterial.setDiffuseColor(Color.web("#e0e0e0"));

        for (int i = 0; i <= 4; i++) {
            Box gridLine = new Box(1, height * 1.2, 1);
            gridLine.setMaterial(gridMaterial);
            gridLine.setTranslateX(-width/2 + (width/4) * i);
            gridLine.setTranslateY(-height * 0.6);
            gridLine.setTranslateZ(depth/2 + 10);
            gridLine.setOpacity(0.3);
            root3D.getChildren().add(gridLine);
        }
    }

    private void addFullscreenMouseInteraction(SubScene subScene, Group root3D) {
        final double[] mouseOldX = new double[1];
        final double[] mouseOldY = new double[1];
        final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

        root3D.getTransforms().addAll(rotateX, rotateY);

        subScene.setOnMousePressed(event -> {
            mouseOldX[0] = event.getSceneX();
            mouseOldY[0] = event.getSceneY();
            subScene.setCursor(javafx.scene.Cursor.CLOSED_HAND);
        });

        subScene.setOnMouseReleased(event -> {
            subScene.setCursor(javafx.scene.Cursor.HAND);
        });

        subScene.setOnMouseEntered(event -> {
            subScene.setCursor(javafx.scene.Cursor.HAND);
        });

        subScene.setOnMouseExited(event -> {
            subScene.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        subScene.setOnMouseDragged(event -> {
            double mouseDeltaX = (event.getSceneX() - mouseOldX[0]);
            double mouseDeltaY = (event.getSceneY() - mouseOldY[0]);

            if (event.isPrimaryButtonDown()) {
                double sensitivity = 0.4;
                double newRotateY = rotateY.getAngle() + mouseDeltaX * sensitivity;
                double newRotateX = Math.max(-75, Math.min(75, rotateX.getAngle() - mouseDeltaY * sensitivity));

                rotateY.setAngle(newRotateY);
                rotateX.setAngle(newRotateX);
            }

            mouseOldX[0] = event.getSceneX();
            mouseOldY[0] = event.getSceneY();
        });

        subScene.setOnScroll(event -> {
            double currentScale = root3D.getScaleX();
            double zoomFactor = event.getDeltaY() > 0 ? 1.15 : 0.87;
            double newScale = currentScale * zoomFactor;

            newScale = Math.max(0.3, Math.min(3.0, newScale));

            root3D.setScaleX(newScale);
            root3D.setScaleY(newScale);
            root3D.setScaleZ(newScale);
        });
    }

    public void setMainController(FinanceMainController controller) {
        mainController = controller;
    }

    public void onPressViewAll() {
        navigator.navigate(navigator.getRouters("finance").getRoute("viewAllPayments"));
    }

    public void renderRecentTransactions() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String[] colors = new String[]{"pink", " #7fab92", " #f39583", " #5cb9e0"};
        int count = 0;

        ArrayList<Payment> transactions = FileIO.getObjectsFromXLines(Payment.class, "Payment", 3);
        System.out.println("transactions" + transactions);

        for (Payment i : transactions) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Components/RecentTransactionItem.fxml"));
            Parent card = loader.load();

            RecentTransactionItemController controller = loader.getController();
            controller.setData(i.getPaymentID(), i.getCreatedAt(), i.getAmount());
            controller.setColor(colors[count]);

            recentTransactionContainer.getChildren().add(card);

            count++;
        }
    }

    public void getMonthlyGPM() throws IOException {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        double priceMultiplier = 1.15;
        // Gross profit margin = (Revenue - Cost of Goods Sold) / Revenue × 100
        // revenue is total sales
        // cost of goods sold is total payments per month
        Map<String, Double> revenueMap = new HashMap<>();
        Map<String, Double> costMap = new HashMap<>();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        String formattedDate = today.format(formatter);

        // get prices
        Map<String, Double> priceMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/Item.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                priceMap.put(parts[0].trim(), Double.parseDouble(parts[6].trim()) * priceMultiplier);
            }
        }

        // revenue
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

        // cost
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

        double thisMonthRevenue = revenueMap.getOrDefault(formattedDate, 0.00);
        double thisMonthCost = costMap.getOrDefault(formattedDate, 0.00);

        totalRevenueField.setText("RM " + String.format("%.2f", thisMonthRevenue));
        totalCostField.setText("RM " + String.format("%.2f", thisMonthCost));

        double netProfit = thisMonthRevenue - thisMonthCost;
        String formattedProfit = String.format("%.2f", Math.abs(netProfit));
        String prefix = netProfit < 0 ? "-RM " : "RM ";

        totalNetProfitField.setText(prefix + formattedProfit);


        XYChart.Series<String, Number> gpmSeries = new XYChart.Series<>();
        gpmSeries.setName("Gross Profit Margin (%)");

        // Ensure chronological month order (basic)
        Set<String> allMonths = new TreeSet<>(revenueMap.keySet());
        allMonths.addAll(costMap.keySet());

        for (String month : allMonths) {
            double revenue = revenueMap.getOrDefault(month, 0.0);
            double cost = costMap.getOrDefault(month, 0.0);
            double gpm = revenue != 0 ? ((revenue - cost) / revenue) * 100 : 0;
            gpmSeries.getData().add(new XYChart.Data<>(month, gpm));
        }

        lineChart.setTitle("Gross Profit Margin by Month");
        lineChart.getXAxis().setLabel("Month");
        lineChart.getYAxis().setLabel("GPM (%)");
        lineChart.setLegendVisible(false);

        lineChart.getData().add(gpmSeries);

        double thisMonthGPM = thisMonthRevenue != 0 ? ((thisMonthRevenue - thisMonthCost) / thisMonthRevenue) * 100 : 0;
        profitMarginField.setText(String.format("%.2f%%", thisMonthGPM));

    }
}
