/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;
//TODO: This is wrong, the current user must be known.
import controllers.Secure;
import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.Date;
import java.util.List;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgEnvelope;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import validation.Bill;

/**
 * TODO: Review this with another thread/job that has a input queue for events
 * and react according to events from the glory and the electronics in the cage.
 *
 * @author aweil
 */
public class ModelFacade {
    // FACTORY

    static private ModelFacade THIS = new ModelFacade();

    static public ModelFacade get() {
        return THIS;
    }

    // Used by the controllers to fix the current page.
    // The finish xxxxx_finish states can be changed by a mode + a single finish state
    static public enum CurrentStep {

        RESERVED,
        ERROR,
        NONE,
        RUNNING,
        FINISH,;
    }

    static public enum CurrentMode {

        NONE,
        BILL_DEPOSIT,
        ENVELOPE_DEPOSIT,
        COUNTING,
        FILTERING,;
    }
    private Manager.ControllerApi manager = CounterFactory.getGloryManager();
    private CurrentStep currentStep = CurrentStep.NONE;
    private CurrentMode currentMode = CurrentMode.NONE;
    private Deposit currentDeposit = null;
    private Object currentFormData = null;

    private void error(String msg) {
        Logger.error("ERROR IN ModelFacade %s", msg);
        throw new play.mvc.results.Error(msg);
    }

    synchronized public CurrentMode getCurrentMode() {
        return currentMode;
    }

    synchronized public CurrentStep getCurrentStep() {
        if (currentDeposit == null || currentDeposit.user == null) {
            // unfinished Cancelation.
            if (currentStep == CurrentStep.FINISH) {
                return currentStep;
            }
            if (currentStep != CurrentStep.NONE) {
                error(String.format("getCurrentStep Invalid step %s", currentStep.name()));
            }
            return CurrentStep.NONE;
        }
        if (currentDeposit.user.equals(Secure.getCurrentUser())) {
            return currentStep;
        } else {
            return CurrentStep.RESERVED;
        }
    }
    final private WhenDone whenDone = new WhenDone();

    protected class WhenDone implements Runnable {

