/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.DeviceFactory;
import devices.glory.manager.ManagerInterface;
import java.awt.print.PrinterException;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.print.PrintException;
import models.Configuration;
import models.EnvelopeDeposit;
import models.actions.states.IdleEnvelopeDeposit;
import models.db.LgEnvelope;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    static final EnumMap<ManagerInterface.State, String> messageMap = new EnumMap<ManagerInterface.State, String>(ManagerInterface.State.class);

    static {
        messageMap.put(ManagerInterface.State.PUT_THE_ENVELOPE_IN_THE_ESCROW, "envelope_deposit.put_the_envelope_in_the_escrow");
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
        try {
            Map renderArgs = new HashMap();
            // Print the ticket.
            List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
            List<Currency> currencies = Currency.findAll();
            renderArgs.put("formData", formData);
            renderArgs.put("referenceCodes", referenceCodes);
            renderArgs.put("currencies", currencies);
            renderArgs.put("depositId", currentDepositId);
            DeviceFactory.getPrinter().print("envelopeDeposit_start", renderArgs, 120);
        } catch (PrinterException ex) {
            Logger.error(ex.getMessage());
        } catch (PrintException ex) {
            Logger.error(ex.getMessage());
        }
    }

    @Override
    public void finish() {
        if (currentDepositId != null) {
            EnvelopeDeposit d = EnvelopeDeposit.findById(currentDepositId);
            if (d != null && d.finishDate != null) {
                Map renderArgs = new HashMap();
                renderArgs.put("clientCode", Configuration.getClientDescription());
                renderArgs.put("formData", formData);
                renderArgs.put("depositId", currentDepositId);
                renderArgs.put("envelopes", d.envelopes);

                try {
                    // Print the ticket.
                    DeviceFactory.getPrinter().print("envelopeDeposit_finish", renderArgs, 120);
                } catch (Throwable ex) {
                    Logger.debug(ex.getMessage());
                }
            }
        }
    }
}
