package controllers;

import java.util.List;
import models.ModelFacade;
import models.actions.EnvelopeDepositAction;
import models.actions.FilteringAction;
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
import play.mvc.Before;
import play.mvc.With;
import validation.FormCurrency;
import validation.FormDepositUserCodeReference;

@With(Secure.class)
public class EnvelopeDepositController extends Application {

    static EnvelopeDepositAction currentAction = null;
    
    @Before
    static void wizardFixPage() throws Throwable {
        if (currentAction==null) {
            if (!request.actionMethod.equalsIgnoreCase("start")) {
                Application.index();
            } else {
                if (!(currentAction instanceof EnvelopeDepositAction)) {
                    Application.index();
                }
                //currentAction = (EnvelopeDepositAction) userAction;
            }
        }
        
    /*        switch (modelFacade.getCurrentStep()) {
            case NONE:
                if (!request.actionMethod.equalsIgnoreCase("start")) {
                    Application.index();
                    break;
                }
                break;
            case FINISH:
            case RUNNING:
                if (modelFacade.getCurrentMode() != ModelFacade.CurrentMode.ENVELOPE_DEPOSIT) {
                    Logger.info("Redirect to index 1");
                    Application.index();
                }
                break;
            default:
                Logger.info("Redirect to index 2");
                Application.index();
                break;
        }*/
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

        final public Boolean showReference1 = isProperty("bill_deposit.show_reference1");
        final public Boolean showReference2 = isProperty("bill_deposit.show_reference2");
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
                //modelFacade.depositEnvelope(formData, (DepositUserCodeReference) formData.reference1.lov, formData.reference2, e);
                mainLoop();
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
        if (request.isAjax()) {
            Object[] o = new Object[2];
            //o[0] = modelFacade.getCurrentStep();
            o[1] = null;
            renderJSON(o);
            return;
        }
        //FormData formData = (FormData) modelFacade.getFormData();
        //Logger.debug("deposit data %s", formData);
        //renderArgs.put("formData", formData);
        render();
    }

    public static void cancel() {
        //modelFacade.cancelDeposit();
        mainLoop();
    }

    public static void accept() {
        //modelFacade.acceptDeposit();
        mainLoop();
    }

    public static void finish() {
//        String total = modelFacade.getDepositTotal();
//        FormData formData = (FormData) modelFacade.getFormData();
//        modelFacade.finishDeposit();
//        if (formData == null) {
//            Application.index();
//            return;
//        }
//        renderArgs.put("clientCode", getProperty("client_code"));
//        renderArgs.put("formData", formData);
//        renderArgs.put("depositTotal", total);
        render();
    }
}
