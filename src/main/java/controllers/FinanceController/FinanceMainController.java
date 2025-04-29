package controllers.FinanceController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// where the sidebar tings will be
public class FinanceMainController implements Initializable {

    @FXML
    private AnchorPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setAnchor("/FinanceFXML/FinanceHome.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAnchor(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Parent root = loader.load();
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        contentArea.getChildren().setAll(root);
    }
}
