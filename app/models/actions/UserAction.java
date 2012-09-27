/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.GloryManager;
import devices.IoBoard;
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
        READY_TO_STORE_STORING,
        ESCROW_FULL_STORING,
        FINISH,
        CANCELING,;
    };
    public String error = null;
    final protected Object formData;
    protected UserActionApi userActionApi = null;
    protected User currentUser = null;
    protected final EnumMap<GloryManager.Status, String> messages;
    protected ActionState state = ActionState.IDLE;
    protected Deposit currentDeposit = null;

    public UserAction(Object formData, EnumMap<GloryManager.Status, String> messages) {
        this.formData = formData;
        this.messages = messages;
    }

    public String getActionState() {
        return state.name();
    }

    public String getActionMessage() {
        GloryManager.Status m = userActionApi.getManagerStatus();
        Logger.debug("getActionMessage Manager Status %s %s", m.name(), userActionApi.getErrorDetail());
        if (m == GloryManager.Status.ERROR) {
            state = ActionState.ERROR;
        }
        return messages.get(m);
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

    abstract public void gloryDone(GloryManager.Status m, GloryManager.ErrorDetail me);

    abstract public void ioBoardEvent(IoBoard.IoBoardStatus status);

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
