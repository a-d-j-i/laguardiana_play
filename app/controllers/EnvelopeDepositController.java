package controllers;

import static controllers.BillDepositController.status;
import java.util.List;
import java.util.Set;
import machines.status.MachineEnvelopeDepositStatus;
import machines.status.MachineStatus;
import models.Configuration;
import models.EnvelopeDeposit;
import models.ModelFacade;
import models.db.LgEnvelope;
import models.db.LgEnvelopeContent;
import models.db.LgEnvelopeContent.EnvelopeContentType;
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
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;
import validation.FormCurrency;
import validation.FormDepositUserCodeEnvelopeReference;

@With(Secure.class)
public class EnvelopeDepositController extends Controller {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        MachineStatus status = ModelFacade.getCurrentStatus();
        if (request.isAjax()) {
            return;
        }

        String neededAction = status.getNeededAction();
        if (neededAction == null) {
            if (!request.actionMethod.equalsIgnoreCase("start") || (status.getCurrentUserId() == null || !status.getCurrentUserId().equals(Secure.getCurrentUserId()))) {
                Logger.debug("wizardFixPage Redirect Application.index, requested %s, currentUser %s, statusUser %s",
                        request.actionMethod, Secure.getCurrentUser(), status.getCurrentUserId());
                Application.index();
            }
        } else {
            if (!(request.action.equalsIgnoreCase(neededAction))) {
                Logger.debug("wizardFixPage REDIRECT Action %s TO NeededAction %s", request.action, neededAction);
                redirect(Router.getFullUrl(neededAction));
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
                if (formData.hasDocuments != null && formData.hasDocuments) {
                    e.addContent(new LgEnvelopeContent(EnvelopeContentType.DOCUMENTS, null, null));
                }
                if (formData.hasOthers != null && formData.hasOthers) {
                    e.addContent(new LgEnvelopeContent(EnvelopeContentType.OTHERS, null, null));
                }
                Integer reference1 = (formData.reference1.lov == null ? null : formData.reference1.lov.lovId);
                EnvelopeDeposit refDeposit = new EnvelopeDeposit(formData.currentUser, formData.reference2, reference1);
                refDeposit.addEnvelope(e);
                if (ModelFacade.startEnvelopeDepositAction(refDeposit)) {
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
        if (!Configuration.isIgnoreBag() && !ModelFacade.isBagFull(true)) {
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
        MachineEnvelopeDepositStatus envStatus = (MachineEnvelopeDepositStatus) status;
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = envStatus.getStateName();
            o[1] = null;
            o[2] = Messages.get(envStatus.getMessage());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", Configuration.getClientDescription());
            renderArgs.put("user", Secure.getCurrentUser());
            renderArgs.put("providerCode", Configuration.getProviderDescription());
            renderArgs.put("currentDeposit", envStatus.getCurrentDeposit());
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
        MachineEnvelopeDepositStatus envStatus = (MachineEnvelopeDepositStatus) status;
        EnvelopeDeposit deposit = envStatus.getCurrentDeposit();
        if (deposit != null) {
            Set<LgEnvelope> envelopes = deposit.envelopes;
            renderArgs.put("envelopes", envelopes);
            renderArgs.put("finishCause", deposit.finishCause);
            renderArgs.put("truncated", deposit.closeDate == null);
        }
        if (deposit.isFinished()) {
            ModelFacade.confirmAction();
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
