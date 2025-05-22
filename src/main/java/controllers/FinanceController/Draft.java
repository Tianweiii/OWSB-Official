package controllers.FinanceController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.List;


public class Draft {

    public void init3DChart(VBox chartContainer) {
        Map<YearMonth, Double> monthTotals = new HashMap<>();

        // Read and process data
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

        // Create main container that matches your UI style
        VBox mainContainer = new VBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setSpacing(15);

        // Chart title
        Label title = new Label("Monthly Payments");
        title.setFont(Font.font("October Tamil Black", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#333333"));

        // Create 3D scene container
        StackPane chartPane = new StackPane();
        chartPane.setPrefHeight(250); // Constrain height to fit in container
        chartPane.setMaxHeight(250);

        // Create 3D content
        Group root3D = new Group();

        // Define colors that match your UI theme
        Color[] colors = {
                Color.web("#5650dc"), Color.web("#7c4dff"), Color.web("#536dfe"),
                Color.web("#448aff"), Color.web("#40c4ff"), Color.web("#18ffff"),
                Color.web("#64ffda"), Color.web("#69f0ae"), Color.web("#b2ff59"), Color.web("#eeff41")
        };

        // Sort data and find max value for scaling
        List<Map.Entry<YearMonth, Double>> sortedEntries = monthTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        if (sortedEntries.isEmpty()) {
            Label noDataLabel = new Label("No payment data available");
            noDataLabel.setFont(Font.font("October Tamil Black", 14));
            noDataLabel.setTextFill(Color.web("#c1bfc4"));
            chartContainer.getChildren().clear();
            chartContainer.getChildren().add(noDataLabel);
            return;
        }

        double maxValue = sortedEntries.stream()
                .mapToDouble(Map.Entry::getValue)
                .max()
                .orElse(1.0);

        // Create 3D bars with appropriate sizing for container
        double barWidth = Math.min(15, 200.0 / sortedEntries.size()); // Adaptive bar width
        double barSpacing = Math.min(20, 250.0 / sortedEntries.size()); // Adaptive spacing
        double maxHeight = 100; // Reduced height to fit container

        for (int i = 0; i < sortedEntries.size(); i++) {
            Map.Entry<YearMonth, Double> entry = sortedEntries.get(i);
            double value = entry.getValue();
            double height = Math.max(2, (value / maxValue) * maxHeight); // Minimum height of 2

            // Create 3D bar
            Box bar = new Box(barWidth, height, barWidth);

            // Set material with gradient-like effect
            PhongMaterial material = new PhongMaterial();
            Color barColor = colors[i % colors.length];
            material.setDiffuseColor(barColor);
            material.setSpecularColor(barColor.brighter().brighter());
            bar.setMaterial(material);

            // Position bars centered
            double totalWidth = (sortedEntries.size() - 1) * barSpacing;
            bar.setTranslateX(i * barSpacing - totalWidth / 2.0);
            bar.setTranslateY(-height / 2);
            bar.setTranslateZ(0);

            // Add month label (smaller, positioned below)
            String monthLabel = entry.getKey().getMonth().name().substring(0, 3);
            Text text = new Text(monthLabel);
            text.setFont(Font.font("October Tamil Black", 8));
            text.setFill(Color.web("#666666"));
            text.setTranslateX(i * barSpacing - totalWidth / 2.0);
            text.setTranslateY(15);
            text.setTranslateZ(barWidth);

            // Add value label (on hover via tooltip)
            Tooltip tooltip = new Tooltip(String.format("%s %d\nRM %.2f",
                    entry.getKey().getMonth().name(),
                    entry.getKey().getYear(),
                    value));
            tooltip.setStyle("-fx-background-color: #5650dc; -fx-text-fill: white; -fx-font-size: 12px;");
            Tooltip.install(bar, tooltip);

            root3D.getChildren().addAll(bar, text);
        }

        // Create subtle base platform
        if (!sortedEntries.isEmpty()) {
            double totalWidth = (sortedEntries.size() - 1) * barSpacing + barWidth * 2;
            Box base = new Box(Math.max(totalWidth, 50), 1, barWidth + 5);
            PhongMaterial baseMaterial = new PhongMaterial();
            baseMaterial.setDiffuseColor(Color.web("#ededf6"));
            baseMaterial.setSpecularColor(Color.web("#f5f5f5"));
            base.setMaterial(baseMaterial);
            base.setTranslateY(2);
            root3D.getChildren().add(base);
        }

        // Create SubScene that fits the container
        SubScene subScene = new SubScene(root3D, 400, 200, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT); // Transparent background

        // Set up camera with better positioning
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-150);
        camera.setTranslateY(-20);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        subScene.setCamera(camera);

        // Add lighting for better visual appeal
        AmbientLight ambientLight = new AmbientLight(Color.WHITE.deriveColor(0, 1, 0.4, 1));
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-30);
        pointLight.setTranslateY(-50);
        pointLight.setTranslateZ(-100);
        root3D.getChildren().addAll(ambientLight, pointLight);

        // Initial rotation for better perspective
        Rotate initialRotateX = new Rotate(-15, Rotate.X_AXIS);
        Rotate initialRotateY = new Rotate(25, Rotate.Y_AXIS);
        root3D.getTransforms().addAll(initialRotateX, initialRotateY);

        // Add mouse interaction
        addSmoothMouseInteraction(subScene, root3D);

        // Add components to containers
        chartPane.getChildren().add(subScene);
        mainContainer.getChildren().addAll(title, chartPane);

        // Create summary info
        VBox summaryBox = new VBox(5);
        summaryBox.setAlignment(Pos.CENTER);

        double totalAmount = sortedEntries.stream().mapToDouble(Map.Entry::getValue).sum();
        Label totalLabel = new Label(String.format("Total: RM %.2f", totalAmount));
        totalLabel.setFont(Font.font("October Tamil Black", FontWeight.NORMAL, 12));
        totalLabel.setTextFill(Color.web("#5650dc"));

        Label instructionLabel = new Label("Drag to rotate • Scroll to zoom");
        instructionLabel.setFont(Font.font("October Tamil Black", 10));
        instructionLabel.setTextFill(Color.web("#c1bfc4"));

        summaryBox.getChildren().addAll(totalLabel, instructionLabel);
        mainContainer.getChildren().add(summaryBox);

        Platform.runLater(() -> {
            chartContainer.getChildren().clear();
            chartContainer.getChildren().add(mainContainer);
        });
    }

    private void addSmoothMouseInteraction(SubScene subScene, Group root3D) {
        final double[] mouseOldX = new double[1];
        final double[] mouseOldY = new double[1];
        final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

        // Add to existing transforms
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
                // Smooth rotation with constraints
                double newRotateY = rotateY.getAngle() + mouseDeltaX * 0.3;
                double newRotateX = Math.max(-60, Math.min(60, rotateX.getAngle() - mouseDeltaY * 0.3));

                rotateY.setAngle(newRotateY);
                rotateX.setAngle(newRotateX);
            }

            mouseOldX[0] = event.getSceneX();
            mouseOldY[0] = event.getSceneY();
        });

        // Smooth zoom with limits
        subScene.setOnScroll(event -> {
            double currentScale = root3D.getScaleX();
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            double newScale = currentScale * zoomFactor;

            // Constrain zoom levels
            newScale = Math.max(0.5, Math.min(2.0, newScale));

            root3D.setScaleX(newScale);
            root3D.setScaleY(newScale);
            root3D.setScaleZ(newScale);
        });
    }

}
