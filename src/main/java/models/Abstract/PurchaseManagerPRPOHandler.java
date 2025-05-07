package models.Abstract;

import controllers.PRPOBoxController;

public class PurchaseManagerPRPOHandler extends PRPOAbstractHandler {
    public PurchaseManagerPRPOHandler(PRPOBoxController controller) {
        super(controller);
    }

    @Override
    public void setUpView() {
        controller.setPOData(); // Put ur data
        controller.setPRData();
        controller.setBoxStatus("approved");
        // Disable editing buttons
    }
}
