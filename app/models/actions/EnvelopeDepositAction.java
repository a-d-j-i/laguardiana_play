/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.glory.manager.GloryManager;
import devices.io_board.IoBoard;
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
        super(formData, messageMap);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.envelope = envelope;
    }

    @Override
    final public String getNeededController() {
        return "EnvelopeDepositController";
    }

    @Override
    public void start() {
        currentDeposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov, null);
        currentDeposit.addEnvelope(envelope);
        currentDeposit.save();
        if (!userActionApi.envelopeDeposit()) {
            state = ActionState.ERROR;
            error = String.format("startEnvelopeDeposit can't start glory %s", userActionApi.getErrorDetail());
        }
    }

    public void acceptDeposit() {
        if (state != ActionState.READY_TO_STORE || currentDeposit == null) {
            Logger.error("acceptDeposit Invalid step");
            return;
        }
        if (!userActionApi.storeDeposit(currentDeposit.depositId)) {
            Logger.error("startEnvelopeDeposit can't cancel glory");
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

    @Override
    public void gloryDone(GloryManager.Status m, GloryManager.ErrorDetail me) {
        Logger.debug("EnvelopeDepositAction When Done %s %s", m.name(), state.name());
        if (m == GloryManager.Status.ERROR) {
            error("Glory Error : %s", me);
            return;
        }
        switch (state) {
            case IDLE:
                switch (m) {
                    case PUT_THE_ENVELOPE_IN_THE_ESCROW:
                        state = ActionState.READY_TO_STORE;
                        break;
                    default:
                        Logger.debug("getControllerAction Current manager state %s %s",
                                state.name(), userActionApi.getManagerStatus().name());
                        break;
                }
                break;
            case CANCELING:
                if (m != GloryManager.Status.CANCELED) {
                    error("CANCELING Invalid manager status %s", m.name());
                } else {
                    state = ActionState.FINISH;
                }
                currentDeposit = null;
                break;
            case READY_TO_STORE:
                if (m != GloryManager.Status.IDLE) {
                    Logger.error("READY_TO_STORE Invalid manager status %s", m.name());
                }
                state = ActionState.FINISH;
                currentDeposit.finishDate = new Date();
                currentDeposit.merge();
                currentDeposit = null;
                break;
            default:
                error("WhenDone invalid status %s %s %s", state.name(), m.name(), me);
                break;
        }
    }

    @Override
    public void ioBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.debug("CountingAction ioBoardEvent %s %s", status.status.name(), state.name());
    }
}
