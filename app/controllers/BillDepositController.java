package controllers;

import devices.glory.manager.Manager;
import java.util.List;
import models.ModelFacade;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import validation.FormCurrency;
import validation.FormDepositUserCodeReference;

public class BillDepositController extends Application {

    @Before
    static void wizardFixPage() throws Throwable {
        switch (modelFacade.getCurrentStep()) {
            case NONE:
                if (!request.actionMethod.equalsIgnoreCase("start")) {
                    Application.index();
                    break;
                }
                break;
            case FINISH:
            case RUNNING:
            case ERROR:
                if (modelFacade.getCurrentMode() != ModelFacade.CurrentMode.BILL_DEPOSIT) {
                    Application.index();
                }
                break;
            default:
                Application.index();
                break;
        }
    }

    static public class FormData {

        final public Boolean showReference1 = isProperty("bill_deposit.show_reference1");
        final public Boolean showReference2 = isProperty("bill_deposit.show_reference2");
        @CheckWith(FormDepositUserCodeReference.Validate.class)
        public FormDepositUserCodeReference reference1 = new FormDepositUserCodeReference();
        @Required(message = "validation.required.reference2")
        public String reference2 = null;
        @CheckWith(FormCurrency.Validate.class)
        public FormCurrency currency = new FormCurrency();

        @Override
        public String toString() {
            return "FormData{" + "reference1=" + reference1 + ", reference2=" + reference2 + '}';
        }
    }

    public static void start(@Valid FormData formData) throws Throwable {
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                Logger.error("Wizard : %s %s", error.getKey(), error.message());
            }
            params.flash(); // add http parameters to the flash scope
        } else {
            if (formData != null) {
                modelFacade.startBillDeposit(formData, (DepositUserCodeReference) formData.reference1.lov, formData.reference2, formData.currency.currency);
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
        if (request.isAjax()) {
            renderJSON(getCountingStatus());
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("billData", modelFacade.getBillData());
            renderArgs.put("formData", modelFacade.getFormData());
            render();
        }
    }

    public static void cancel() {
        modelFacade.cancelDeposit();
        mainLoop();
    }

    public static void accept() {
        modelFacade.acceptDeposit();
        mainLoop();
    }

    public static void finish() {
        String total = modelFacade.getDepositTotal();
        FormData formData = (FormData) modelFacade.getFormData();
        modelFacade.finishDeposit();
        if (formData == null) {
            Application.index();
            return;
        }
        renderArgs.put("clientCode", getProperty("client_code"));
        renderArgs.put("formData", formData);
        renderArgs.put("depositTotal", total);
        render();
    }
}
