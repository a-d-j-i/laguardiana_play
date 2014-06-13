package controllers;

import java.util.List;
import java.util.Set;
import models.Configuration;
import models.EnvelopeDeposit;
import models.ModelFacade;
import models.db.LgEnvelope;
import models.db.LgUser;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Range;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Router;
import play.mvc.With;
import validation.FormCurrency;
import validation.FormDepositUserCodeEnvelopeReference;

@With(Secure.class)
public class EnvelopeDepositController extends ErrorController {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        if (request.isAjax()) {
            return;
        }
        status = ModelFacade.getStateStatus();
        String neededAction = status.getNeededAction();
        String neededController = status.getNeededController();
        if (neededAction == null || neededController == null) {
            EvenlopeDepositData data = (EvenlopeDepositData) status.getFormData();
            if (!request.actionMethod.equalsIgnoreCase("start") || data.currentUser != Secure.getCurrentUser()) {
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
                    data.amount = 0.0;
                }
                return true;
            }
        }
        public Double amount = null;

        @Override
        public String toString() {
            return "FormDataContent{" + "amount=" + amount + '}' + super.toString();
        }
    }

    static public class EvenlopeDepositData {

        transient public LgUser currentUser = Secure.getCurrentUser();

        final public Boolean showReference1 = Configuration.mustShowEnvelopeDepositReference1();
        final public Boolean showReference2 = Configuration.mustShowEnvelopeDepositReference2();
        @CheckWith(FormDepositUserCodeEnvelopeReference.Validate.class)
        public FormDepositUserCodeEnvelopeReference reference1 = new FormDepositUserCodeEnvelopeReference();
        //@Required(message = "validation.required.reference2")
        public String reference2 = null;
        @Required(message = "validation.required.envelopeCode")
        @Range(min = 0, max = 99999)
        public String envelopeCode = null;
        public Boolean hasDocuments = null;
        public Boolean hasOthers = null;
        @CheckWith(FormDataContent.Validate.class)
        public FormDataContent cashData = new FormDataContent();
        @CheckWith(FormDataContent.Validate.class)
        public FormDataContent checkData = new FormDataContent();
        @CheckWith(FormDataContent.Validate.class)
        public FormDataContent ticketData = new FormDataContent();

        public Currency getCurrency() {
            return null;
        }

        @Override
        public String toString() {
            return "FormData{" + "reference1=" + reference1 + ", reference2=" + reference2 + ", envelopeCode=" + envelopeCode + ", hasDocuments=" + hasDocuments + ", hasOther=" + hasOthers + ", cashData=" + cashData + ", checkData=" + checkData + ", ticketData=" + ticketData + '}';
        }
    }

    public static void start(@Valid EvenlopeDepositData formData) {
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                Logger.error("Wizard : %s %s", error.getKey(), error.message());
            }
            params.flash(); // add http parameters to the flash scope
        } else {
            if (formData != null) {
                if (ModelFacade.startEnvelopeDepositAction(formData)) {
                    mainLoop();
                    return;
                } else {
                    Application.index();
                }
                return;
            }
        }
        if (formData == null) {
            formData = new EvenlopeDepositData();
            formData.cashData.value = Configuration.getDefaultCurrency();
            formData.checkData.value = Configuration.getDefaultCurrency();
            formData.ticketData.value = Configuration.getDefaultCurrency();

        }
        if (!Configuration.isIgnoreBag() && !ModelFacade.isBagReady(true)) {
            Application.index();
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
            o[0] = status.getState();
            o[1] = null;
            o[2] = Messages.get(status.getActionMessage());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", Configuration.getClientDescription());
            renderArgs.put("user", Secure.getCurrentUser());
            renderArgs.put("providerCode", Configuration.getProviderDescription());
            renderArgs.put("formData", status.getFormData());
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
        EnvelopeDeposit deposit = (EnvelopeDeposit) status.getDeposit();
        EvenlopeDepositData formData = (EvenlopeDepositData) status.getFormData();
        if (deposit != null) {
            Set<LgEnvelope> envelopes = deposit.envelopes;
            renderArgs.put("envelopes", envelopes);
            renderArgs.put("finishCause", deposit.finishCause);
            renderArgs.put("truncated", deposit.closeDate == null);
        }
        if (formData != null) {
            ModelFacade.finishAction();
        } else {
            Application.index();
            return;
        }
        renderArgs.put("clientCode", Configuration.getClientDescription());
        renderArgs.put("user", Secure.getCurrentUser());
        renderArgs.put("providerCode", Configuration.getProviderDescription());
        render();
    }
}
