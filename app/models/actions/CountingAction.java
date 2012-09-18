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
import models.ModelFacade.CurrentMode;
import models.lov.Currency;
import play.Logger;

/**
 *
 * @author adji
 */
public class CountingAction extends UserAction {

    public Currency currency;

    public CountingAction(Currency currency, Object formData) {
        super(formData);
        this.currency = currency;
    }

    @Override
    final public String getNeededController() {
        return "CountController";
    }

    public void start() {
        if (getCurrentStep() != CurrentStep.NONE) {
            error(String.format("startCounting Invalid step %s", currentStep.name()));
            return;
        }
        currentFormData = formData;
        currentDeposit = new Deposit(Secure.getCurrentUser(), null, null, currency);
        currentMode = CurrentMode.COUNTING;
        currentStep = CurrentStep.RUNNING;
        if (!manager.count(whenDone, null, currency.numericId)) {
            error("startCounting can't start glory");
            return;
        }
    }
    final private BillDepositAction.WhenGloryDone whenDone = new BillDepositAction.WhenGloryDone();

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
