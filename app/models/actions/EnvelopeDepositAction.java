/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.glory.manager.GloryManager;
import java.util.EnumMap;
import models.Deposit;
import models.actions.states.IdleEnvelopeDeposit;
import models.db.LgEnvelope;
import models.lov.DepositUserCodeReference;

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
        state = new IdleEnvelopeDeposit(new StateApi());
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
}
