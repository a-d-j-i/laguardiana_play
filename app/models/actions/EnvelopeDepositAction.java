/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.glory.manager.Manager;
import devices.glory.manager.Manager.ErrorDetail;
import devices.glory.manager.Manager.Status;
import java.util.Date;
import models.Deposit;
import models.ModelFacade;
import models.db.LgEnvelope;
import models.lov.DepositUserCodeReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public LgEnvelope envelope;

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
        if (getCurrentStep() != ModelFacade.CurrentStep.NONE) {
            Logger.error(String.format("depositEnvelope Invalid step %s", currentStep.name()));
            return;
        }
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov, null);
        currentDeposit.addEnvelope(envelope);
        currentMode = ModelFacade.CurrentMode.ENVELOPE_DEPOSIT;
        currentStep = ModelFacade.CurrentStep.RUNNING;
        if (!manager.envelopeDeposit(whenDone)) {
            error("depositEnvelope can't start glory");
            return;
        }
    }
    
        final private BillDepositAction.WhenGloryDone whenDone = new BillDepositAction.WhenGloryDone();

    @Override
    public void gloryDone(Status m, ErrorDetail me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected class WhenGloryDone implements Runnable {

        public void run() {
            synchronized (ModelFacade.this) {
                Logger.debug("When Done %s %s %s", manager.getStatus().name(), currentStep.name(), currentMode.name());
                Manager.Status m = manager.getStatus();
                if (currentStep != ModelFacade.CurrentStep.RUNNING && currentStep != ModelFacade.CurrentStep.ERROR) {
                    // TODO: Log the event.
                    Logger.error("WhenDone not running");
                    currentStep = ModelFacade.CurrentStep.ERROR;
                    return;
                }
                currentStep = ModelFacade.CurrentStep.FINISH;
                switch (currentMode) {
                    case ERROR_RECOVERY:
                    case STORING_ERROR_RECOVERY:
                        if (Deposit.em().getTransaction().isActive()) {
                            Deposit.em().getTransaction().rollback();
                        }
                        currentDeposit = null;
                        currentStep = ModelFacade.CurrentStep.NONE;
                        currentMode = CurrentMode.NONE;
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
                                currentStep = ModelFacade.CurrentStep.ERROR;
                                break;
                            default:
                                Logger.error("WhenDone invalid machine status %s", m);
                                currentStep = ModelFacade.CurrentStep.ERROR;
                                break;
                        }
                        break;
                    case COUNTING:
                        if (m != Manager.Status.CANCELED) {
                            Logger.error(String.format("WhenDone invalid manager status %s", m.name()));
                            currentStep = ModelFacade.CurrentStep.ERROR;
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
                                currentStep = ModelFacade.CurrentStep.RUNNING;
                                break;
                            case ERROR:
                                Logger.error("WhenDone invalid machine error %s", manager.getErrorDetail().toString());
                                currentStep = ModelFacade.CurrentStep.ERROR;
                                break;
                            default:
                                Logger.error("WhenDone invalid machine status %s", m);
                                currentStep = ModelFacade.CurrentStep.ERROR;
                                break;
                        }
                        break;
                    default:
                        Logger.error(String.format("WhenDone invalid step %s", currentStep.name()));
                        currentStep = ModelFacade.CurrentStep.ERROR;
                        break;
                }
            }
        }
    }

}
