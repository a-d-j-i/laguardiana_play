/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.IoBoard;
import devices.glory.manager.GloryManager;
import java.util.Date;
import java.util.EnumMap;
import models.Deposit;
import models.db.LgEnvelope;
import models.lov.DepositUserCodeReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    static final EnumMap<GloryManager.Status, String> messageMap = new EnumMap<GloryManager.Status, String>(GloryManager.Status.class);

    static {
        messageMap.put(GloryManager.Status.PUT_THE_ENVELOPE_IN_THE_ESCROW, "envelope_deposit.put_the_envelope_in_the_escrow");
        messageMap.put(GloryManager.Status.CANCELING, "counting_page.canceling");
        messageMap.put(GloryManager.Status.CANCELED, "counting_page.deposit_canceled");
        messageMap.put(GloryManager.Status.ERROR, "application.error");
    }
    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public LgEnvelope envelope;

    public EnvelopeDepositAction(DepositUserCodeReference userCodeLov, String userCode, LgEnvelope envelope, Object formData) {
        super(null, formData, messageMap);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.envelope = envelope;
    }

    @Override
    final public String getActionNeededController() {
        return "EnvelopeDepositController";
    }

    @Override
    public void start() {
        Deposit deposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov);
        deposit.addEnvelope(envelope);
        deposit.save();
        currentDepositId = deposit.depositId;
        userActionApi.envelopeDeposit();
    }

    @Override
    public void accept() {
        if (state != ActionState.READY_TO_STORE) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        Deposit d = Deposit.findById(currentDepositId);
        d.startDate = new Date();
        d.save();
        if (!userActionApi.store(currentDepositId)) {
            Logger.error("startEnvelopeDeposit can't cancel glory");
        }
    }

    @Override
    public void cancel() {
        if (state != ActionState.READY_TO_STORE) {
            Logger.error("cancel Invalid step");
            return;
        }
        state = ActionState.CANCELING;
        if (!userActionApi.cancelDeposit()) {
            Logger.error("cancelDeposit can't cancel glory");
        }
    }

    @Override
    public void onGloryEvent(GloryManager.Status m) {
        Logger.debug("EnvelopeDepositAction When Done %s %s", m.name(), state.name());
        switch (state) {
            case IDLE:
                switch (m) {
                    case PUT_THE_ENVELOPE_IN_THE_ESCROW:
                        state = ActionState.READY_TO_STORE;
                        break;
                    default:
                        Logger.debug("getControllerAction Current manager state %s %s", state.name(), m.name());
                        break;
                }
                break;
            case CANCELING:
                if (m != GloryManager.Status.CANCELED) {
                    Logger.error("CANCELING Invalid manager status %s", m.name());
                } else {
                    state = ActionState.FINISH;
                }
                break;
            case READY_TO_STORE:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("READY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                Deposit d = Deposit.findById(currentDepositId);
                d.finishDate = new Date();
                d.save();
                break;
            default:
                Logger.error("WhenDone invalid status %s %s", state.name(), m.name());
                break;
        }
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.debug("CountingAction ioBoardEvent %s %s", status.status.name(), state.name());
    }

    @Override
    public void onTimeoutEvent(Timeout timeout, ActionState startState) {
        Logger.debug("CountingAction timeoutEvent");
    }

    @Override
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
