/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import devices.ioboard.IoBoard;
import devices.printer.OSPrinter;
import java.util.EnumMap;
import java.util.Map;
import models.Configuration;
import models.ItemQuantity;
import models.ModelError;
import models.actions.TimeoutTimer;
import models.actions.UserAction;
import models.actions.UserAction.StateApi;
import models.db.LgBill;
import models.db.LgDeposit;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class ActionState {

//    final static protected Map<ManagerInterface.MANAGER_STATE, String> managerMessages = new EnumMap<ManagerInterface.MANAGER_STATE, String>(ManagerInterface.MANAGER_STATE.class);
    final protected StateApi stateApi;
    /*
     static {
     managerMessages.put(ManagerInterface.MANAGER_STATE.PUT_THE_BILLS_ON_THE_HOPER, "counting_page.put_the_bills_on_the_hoper");
     managerMessages.put(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_ESCROW, "counting_page.remove_the_bills_from_escrow");
     managerMessages.put(ManagerInterface.MANAGER_STATE.REMOVE_REJECTED_BILLS, "counting_page.remove_rejected_bills");
     managerMessages.put(ManagerInterface.MANAGER_STATE.REMOVE_THE_BILLS_FROM_HOPER, "counting_page.remove_the_bills_from_hoper");
     managerMessages.put(ManagerInterface.MANAGER_STATE.CANCELING, "application.canceling");
     //messages.put(ManagerInterface.Status.CANCELED, "counting_page.deposit_canceled");
     managerMessages.put(ManagerInterface.MANAGER_STATE.ERROR, "application.error");
     managerMessages.put(ManagerInterface.MANAGER_STATE.JAM, "application.jam");
     }
     */

    public ActionState(StateApi stateApi) {
        this.stateApi = stateApi;
    }

    abstract public String name();

    public String getNeededActionAction() {
        return "mainLoop";
    }

    public void start() {
        Logger.error("ActionState start Invalid step %s", name());
    }

    public void cancel() {
        Logger.error("ActionState cancel Invalid step %s", name());
    }

    public void accept() {
        Logger.error("ActionState accept Invalid step %s", name());
    }

    public void cancelWithCause(LgDeposit.FinishCause cause) {
        stateApi.closeDeposit(cause);
        stateApi.cancelTimer();
        stateApi.cancelDeposit();
    }

    public boolean isReadyToAccept(boolean envelope) {
        ItemQuantity iq = stateApi.getCurrentItemQuantity();
        if (envelope) {
            //iq.envelopes++;
        } else {
            for (LgBill bill : stateApi.getCurrentBillList()) {
                iq.bills += bill.quantity;
            }
        }

        // During bill deposit I can make the bag full, but not more than that.
        iq.bills--;
        Logger.debug("isReadyToAccept count : %s", iq.toString());
        if (Configuration.isBagFull(iq.bills, iq.envelopes)) {
            cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_FULL);
            return false;
        }
        if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
            /*            delayedStore = true;
             stateApi.setState(new BagRemoved(stateApi, this));*/
            cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
            return false;
        }
        return true;
    }

//    abstract public void onGloryEvent(ManagerStatus m);
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("ActionState onIoBoardEvent %s", status.toString());
        if (!Configuration.isIgnoreShutter() && status.getShutterState() != IoBoard.SHUTTER_STATE.SHUTTER_CLOSED) {
            stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_CLOSED,
                    String.format("ActionState shutter open %s %s", status.getShutterState().name(), name()));
        }
    }

    public void onPrinterEvent(OSPrinter.PrinterStatus status) {
        Logger.error("ActionState invalid onPrinterEvent %s", status.toString());
    }

    public void onTimeoutEvent(TimeoutTimer timer) {
        Logger.error("ActionState invalid onTimeoutEvent %s", name());
    }

    // used by timeout state.
    public void suspendTimeout() {
        Logger.debug("ActionState suspendTimeout %s", name());
    }

    public boolean canFinishAction() {
        return false;
    }

    public String getMessage(UserAction userAction) {
        return null;
        //return managerMessages.get(stateApi.getManagerState());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ActionState other = (ActionState) obj;
        return (this.name().equals(other.name()));
    }
}
