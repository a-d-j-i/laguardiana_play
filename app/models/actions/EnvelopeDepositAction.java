/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import controllers.Secure;
import devices.DeviceFactory;
import devices.glory.manager.GloryManager;
import java.awt.print.PrinterException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.print.PrintException;
import models.Deposit;
import models.actions.states.IdleEnvelopeDeposit;
import models.db.LgEnvelope;
import models.db.LgSystemProperty;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDepositAction extends UserAction {

    static final EnumMap<GloryManager.State, String> messageMap = new EnumMap<GloryManager.State, String>(GloryManager.State.class);

    static {
        messageMap.put(GloryManager.State.PUT_THE_ENVELOPE_IN_THE_ESCROW, "envelope_deposit.put_the_envelope_in_the_escrow");
        messageMap.put(GloryManager.State.CANCELING, "application.canceling");
        messageMap.put(GloryManager.State.CANCELED, "counting_page.deposit_canceled");
        messageMap.put(GloryManager.State.ERROR, "application.error");
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
        Deposit deposit = new Deposit(Secure.getCurrentUser(), userCode, userCodeLov);
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
            DeviceFactory.getPrinter().print("envelopeDeposit_start", renderArgs, 110);
        } catch (PrinterException ex) {
            Logger.error(ex.getMessage());
        } catch (PrintException ex) {
            Logger.error(ex.getMessage());
        }
    }

    @Override
    public void finish() {
        if (currentDepositId != null) {
            Deposit d = Deposit.findById(currentDepositId);
            if (d != null && d.finishDate != null) {
                Map renderArgs = new HashMap();
                renderArgs.put("clientCode", LgSystemProperty.getProperty(LgSystemProperty.Types.CLIENT_CODE));
                renderArgs.put("formData", formData);
                renderArgs.put("depositId", currentDepositId);

                try {
                    // Print the ticket.
                    DeviceFactory.getPrinter().print("envelopeDeposit_finish", renderArgs);
                } catch (Throwable ex) {
                    Logger.debug(ex.getMessage());
                }
            }
        }
    }
}
