package views.PRPO;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import views.View;

import java.io.IOException;

public class EditPRPOView implements View {
    private AnchorPane EditPRPOPane;

    public EditPRPOView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/PRPO/EditPRPOView.fxml"));
        this.EditPRPOPane = loader.load();
    }

    @Override
    public Parent getView() { return EditPRPOPane; }
}
