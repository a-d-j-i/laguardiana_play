/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.glory.manager.Manager;
import java.util.Date;
import models.Deposit;
import models.ModelFacade;
import models.db.LgEnvelope;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.libs.F.Tuple;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public LgEnvelope envelope;
    public Deposit currentDeposit = null;

    public EnvelopeDepositAction(DepositUserCodeReference userCodeLov, String userCode, LgEnvelope envelope, Object formData) {
        super(formData);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.envelope = envelope;
    }
    @Override
    final public String getNeededController() {
        return "EnvelopeDepositController";
    }

    public void start() {

        // TODO: Generalize, at start fromData is a kind on context saved
        //synchronized 
    }
        
    public void depositEnvelope(Object formData ) {
        /*
        if (getCurrentActionState() != ModelFacade.ActionState.START) {
            error(String.format("depositEnvelope Invalid step %s", actionState.name()));
            return;
        }
        currentFormData = formData;
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov, null);
        currentDeposit.addEnvelope(envelope);
        currentMode = ModelFacade.CurrentMode.ENVELOPE_DEPOSIT;
        actionState = ModelFacade.ActionState.RUNNING;
        if (!manager.envelopeDeposit(whenDone)) {
            error("depositEnvelope can't start glory");
            return;
        } */
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, 
                                                    userCodeLov, null);
        currentDeposit.save();
        Deposit.em().getTransaction().commit();
        Deposit.em().getTransaction().begin();
        //actionState = ActionState.RUNNING;
        // TODO this!
//        if (!userActionApi.count(currency.numericId)) {
//            actionState = ActionState.ERROR;
//            error = "startBillDeposit can't start glory";
//        }
    }

    
    @Override
    public void gloryDone(Manager.Status m, Manager.ErrorDetail me) {
//        Logger.debug("EnvelopeDepositAction When Done %s %s", m.name(), actionState.name());
    }
    /*
    final private BillDepositAction.WhenGloryDone whenDone = new BillDepositAction.WhenGloryDone();

    protected class WhenGloryDone implements Runnable {

        public void run() {
            synchronized (ModelFacade.this) {
                Logger.debug("When Done %s %s %s", manager.getStatus().name(), actionState.name(), currentMode.name());
                Manager.Status m = manager.getStatus();
                if (actionState != ModelFacade.ActionState.RUNNING && actionState != ModelFacade.ActionState.ERROR) {
                    // TODO: Log the event.
                    Logger.error("WhenDone not running");
                    actionState = ModelFacade.ActionState.ERROR;
                    return;
                }
                actionState = ModelFacade.ActionState.FINISH;
                switch (currentMode) {
                    case ERROR_RECOVERY:
                    case STORING_ERROR_RECOVERY:
                        if (Deposit.em().getTransaction().isActive()) {
                            Deposit.em().getTransaction().rollback();
                        }
                        currentDeposit = null;
                        actionState = ModelFacade.ActionState.START;
                        currentMode = CurrentMode.START;
                        break;
                    case ENVELOPE_DEPOSIT:
                        switch (m) {
                            case CANCELED:
                                if (Deposit.em().getTransaction().isActive()) {
                                    Deposit.em().getTransaction().rollback();
                                }
                                currentDeposit = null;
                                break;

                            case IDLE:
                                Deposit.em().getTransaction().commit();
                                Deposit.em().getTransaction().begin();
                                currentDeposit.finishDate = new Date();
                                currentDeposit.save();
                                Logger.debug("--------- presave");
                                if (Deposit.em().getTransaction().isActive()) {
                                    Deposit.em().getTransaction().commit();
                                    Deposit.em().getTransaction().begin();
                                }
                                break;
                            case ERROR:
                                Logger.error("WhenDone invalid machine error %s", manager.getErrorDetail().toString());
                                actionState = ModelFacade.ActionState.ERROR;
                                break;
                            default:
                                Logger.error("WhenDone invalid machine status %s", m);
                                actionState = ModelFacade.ActionState.ERROR;
                                break;
                        }
                        break;
                    case COUNTING:
                        if (m != Manager.Status.CANCELED) {
                            Logger.error(String.format("WhenDone invalid manager status %s", m.name()));
                            actionState = ModelFacade.ActionState.ERROR;
                            return;
                        }
                        return;
                    case BILL_DEPOSIT:
                        switch (m) {
                            case CANCELED:
                                if (Deposit.em().getTransaction().isActive()) {
                                    Deposit.em().getTransaction().rollback();
                                }
                                currentDeposit = null;
                                break;

                            case IDLE:
                                Logger.debug("--------- idle SAVE");
                                addBatchToDeposit();
                                currentDeposit.finishDate = new Date();
                                currentDeposit.merge();
                                Logger.debug("--------- presave");
                                if (Deposit.em().getTransaction().isActive()) {
                                    Deposit.em().getTransaction().commit();
                                    Deposit.em().getTransaction().begin();
                                }
                                break;
                            case STORING: // ESCROW_FULL
                                Logger.debug("--------- esNewClasscrow full SAVE");
                                addBatchToDeposit();
                                currentDeposit.merge();
                                if (Deposit.em().getTransaction().isActive()) {
                                    Deposit.em().getTransaction().commit();
                                    Deposit.em().getTransaction().begin();
                                }
                                actionState = ModelFacade.ActionState.RUNNING;
                                break;
                            case ERROR:
                                Logger.error("WhenDone invalid machine error %s", manager.getErrorDetail().toString());
                                actionState = ModelFacade.ActionState.ERROR;
                                break;
                            default:
                                Logger.error("WhenDone invalid machine status %s", m);
                                actionState = ModelFacade.ActionState.ERROR;
                                break;
                        }
                        break;
                    default:
                        Logger.error(String.format("WhenDone invalid step %s", actionState.name()));
                        actionState = ModelFacade.ActionState.ERROR;
                        break;
                }
            }
        }
        
    }*/

    @Override
    public String getControllerAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tuple<String, String> getActionState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
