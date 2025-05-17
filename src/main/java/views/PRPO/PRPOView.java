package views.PRPO;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;
import java.net.URL;

public class PRPOView implements View {
    private AnchorPane PRPOPane;

    public PRPOView() throws IOException {
        FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/PRPO/PRPO.fxml"));
        this.PRPOPane = loader.load();
    }

    @Override
    public Parent getView() { return PRPOPane; }
}
