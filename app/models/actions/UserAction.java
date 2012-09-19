/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.Manager;
import models.ModelFacade.UserActionApi;
import models.User;
import play.Logger;
import play.libs.F;

/**
 *
 * @author adji
 */
abstract public class UserAction {

    public String error = null;
    final protected Object formData;
    protected UserActionApi userActionApi = null;
    protected User currentUser = null;

    public UserAction(Object formData) {
        this.formData = formData;
    }

    protected void error(String message, Object... args) {
        Logger.error(message, args);
        error = String.format(message, args);
    }

    abstract public F.Tuple<String, String> getActionState();

    abstract public String getControllerAction();

//    {
//        if (currentUserAction == null || currentDeposit.user == null) {
//            // unfinished Cancelation.
//            if (actionState == ModelFacade.ActionState.FINISH || actionState == ModelFacade.ActionState.ERROR) {
//                return actionState;
//            }
//            if (actionState != ModelFacade.ActionState.START) {
//                error(String.format("getCurrentActionState Invalid step %s", actionState.name()));
//            }
//            return ModelFacade.ActionState.START;
//        }
//        if (currentDeposit.user.equals(Secure.getCurrentUser())) {
//            return actionState;
//        } else {
//            return ModelFacade.ActionState.RESERVED;
//        }
//    }
    public void start(User currentUser, UserActionApi userActionApi) {
        this.userActionApi = userActionApi;
        this.currentUser = currentUser;
        start();
    }

    public Object getFormData() {
        return formData;
    }

    abstract public void start();

    abstract public void gloryDone(Manager.Status m, Manager.ErrorDetail me);

    abstract public String getNeededController();

    public void finishAction() {
        userActionApi.finishAction();
    }
}
