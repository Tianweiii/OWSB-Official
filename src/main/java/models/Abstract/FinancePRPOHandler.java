package models.Abstract;

import controllers.PRPOBoxController;

public class FinancePRPOHandler extends PRPOAbstractHandler{
    public FinancePRPOHandler(PRPOBoxController controller) {
        super(controller);
    }

    @Override
    public void setUpView() {
        controller.setPOData();
    }
}
