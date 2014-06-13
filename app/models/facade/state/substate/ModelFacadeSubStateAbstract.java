/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facade.state.substate;

import machines.events.MachineEvent;
import models.facade.state.ModelFacadeStateAbstract;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class ModelFacadeSubStateAbstract {

    final protected ModelFacadeSubStateApi api;

//    final static protected Map<ManagerInterface.MANAGER_STATE, String> managerMessages = new EnumMap<ManagerInterface.MANAGER_STATE, String>(ManagerInterface.MANAGER_STATE.class);
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
    public ModelFacadeSubStateAbstract(ModelFacadeSubStateApi api) {
        this.api = api;
    }

    abstract public String getSubStateName();

    public String getNeededActionAction() {
        return "mainloop";
    }

    public boolean cancel() {
        Logger.error("Invalid call to cancel on state %s", getSubStateName());
        return false;
    }

    public boolean accept() {
        Logger.error("Invalid call to accept on state %s", getSubStateName());
        return false;
    }

    public void onMachineEvent(MachineEvent evt) {
        switch (evt.getStatus().getType()) {
            default:
                Logger.error("Invalid event status %s for state %s substate %s",
                        evt.getStatus(), this.toString(), toString());
                break;
        }
    }

//    public void cancelWithCause(LgDeposit.FinishCause cause) {
//
//        stateApi.closeDeposit(cause);
//        stateApi.cancelTimer();
//        stateApi.cancelDeposit();
//    }
//    abstract public void onGloryEvent(ManagerStatus m);
 /*   public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
     Logger.error("ActionState onIoBoardEvent %s", status.toString());
     if (!Configuration.isIgnoreShutter() && status.getShutterState() != IoBoard.SHUTTER_STATE.SHUTTER_CLOSED) {
     stateApi.setError(ModelError.ERROR_CODE.SHUTTER_NOT_CLOSED,
     String.format("ActionState shutter open %s %s", status.getShutterState().name(), getName()));
     }
     }

     public void onPrinterEvent(OSPrinter.PrinterStatus status) {
     Logger.error("ActionState invalid onPrinterEvent %s", status.toString());
     }

     // used by timeout state.
     public void suspendTimeout() {
     Logger.debug("ActionState suspendTimeout %s", getName());
     }
     */
    public String getMessage(ModelFacadeStateAbstract userAction) {
        return null;
        //return managerMessages.get(stateApi.getManagerState());
    }


    /*
     public boolean isReadyToAccept(boolean envelope) {
     ItemQuantity iq = api.getMachine().getCurrentItemQuantity();
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
     //            delayedStore = true;
     //stateApi.setState(new BagRemoved(stateApi, this));
     cancelWithCause(LgDeposit.FinishCause.FINISH_CAUSE_BAG_REMOVED);
     return false;
     }
     return true;
     }
     */
    public String getNeededAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean canFinishAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean finish() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