        public void run() {
            synchronized (ModelFacade.this) {
                Logger.debug("When Done %s %s %s", manager.getStatus().name(), currentStep.name(), currentMode.name());
                Manager.Status m = manager.getStatus();
                if (currentStep != CurrentStep.RUNNING) {
                    // TODO: Log the event.
                    Logger.error("WhenDone not running");
                    currentStep = CurrentStep.ERROR;
                    return;
                }
                currentStep = CurrentStep.FINISH;
                switch (currentMode) {
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
                                currentStep = CurrentStep.ERROR;
                                break;
                            default:
                                Logger.error("WhenDone invalid machine status %s", m);
                                currentStep = CurrentStep.ERROR;
                                break;
                        }
                        break;
                    case COUNTING:
                        if (m != Manager.Status.CANCELED) {
                            Logger.error(String.format("WhenDone invalid manager status %s", m.name()));
                            currentStep = CurrentStep.ERROR;
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
                                Logger.debug("--------- escrow full SAVE");
                                addBatchToDeposit();
                                currentDeposit.merge();
                                if (Deposit.em().getTransaction().isActive()) {
                                    Deposit.em().getTransaction().commit();
                                    Deposit.em().getTransaction().begin();
                                }
                                currentStep = CurrentStep.RUNNING;
                                break;
                            case ERROR:
                                Logger.error("WhenDone invalid machine error %s", manager.getErrorDetail().toString());
                                currentStep = CurrentStep.ERROR;
                                break;
                            default:
                                Logger.error("WhenDone invalid machine status %s", m);
                                currentStep = CurrentStep.ERROR;
                                break;
                        }
                        break;
                    default:
                        Logger.error(String.format("WhenDone invalid step %s", currentStep.name()));
                        currentStep = CurrentStep.ERROR;
                        break;
                }
            }
        }
    }

    // TODO: Generalize, at start fromData is a kind on context saved
    synchronized public void depositEnvelope(Object formData, DepositUserCodeReference userCodeLov, String userCode, LgEnvelope envelope) {
        if (getCurrentStep() != CurrentStep.NONE) {
            error(String.format("depositEnvelope Invalid step %s", currentStep.name()));
            return;
        }
        currentFormData = formData;
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov, null);
        currentDeposit.addEnvelope(envelope);
        currentMode = CurrentMode.ENVELOPE_DEPOSIT;
        currentStep = CurrentStep.RUNNING;
        if (!manager.envelopeDeposit(whenDone)) {
            error("depositEnvelope can't start glory");
            return;
        }
    }

    synchronized public void startCounting(Object formData, Currency currency) {
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

    synchronized public void startBillDeposit(Object formData, DepositUserCodeReference userCodeLov, String userCode, Currency currency) {
        if (getCurrentStep() != CurrentStep.NONE) {
            error(String.format("startBillDeposit Invalid step %s", currentStep.name()));
            return;
        }
        currentFormData = formData;
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov, currency);
        currentDeposit.save();
        Deposit.em().getTransaction().commit();
        Deposit.em().getTransaction().begin();
        currentMode = CurrentMode.BILL_DEPOSIT;
        currentStep = CurrentStep.RUNNING;
        if (!manager.count(whenDone, null, currency.numericId)) {
            error("startBillDeposit can't start glory");
            return;
        }
    }

    synchronized public void acceptDeposit() {
        if (getCurrentStep() != CurrentStep.RUNNING || currentDeposit == null
                || (currentMode != CurrentMode.BILL_DEPOSIT && currentMode != CurrentMode.ENVELOPE_DEPOSIT)) {
            error("cancelBillDeposit Invalid step");
            return;
        }
        if (!manager.storeDeposit(currentDeposit.depositId)) {
            error("startBillDeposit can't cancel glory");
        }
    }

    synchronized public void cancelDeposit() {
        CurrentStep c = getCurrentStep();
        if (currentDeposit == null || c != CurrentStep.RUNNING) {
            error(String.format("cancelDeposit Invalid step %s", c.name()));
            return;
        }
        if (!manager.cancelDeposit(whenDone)) {
            error("cancelDeposit can't cancel glory");
        }
    }

    synchronized public void finishDeposit() {
        CurrentStep c = getCurrentStep();
        if (c != CurrentStep.FINISH) {
            error(String.format("finishDeposit Invalid step %s", c.name()));
            return;
        }
        // Allready detached.
        currentDeposit = null;
        currentFormData = null;
        currentStep = CurrentStep.NONE;
        currentMode = CurrentMode.NONE;
    }

    private Boolean checkCurrentStep() {
        CurrentStep s = getCurrentStep();
        if (s != CurrentStep.RUNNING && s != CurrentStep.FINISH) {
            error(String.format("checkCurrentStep Invalid step %s", currentStep.name()));
            return false;
        }
        if (currentDeposit == null) {
            return false;
        }
        return true;
    }

    synchronized public Object getFormData() {
        return currentFormData;
    }

    synchronized public List<Bill> getBillData() {
        if (checkCurrentStep()) {
            return Bill.getCurrentCounters(currentDeposit.currency.numericId);
        }
        return null;
    }

    synchronized public String getStatus() {
        if (currentStep == CurrentStep.RUNNING) {
            return manager.getStatus().name();
        } else {
            return currentStep.name();
        }
    }

    synchronized public String getDepositTotal() {
        if (currentDeposit != null) {
            Logger.error("TOTAL : %d", currentDeposit.getTotal());
            return currentDeposit.getTotal().toString();
        }
        return null;
    }

    // TODO: Manage the db error here.
    private void addBatchToDeposit() {
        if (currentDeposit == null) {
            error("addBatchToDeposit current deposit is null");
            return;
        }
        LgBatch batch = new LgBatch();
        for (Bill bill : Bill.getCurrentCounters(currentDeposit.currency.numericId)) {
            Logger.debug(" -> quantity %d", bill.quantity);
            LgBill b = new LgBill(bill.quantity, bill.billType);
            batch.addBill(b);
        }
        currentDeposit.addBatch(batch);
        batch.save();
    }
}
