package controllers;

import devices.CounterFactory;
import devices.glory.manager.Manager;
import java.util.Date;
import java.util.List;
import models.Deposit;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import models.lov.EnvelopeType;
import play.Logger;
import play.mvc.Router;
import play.mvc.With;

@With(Secure.class)
public class EnvelopeDepositController extends BaseController {

    public static void index() {
        Application.index();
    }

    public static void inputReference(String reference1, String reference2) throws Throwable {

        Boolean r1 = isProperty("envelope_deposit.show_reference1");
        Boolean r2 = isProperty("envelope_deposit.show_reference2");

        if (validateReference(r1, r2, reference1, reference2)) {
            getEnvelopeContents();
        }
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("showReference1", r1);
        renderArgs.put("showReference2", r2);
        render(referenceCodes, currencies);

    }

    public static void getEnvelopeContents() {
        if (true) {
            confirmDeposit(null);
            return;
        }
        List<EnvelopeType> envelopeTypes = EnvelopeType.findAll();
        renderArgs.put("envelopeTypes", envelopeTypes);
        render();
    }

    public static void addCash(Integer currency, Integer amount) {

        if (currency != null || amount != null) {
            getEnvelopeContents();
            return;
        }
        List<Currency> currencies = Currency.findAll();
        render(currencies);
    }

    public static void addDocument() {
        getEnvelopeContents();
    }

    public static void addTicket(Integer amount) {

        if (amount != null) {
            getEnvelopeContents();
            return;
        }
        List<Currency> currencies = Currency.findAll();
        render(currencies);
    }

    public static void addCheck(Integer currency, Integer amount) {

        if (currency != null || amount != null) {
            getEnvelopeContents();
            return;
        }
        List<Currency> currencies = Currency.findAll();
        render(currencies);
    }

    public static void addOther() {
        getEnvelopeContents();
    }

    public static void cancelDeposit() {
    }

    public static void confirmDeposit(Integer envelopeCode) {
        if (envelopeCode != null) {
            printTicket();
        }
        render();
    }

    public static void printTicket() {
        render();
    }

    public static void summary() {
        render();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * public static void getEnvelopeContents(String depositId, LgEnvelope
     * envelope) { Deposit deposit =
     * Deposit.getAndValidateOpenDeposit(depositId);
     *
     * if (envelope != null && envelope.envelopeTypeLov != null) { // TODO: Use
     * validate. envelope.deposit = deposit;
     * Logger.debug("envelope.envelopeTypeLov : %d", envelope.envelopeTypeLov);
     * Logger.debug("envelope.number: %s", envelope.envelopeNumber); for
     * (LgEnvelopeContent l : envelope.envelopeContents) { if (l != null) {
     * Logger.debug("content amount: %d", l.amount); Logger.debug("content
     * content: %d", l.contentTypeLov); Logger.debug("content unit: %d",
     * l.unitLov); } } renderArgs.put("confirm", true); } List<EnvelopeType>
     * envelopeTypes = EnvelopeType.findAll(); renderArgs.put("envelopeTypes",
     * envelopeTypes); render(deposit, envelope); }
     */
    public static void acceptEnvelope(String depositId, LgEnvelope envelope) {
        Deposit deposit = Deposit.getAndValidateOpenDeposit(depositId);

        // Validate
        if (envelope == null || envelope.envelopeTypeLov == null) {
            Logger.error("INVALID ");
            index();
            return;
        }

        // TODO: Use validate.
        envelope.deposit = deposit;
        Logger.debug("envelope.envelopeTypeLov : %d", envelope.envelopeTypeLov);
        Logger.debug("envelope.number: %s", envelope.envelopeNumber);
        for (LgEnvelopeContent l : envelope.envelopeContents) {
            if (l != null) {
                Logger.debug("content amount: %d", l.amount);
                Logger.debug("content content: %d", l.contentTypeLov);
                Logger.debug("content unit: %d", l.unitLov);
            }
        }
        renderArgs.put("confirm", true);
        Manager.ControllerApi manager = CounterFactory.getGloryManager();
        if (!manager.envelopeDeposit()) {
            Logger.error("TODO ERROR HERE ???");
        }
        Logger.debug("------------ > Current Status %s", manager.getStatus().name());
        boolean done = false;
        while (!done) {
            Logger.debug("------------ > Current Status %s", manager.getStatus().name());
            switch (manager.getStatus()) {
                case IDLE:
                    done = true;
                    break;
                case ERROR:
                    Logger.error("TODO: ERROR DAVE HELP ME");
                    index();
                    return;
                default:
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                    }
                    break;
            }
        }
        // TODO: Put in a better place.
        for (LgEnvelopeContent c : envelope.envelopeContents) {
            c.envelope = envelope;
        }
        envelope.save();

        deposit.finishDate = new Date();
        deposit.save();

        flash.success("Deposit is done!");
        Application.index();
    }
}
