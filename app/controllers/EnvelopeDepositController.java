package controllers;

import java.util.List;
import models.ModelFacade;
import models.actions.EnvelopeDepositAction;
import models.actions.UserAction;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgEnvelopeContent.EnvelopeContentType;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Before;
import play.mvc.With;
import validation.FormCurrency;
import validation.FormDepositUserCodeReference;

@With(Secure.class)
public class EnvelopeDepositController extends Application {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {

        F.Tuple<UserAction, Boolean> userActionTuple = ModelFacade.getCurrentUserAction();
        if (userActionTuple._1 == null) {
            if (!request.actionMethod.equalsIgnoreCase("start") || !userActionTuple._2) {
                Application.index();
            }
        } else {
            if (!(userActionTuple._1 instanceof EnvelopeDepositAction)) {
                Application.index();
            }
        }
    }

    static public class FormDataContent extends FormCurrency {

        static public class Validate extends FormCurrency.Validate {

            @Override
            public boolean isSatisfied(Object validatedObject, Object data) {
                return this.isSatisfied(validatedObject, (FormDataContent) data);
            }

            public boolean isSatisfied(Object validatedObject, FormDataContent data) {
                if (!super.isSatisfied(validatedObject, (FormCurrency) data)) {
                    return false;
                }
                if (data.amount == null) {
                    data.amount = 0;
                }
                return true;
            }
        }
        public Integer amount = null;

        @Override
        public String toString() {
            return "FormDataContent{" + "amount=" + amount + '}' + super.toString();
        }
    }

    static public class FormData {

        final public Boolean showReference1 = isProperty("envelope_deposit.show_reference1");
        final public Boolean showReference2 = isProperty("envelope_deposit.show_reference2");
        @CheckWith(FormDepositUserCodeReference.Validate.class)
        public FormDepositUserCodeReference reference1 = new FormDepositUserCodeReference();
        @Required(message = "validation.required.reference2")
        public String reference2 = null;
        @Required(message = "validation.required.envelopeCode")
        public String envelopeCode = null;
        public Boolean hasDocuments = null;
        public Boolean hasOthers = null;
        @CheckWith(FormDataContent.Validate.class)
        public FormDataContent cashData = new FormDataContent();
        @CheckWith(FormDataContent.Validate.class)
        public FormDataContent checkData = new FormDataContent();
        @CheckWith(FormDataContent.Validate.class)
        public FormDataContent ticketData = new FormDataContent();

        @Override
        public String toString() {
            return "FormData{" + "reference1=" + reference1 + ", reference2=" + reference2 + ", envelopeCode=" + envelopeCode + ", hasDocuments=" + hasDocuments + ", hasOther=" + hasOthers + ", cashData=" + cashData + ", checkData=" + checkData + ", ticketData=" + ticketData + '}';
        }
    }

    public static void start(@Valid FormData formData) {
        Logger.debug("wizard data %s", formData);
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                Logger.error("Wizard : %s %s", error.getKey(), error.message());
            }
            params.flash(); // add http parameters to the flash scope
        } else {
            if (formData != null) {
                LgEnvelope e = new LgEnvelope(0, formData.envelopeCode);
                if (formData.cashData.amount > 0) {
                    e.addContent(new LgEnvelopeContent(EnvelopeContentType.CASH, formData.cashData.amount, formData.cashData.currency.numericId));
                }
                if (formData.checkData.amount > 0) {
                    e.addContent(new LgEnvelopeContent(EnvelopeContentType.CHECKS, formData.checkData.amount, formData.checkData.currency.numericId));
                }
                if (formData.ticketData.amount > 0) {
                    e.addContent(new LgEnvelopeContent(EnvelopeContentType.TICKETS, formData.ticketData.amount, formData.ticketData.currency.numericId));
                }
                if (formData.hasDocuments != null) {
                    e.addContent(new LgEnvelopeContent(EnvelopeContentType.DOCUMENTS, null, null));
                }
                if (formData.hasOthers != null) {
                    e.addContent(new LgEnvelopeContent(EnvelopeContentType.OTHERS, null, null));
                }
                EnvelopeDepositAction currentAction = new EnvelopeDepositAction((DepositUserCodeReference) formData.reference1.lov, formData.reference2, e, formData);
                ModelFacade.startAction(currentAction);
                mainLoop();
                return;
            }
        }
        if (formData == null) {
            formData = new FormData();
        }
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("formData", formData);
        renderArgs.put("referenceCodes", referenceCodes);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        EnvelopeDepositAction currentAction = getCurrentAction();
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = currentAction.getActionState();
            o[1] = null;
            o[2] = Messages.get(currentAction.getActionMessage());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("formData", currentAction.getFormData());
            render();
        }
    }

    public static void cancel() {
        EnvelopeDepositAction currentAction = getCurrentAction();
        currentAction.cancelDeposit();
        mainLoop();
    }

    public static void accept() {
        EnvelopeDepositAction currentAction = getCurrentAction();
        currentAction.acceptDeposit();
        mainLoop();
    }

    public static void finish() {
        EnvelopeDepositAction currentAction = getCurrentAction();
        FormData formData = (FormData) currentAction.getFormData();
        currentAction.finishAction();
        if (formData == null) {
            Application.index();
            return;
        }
        renderArgs.put("clientCode", getProperty("client_code"));
        renderArgs.put("formData", formData);
        render();
    }
}
