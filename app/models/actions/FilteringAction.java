/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import devices.glory.manager.Manager.ErrorDetail;
import devices.glory.manager.Manager.Status;
import models.lov.Currency;
import play.libs.F.Tuple;

/**
 *
 * @author adji
 */
public class FilteringAction extends UserAction {

    public Currency currency;

    public FilteringAction(Currency currency, Object formData) {
        super(formData);
        this.currency = currency;
    }

    @Override
    final public String getNeededController() {
        return "FilterController";
    }

//    public void start() {
//        if (getCurrentActionState() != ModelFacade.CurrentStep.NONE) {
//            error(String.format("startCounting Invalid step %s", actionState.name()));
//            return;
//        }
//        currentFormData = formData;
//        currentDeposit = new Deposit(Secure.getCurrentUser(), null, null, currency);
//        currentMode = ModelFacade.CurrentMode.COUNTING;
//        actionState = ModelFacade.CurrentStep.RUNNING;
//        if (!manager.count(whenDone, null, currency.numericId)) {
//            error("startCounting can't start glory");
//            return;
//        }
//    }
//    final private BillDepositAction.WhenGloryDone whenDone = new BillDepositAction.WhenGloryDone();
//
//    protected class WhenGloryDone implements Runnable {
//
//        public void run() {
//            synchronized (ModelFacade.this) {
//                Logger.debug("When Done %s %s %s", manager.getStatus().name(), actionState.name(), currentMode.name());
//                Manager.Status m = manager.getStatus();
//                if (actionState != ModelFacade.CurrentStep.RUNNING && actionState != ModelFacade.CurrentStep.ERROR) {
//                    // TODO: Log the event.
//                    Logger.error("WhenDone not running");
//                    actionState = ModelFacade.CurrentStep.ERROR;
//                    return;
//                }
//                actionState = ModelFacade.CurrentStep.FINISH;
//                switch (currentMode) {
//                    case ERROR_RECOVERY:
//                    case STORING_ERROR_RECOVERY:
//                        if (Deposit.em().getTransaction().isActive()) {
//                            Deposit.em().getTransaction().rollback();
//                        }
//                        currentDeposit = null;
//                        actionState = ModelFacade.CurrentStep.NONE;
//                        currentMode = CurrentMode.NONE;
//                        break;
//                    case ENVELOPE_DEPOSIT:
//                        switch (m) {
//                            case CANCELED:
//                                if (Deposit.em().getTransaction().isActive()) {
//                                    Deposit.em().getTransaction().rollback();
//                                }
//                                currentDeposit = null;
//                                break;
//
//                            case IDLE:
//                                Deposit.em().getTransaction().commit();
//                                Deposit.em().getTransaction().begin();
//                                currentDeposit.finishDate = new Date();
//                                currentDeposit.save();
//                                Logger.debug("--------- presave");
//                                if (Deposit.em().getTransaction().isActive()) {
//                                    Deposit.em().getTransaction().commit();
//                                    Deposit.em().getTransaction().begin();
//                                }
//                                break;
//                            case ERROR:
//                                Logger.error("WhenDone invalid machine error %s", manager.getErrorDetail().toString());
//                                actionState = ModelFacade.CurrentStep.ERROR;
//                                break;
//                            default:
//                                Logger.error("WhenDone invalid machine status %s", m);
//                                actionState = ModelFacade.CurrentStep.ERROR;
//                                break;
//                        }
//                        break;
//                    case COUNTING:
//                        if (m != Manager.Status.CANCELED) {
//                            Logger.error(String.format("WhenDone invalid manager status %s", m.name()));
//                            actionState = ModelFacade.CurrentStep.ERROR;
//                            return;
//                        }
//                        return;
//                    case BILL_DEPOSIT:
//                        switch (m) {
//                            case CANCELED:
//                                if (Deposit.em().getTransaction().isActive()) {
//                                    Deposit.em().getTransaction().rollback();
//                                }
//                                currentDeposit = null;
//                                break;
//
//                            case IDLE:
//                                Logger.debug("--------- idle SAVE");
//                                addBatchToDeposit();
//                                currentDeposit.finishDate = new Date();
//                                currentDeposit.merge();
//                                Logger.debug("--------- presave");
//                                if (Deposit.em().getTransaction().isActive()) {
//                                    Deposit.em().getTransaction().commit();
//                                    Deposit.em().getTransaction().begin();
//                                }
//                                break;
//                            case STORING: // ESCROW_FULL
//                                Logger.debug("--------- esNewClasscrow full SAVE");
//                                addBatchToDeposit();
//                                currentDeposit.merge();
//                                if (Deposit.em().getTransaction().isActive()) {
//                                    Deposit.em().getTransaction().commit();
//                                    Deposit.em().getTransaction().begin();
//                                }
//                                actionState = ModelFacade.CurrentStep.RUNNING;
//                                break;
//                            case ERROR:
//                                Logger.error("WhenDone invalid machine error %s", manager.getErrorDetail().toString());
//                                actionState = ModelFacade.CurrentStep.ERROR;
//                                break;
//                            default:
//                                Logger.error("WhenDone invalid machine status %s", m);
//                                actionState = ModelFacade.CurrentStep.ERROR;
//                                break;
//                        }
//                        break;
//                    default:
//                        Logger.error(String.format("WhenDone invalid step %s", actionState.name()));
//                        actionState = ModelFacade.CurrentStep.ERROR;
//                        break;
//                }
//            }
//        }
//    }

    @Override
    public String getControllerAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void gloryDone(Status m, ErrorDetail me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tuple<String, String> getActionState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
