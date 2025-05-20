package controllers.FinanceController;

import javafx.fxml.Initializable;

public abstract class ViewPageEssentials {
    public abstract void setMainController(FinanceMainController controller);

    public abstract void onPressBack();

    public abstract void search(String input);
}
