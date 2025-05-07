package models.Abstract;

import controllers.PRPOBoxController;

public class SalesManagerPRPOHandler extends PRPOAbstractHandler {
    public SalesManagerPRPOHandler(PRPOBoxController controller) {
        super(controller);
    }

    @Override
    public void setUpView() {
        controller.setPRData();
        controller.setPOData();
        controller.setBoxStatus("pending");
//        ADD IN CRUD
    }
}
