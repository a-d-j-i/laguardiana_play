/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.Manager;
import java.util.EnumMap;
import models.Deposit;
import models.ModelFacade.UserActionApi;
import models.User;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class UserAction {

    static public enum ActionState {

        IDLE,
        ERROR,
        READY_TO_STORE,
        ESCROW_FULL,
        FINISH,
        CANCELING,;
    };
    public String error = null;
    final protected Object formData;
    protected UserActionApi userActionApi = null;
    protected User currentUser = null;
    protected final EnumMap<Manager.Status, String> messages;
    protected ActionState state = ActionState.IDLE;
    protected Deposit currentDeposit = null;

    public UserAction(Object formData, EnumMap<Manager.Status, String> messages) {
        this.formData = formData;
        this.messages = messages;
    }

    public String getActionState() {
        return state.name();
    }

    public String getActionMessage() {
        return messages.get(userActionApi.getManagerStatus());
    }

    public String getControllerAction() {
        switch (state) {
            case ERROR:
                return "counterError";
            case FINISH:
                return "finish";
            default:
                return "mainLoop";
        }
    }

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

    public Deposit getDeposit() {
        return currentDeposit;
    }

    public void finishAction() {
        userActionApi.finishAction();
    }

    protected void error(String message, Object... args) {
        Logger.error(message, args);
        error = String.format(message, args);
        state = ActionState.ERROR;
    }
}
