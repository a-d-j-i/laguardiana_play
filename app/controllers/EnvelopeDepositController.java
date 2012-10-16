package controllers;

import java.util.List;
import models.EnvelopeDeposit;
import models.ModelFacade;
import models.actions.EnvelopeDepositAction;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgEnvelopeContent.EnvelopeContentType;
import models.db.LgSystemProperty;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Router;
import play.mvc.With;
import validation.FormCurrency;
import validation.FormDepositUserCodeReference;

@With(Secure.class)
public class EnvelopeDepositController extends CounterController {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        if (request.isAjax()) {
            return;
        }
        String neededAction = ModelFacade.getNeededAction();
        String neededController = ModelFacade.getNeededController();
        if (neededAction == null || neededController == null) {
            if (!request.actionMethod.equalsIgnoreCase("start") || ModelFacade.isLocked()) {
                Logger.debug("wizardFixPage Redirect Application.index");
                Application.index();
            }
        } else {
            if (!(request.controller.equalsIgnoreCase(neededController))) {
                Logger.debug("wizardFixPage REDIRECT TO neededController %s : neededAction %s", neededController, neededAction);
                redirect(Router.getFullUrl(neededController + "." + neededAction));
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
        //@Required(message = "validation.required.reference2")
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

                EnvelopeDepositAction currentAction =
                        new EnvelopeDepositAction((DepositUserCodeReference) formData.reference1.lov,
                        formData.reference2, e, formData);
                ModelFacade.startAction(currentAction);
                mainLoop();
            }
        }
        if (formData == null) {
            formData = new FormData();
            formData.cashData.value = getIntProperty(LgSystemProperty.Types.DEFAULT_CURRENCY);
            formData.checkData.value = getIntProperty(LgSystemProperty.Types.DEFAULT_CURRENCY);
            formData.ticketData.value = getIntProperty(LgSystemProperty.Types.DEFAULT_CURRENCY);

        }
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("formData", formData);
        renderArgs.put("referenceCodes", referenceCodes);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = ModelFacade.getState();
            o[1] = null;
            o[2] = Messages.get(ModelFacade.getActionMessage());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty(LgSystemProperty.Types.CLIENT_CODE));
            renderArgs.put("formData", ModelFacade.getFormData());
            render();
        }
    }

    public static void cancel() {
        ModelFacade.cancel();
        renderJSON("");
    }

    public static void accept() {
        ModelFacade.accept();
        renderJSON("");
    }

    public static void finish() {
        EnvelopeDeposit deposit = (EnvelopeDeposit) ModelFacade.getDeposit();
        FormData formData = (FormData) ModelFacade.getFormData();

        renderArgs.put("canceled", (deposit != null && deposit.finishDate == null));
        if (formData != null) {
            ModelFacade.finishAction();
        } else {
            Application.index();
            return;
        }
        render();
    }
}
