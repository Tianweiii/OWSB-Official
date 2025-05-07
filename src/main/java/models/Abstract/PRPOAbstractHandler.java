package models.Abstract;

import controllers.PRPOBoxController;

public abstract class PRPOAbstractHandler {
    protected PRPOBoxController controller;


    public PRPOAbstractHandler(PRPOBoxController controller) {
        this.controller = controller;
    }

    public abstract void setUpView();
}
