/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.glory.manager.ManagerInterface;
import java.util.Date;
import java.util.EnumMap;
import models.EnvelopeDeposit;
import models.actions.states.EnvelopeDepositStart;
import models.db.LgEnvelope;
import models.lov.DepositUserCodeReference;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    static final EnumMap<ManagerInterface.MANAGER_STATE, String> messageMap = new EnumMap<ManagerInterface.MANAGER_STATE, String>(ManagerInterface.MANAGER_STATE.class);

    static {
        messageMap.put(ManagerInterface.MANAGER_STATE.PUT_THE_ENVELOPE_IN_THE_ESCROW, "envelope_deposit.put_the_envelope_in_the_escrow");
    }
    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public LgEnvelope envelope;

    public EnvelopeDepositAction(DepositUserCodeReference userCodeLov, String userCode, LgEnvelope envelope, Object formData) {
        super(null, formData, messageMap);
        this.userCodeLov = userCodeLov;
        this.userCode = userCode;
        this.envelope = envelope;
        state = new EnvelopeDepositStart(new StateApi());
    }

    @Override
    final public String getNeededController() {
        return "EnvelopeDepositController";
    }

    @Override
    public void start() {
        EnvelopeDeposit deposit = new EnvelopeDeposit(Secure.getCurrentUser(), userCode, userCodeLov);
        deposit.startDate = new Date();
        deposit.addEnvelope(envelope);
        deposit.save();
        currentDepositId = deposit.depositId;
        userActionApi.envelopeDeposit();
        deposit.printStart(userActionApi.getPrinter());
    }

    @Override
    public void finish() {
        if (currentDepositId != null) {
            EnvelopeDeposit d = EnvelopeDeposit.findById(currentDepositId);
            if (d != null && d.finishDate != null) {
                d.print(userActionApi.getPrinter(), false);
            }
        }
    }
}
