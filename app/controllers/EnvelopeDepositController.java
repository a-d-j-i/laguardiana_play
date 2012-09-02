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
import play.mvc.With;

@With(Secure.class)
public class EnvelopeDepositController extends Application {

    static public class FormDataContent {

        public String currencyId;
        public Integer amount;
    }

    // Is a big form loaded with a few screens so the data goes from action to action
    static public class FormData {

        public String reference1 = null;
        public String reference2 = null;
        public String envelopeCode = null;
        public Boolean hasDocuments = null;
        public Boolean hasOther = null;
        public FormDataContent cashData = null;
        public FormDataContent checkData = null;
        public FormDataContent ticketData = null;
        public Boolean doDeposit = false;

        @Override
        public String toString() {
            return "FormData{" + "reference1=" + reference1 + ", reference2=" + reference2 + ", envelopeCode=" + envelopeCode + ", hasDocuments=" + hasDocuments + ", hasOther=" + hasOther + ", cashData=" + cashData + ", checkData=" + checkData + ", ticketData=" + ticketData + ", doDeposit=" + doDeposit + '}';
        }
    }

    public static void index() {
        Application.index();
    }

    public static void wizard(FormData formData) {
        Logger.debug("inputReference data %s", formData);
        Boolean r1 = isProperty("bill_deposit.show_reference1");
        Boolean r2 = isProperty("bill_deposit.show_reference2");
        if (formData != null) {
            DepositUserCodeReference userCodeLov = validateReference1(r1, formData.reference1);
            String userCode = validateReference2(r2, formData.reference2);

            // TODO: Use form validation.
            if (userCodeLov != null && userCode != null) {
                getEnvelopeContents(formData);
            }
        }
        renderArgs.put("formData", formData);
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("showReference1", r1);
        renderArgs.put("showReference2", r2);
        render(referenceCodes, currencies);

    }

    public static void inputReference(FormData formData) {
        Logger.debug("inputReference data %s", formData);
        Boolean r1 = isProperty("bill_deposit.show_reference1");
        Boolean r2 = isProperty("bill_deposit.show_reference2");
        if (formData != null) {
            DepositUserCodeReference userCodeLov = validateReference1(r1, formData.reference1);
            String userCode = validateReference2(r2, formData.reference2);

            // TODO: Use form validation.
            if (userCodeLov != null && userCode != null) {
                getEnvelopeContents(formData);
            }
        }
        renderArgs.put("formData", formData);
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("showReference1", r1);
        renderArgs.put("showReference2", r2);
        render(referenceCodes, currencies);
    }

    public static void getEnvelopeContents(FormData formData) {
        Logger.debug("getEnvelopeContents data %s", formData);
        if (formData == null) {
            inputReference(formData);
            return;
        }
        Boolean r1 = isProperty("bill_deposit.show_reference1");
        Boolean r2 = isProperty("bill_deposit.show_reference2");
        if (formData != null) {
            DepositUserCodeReference userCodeLov = validateReference1(r1, formData.reference1);
            String userCode = validateReference2(r2, formData.reference2);
            // TODO: Use form validation.
            if (userCodeLov == null || userCode == null) {
                inputReference(formData);
            }
        }

        if (formData.doDeposit != null && formData.doDeposit) {
            confirmDeposit(formData);
            return;
        }
        renderArgs.put("formData", formData);
        List<EnvelopeType> envelopeTypes = EnvelopeType.findAll();
        renderArgs.put("envelopeTypes", envelopeTypes);
        render();
    }

    public static void addCash(FormData formData, String currency, Integer amount) {
        Logger.debug("addCash data %s", formData);

        if (formData != null) {
            if (currency != null && amount != null) {
                formData.cashData = new FormDataContent();
                formData.cashData.amount = amount;
                formData.cashData.currencyId = currency;
            }
            getEnvelopeContents(formData);
            return;
        }
        renderArgs.put("formData", formData);
        List<Currency> currencies = Currency.findAll();
        render(currencies);
    }

    public static void addDocument(FormData formData) {
        Logger.debug("addDocument data %s", formData);
        getEnvelopeContents(formData);
    }

    public static void addTicket(FormData formData) {
        Logger.debug("addTicket data %s", formData);

        if (formData != null) {
            getEnvelopeContents(formData);
            return;
        }
        List<Currency> currencies = Currency.findAll();
        render(currencies);
    }

    public static void addCheck(FormData formData) {
        Logger.debug("addCheck data %s", formData);

        if (formData != null) {
            getEnvelopeContents(formData);
            return;
        }
        List<Currency> currencies = Currency.findAll();
        render(currencies);
    }

    public static void addOther(FormData formData) {
        Logger.debug("addOther data %s", formData);
        getEnvelopeContents(formData);
    }

    public static void cancelDeposit(FormData formData) {
        Logger.debug("cancelDeposit data %s", formData);
    }

    public static void confirmDeposit(FormData formData) {
        Logger.debug("confirmDeposit data %s", formData);
        if (formData != null) {
            printTicket(formData);
        }
        render();
    }

    public static void printTicket(FormData formData) {
        Logger.debug("printTicket data %s", formData);
        render(formData);
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
