package models.Abstract;

import controllers.PRPOBoxController;

public class InventoryPRPOHandler extends PRPOAbstractHandler{

    public InventoryPRPOHandler(PRPOBoxController controller) {
        super(controller);
    }

    @Override
    public void setUpView() {
        controller.setPOData();
        controller.setBoxStatus("Verified");
    }



}
