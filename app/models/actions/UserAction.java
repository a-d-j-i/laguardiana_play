/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.Manager;
import models.ModelFacade.ActionApi;

/**
 *
 * @author adji
 */
abstract public class UserAction {

    static public enum CurrentStep {

        RESERVED(null),
        ERROR("counterError"),
        NONE("start"),
        RUNNING("mainLoop"),
        FINISH("finish"),;
        final private String action;

        private CurrentStep(String action) {
            this.action = action;

        }

        public String getAction() {
            return action;
        }
    }
    protected CurrentStep currentStep = CurrentStep.NONE;
    protected String error = null;
    final protected Object formData;
    protected ActionApi userActionApi = null;

    public UserAction(Object formData) {
        this.formData = formData;
    }

    public CurrentStep getCurrentStep() {
        return currentStep;
    }
//    {
//        if (currentUserAction == null || currentDeposit.user == null) {
//            // unfinished Cancelation.
//            if (currentStep == ModelFacade.CurrentStep.FINISH || currentStep == ModelFacade.CurrentStep.ERROR) {
//                return currentStep;
//            }
//            if (currentStep != ModelFacade.CurrentStep.NONE) {
//                error(String.format("getCurrentStep Invalid step %s", currentStep.name()));
//            }
//            return ModelFacade.CurrentStep.NONE;
//        }
//        if (currentDeposit.user.equals(Secure.getCurrentUser())) {
//            return currentStep;
//        } else {
//            return ModelFacade.CurrentStep.RESERVED;
//        }
//    }

    public void start(ActionApi userActionApi) {
        this.userActionApi = userActionApi;
    }

    public Object getFormData() {
        return formData;
    }

    abstract public void start();

    abstract public void gloryDone(Manager.Status m, Manager.ErrorDetail me);

    abstract public String getNeededController();
}
