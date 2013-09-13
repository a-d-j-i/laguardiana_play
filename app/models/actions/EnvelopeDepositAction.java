/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import java.util.Date;
import models.EnvelopeDeposit;
import models.actions.states.EnvelopeDepositStart;
import models.db.LgEnvelope;
import models.lov.DepositUserCodeReference;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    public DepositUserCodeReference userCodeLov;
    public String userCode;
    public LgEnvelope envelope;

    public EnvelopeDepositAction(DepositUserCodeReference userCodeLov, String userCode, LgEnvelope envelope, Object formData) {
        super(null, formData);
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
        deposit.printStart();
    }

    @Override
    public void finish() {
        if (currentDepositId != null) {
            EnvelopeDeposit d = EnvelopeDeposit.findById(currentDepositId);
            if (d != null) {
                if (d.closeDate != null && !d.canceled) {
                    d.print(false);
                }
                d.finishDate = new Date();
                d.save();
            }
        }
    }
}
