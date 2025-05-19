package controllers;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/*
SAMPLE USAGE:

 */

public class StatusBoxController implements Initializable {

    @FXML private HBox status_box = new HBox();
    @FXML private Text status_text = new Text();

    private Color GREEN_BOX_COLOR = new Color(116 / 255.0, 248 / 255.0, 116 / 255.0, 1.0);
    private Color GREEN_TEXT_COLOR = new Color(48 / 255.0, 102 / 255.0, 48 / 255.0, 1.0);
    private Color YELLOW_BOX_COLOR = new Color(248 / 255.0, 248 / 255.0, 116 / 255.0, 1.0);
    private Color YELLOW_TEXT_COLOR = new Color(102 / 255.0, 102 / 255.0, 48 / 255.0, 1.0);
    private Color RED_BOX_COLOR = new Color(248 / 255.0, 116 / 255.0, 116 / 255.0, 1.0);
    private Color RED_TEXT_COLOR = new Color(102 / 255.0, 48 / 255.0, 48 / 255.0, 1.0);

    private Color box_color;
    private Color text_color;

    public void setStatus(String status){
        status = status.toUpperCase();
        switch (status){
            case "PENDING":
                box_color = YELLOW_BOX_COLOR;
                text_color = YELLOW_TEXT_COLOR;
                break;
            case "LATE":
                box_color = YELLOW_BOX_COLOR;
                text_color = YELLOW_TEXT_COLOR;
                break;
            case "APPROVED":
                box_color = GREEN_BOX_COLOR;
                text_color = GREEN_TEXT_COLOR;
                break;
            case "VERIFIED":
                box_color = GREEN_BOX_COLOR;
                text_color = GREEN_TEXT_COLOR;
                break;
            case "MISSING ITEM":
                box_color = RED_BOX_COLOR;
                text_color = RED_TEXT_COLOR;
                break;
            case "RETURNED":
                box_color = RED_BOX_COLOR;
                text_color = RED_TEXT_COLOR;
                break;
            case "PAID":
                box_color = GREEN_BOX_COLOR;
                text_color = GREEN_TEXT_COLOR;
                break;
            default:
                // If no status were caught, may be typos like "VERIFED"
                box_color = YELLOW_BOX_COLOR;
                text_color = RED_TEXT_COLOR;
                status = "UNKNOWN";
                System.out.println("Invalid status detected. From: StatusBoxController");
        }
        status_box.setBackground(
                new Background(
                new BackgroundFill(box_color, new CornerRadii(10), Insets.EMPTY)
        ));
        status_text.setFill(text_color);
        status_text.setText(status);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
