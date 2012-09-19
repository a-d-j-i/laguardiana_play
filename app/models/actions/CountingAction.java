/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.Manager;
import java.util.List;
import models.lov.Currency;
import play.Logger;
import play.libs.F;
import validation.Bill;

/**
 *
 * @author adji
 */
public class CountingAction extends UserAction {

    static public enum ActionState {

        IDLE,
        ERROR,
        READY_TO_STORE,
        ESCROW_FULL,
        FINISH,
        CANCELING,;
    };
    public Currency currency;
    public ActionState state = ActionState.IDLE;

    public CountingAction(Currency currency, Object formData) {
        super(formData);
        this.currency = currency;
    }

    @Override
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

    @Override
    public F.Tuple<String, String> getActionState() {
        if (state == ActionState.IDLE) {
            switch (userActionApi.getManagerStatus()) {
                case ERROR:
                    state = ActionState.ERROR;
                    break;
                case READY_TO_STORE:
                    state = ActionState.READY_TO_STORE;
                    break;
                case ESCROW_FULL:
                    state = ActionState.ESCROW_FULL;
                    break;
                default:
                    Logger.debug("getControllerAction Current manager state %s %s",
                            state.name(), userActionApi.getManagerStatus().name());
                    break;
            }
        }
        return new F.Tuple<String, String>(state.name(), userActionApi.getManagerStatus().name());
    }

    @Override
    final public String getNeededController() {
        return "CountController";
    }

    @Override
    public void start() {
        if (!userActionApi.count(currency.numericId)) {
            state = ActionState.ERROR;
            error = String.format("startCounting can't start glory %s", userActionApi.getErrorDetail());
        }
    }

    public List<Bill> getBillData() {
        return Bill.getCurrentCounters(currency.numericId);
    }

    public void acceptDeposit() {
        if (state == ActionState.READY_TO_STORE) {
            if (!userActionApi.cancelDeposit()) {
                Logger.error("startCounting can't cancel glory");
            }
        } else if (state == ActionState.ESCROW_FULL) {
            if (!userActionApi.withdrawDeposit()) {
                Logger.error("startCounting can't cancel glory");
            }
        } else {
            Logger.error("acceptDeposit Invalid step");

        }
    }

    public void cancelDeposit() {
        state = ActionState.CANCELING;
        if (!userActionApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
    }

    protected void error(String message, Object... args) {
        super.error(message, args);
        state = ActionState.ERROR;
    }

    @Override
    public void gloryDone(Manager.Status m, Manager.ErrorDetail me) {
        Logger.debug("CountingAction When Done %s %s", m.name(), state.name());
        switch (state) {
            case CANCELING:
                // IDLE is when we canceled after an full escrow
                if (m != Manager.Status.CANCELED) {
                    error("CANCELING Invalid manager status %s", m.name());
                } else {
                    state = ActionState.FINISH;
                }
                break;
            case ESCROW_FULL:
                if (m != Manager.Status.IDLE) {
                    Logger.error("ESCROW_FULL Invalid manager status %s", m.name());
                }
                state = ActionState.IDLE;
                break;
            case READY_TO_STORE:
                if (m != Manager.Status.CANCELED) {
                    Logger.error("READY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                break;
            default:
                error("WhenDone invalid status %s %s %s", state.name(), m.name(), me);
                return;
        }
        if (state == ActionState.ERROR) {
            return;
        }

        Logger.debug("--------- esNewClasscrow full SAVE");
    }
}
