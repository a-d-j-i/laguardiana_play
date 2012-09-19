/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.glory.manager.Manager;
import java.util.Date;
import models.Deposit;
import models.db.LgBatch;
import models.db.LgBill;
import models.db.LgEnvelope;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.libs.F;
import validation.Bill;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    static public enum ActionState {

        ERROR,
        IDLE,
        READY_TO_STORE,
        FINISH,
        CANCELING,;
    };
    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public LgEnvelope envelope;
    public Deposit currentDeposit = null;
    public ActionState state = ActionState.IDLE;

    public EnvelopeDepositAction(DepositUserCodeReference userCodeLov, String userCode, LgEnvelope envelope, Object formData) {
        super(formData);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.envelope = envelope;
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
                case PUT_THE_ENVELOPE_IN_THE_ESCROW:
                    state = ActionState.READY_TO_STORE;
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
        return "EnvelopeDepositController";
    }

    @Override
    public void start() {
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov, null);
        currentDeposit.addEnvelope(envelope);
        if (!userActionApi.envelopeDeposit()) {
            state = ActionState.ERROR;
            error = String.format("startBillDeposit can't start glory %s", userActionApi.getErrorDetail());
        }
    }

    public void acceptDeposit() {
        if (state != ActionState.READY_TO_STORE || currentDeposit == null) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        if (!userActionApi.storeDeposit(currentDeposit.depositId)) {
            Logger.error("startBillDeposit can't cancel glory");
        }
    }

    public void cancelDeposit() {
        if (currentDeposit == null) {
            Logger.error("cancelDeposit Invalid step %s", state.name());
            return;
        }
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
        Logger.debug("BillDepositAction When Done %s %s", m.name(), state.name());
        switch (state) {
            case CANCELING:
                if (m != Manager.Status.CANCELED) {
                    error("CANCELING Invalid manager status %s", m.name());
                } else {
                    state = ActionState.FINISH;
                    currentDeposit = null;
                }
                break;
            case READY_TO_STORE:
                if (m != Manager.Status.IDLE) {
                    Logger.error("READY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                currentDeposit.finishDate = new Date();
                break;

            default:
                error("WhenDone invalid status %s %s %s", state.name(), m.name(), me);
                return;
        }
        if (state == ActionState.ERROR) {
            currentDeposit = null;
            return;
        }

        Logger.debug("--------- esNewClasscrow full SAVE");
        if (currentDeposit == null) {
            Logger.error("addBatchToDeposit current deposit is null");
            return;
        }
        currentDeposit.save();
    }
}
