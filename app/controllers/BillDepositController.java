package controllers;

import java.util.List;
import models.ModelFacade;
import models.actions.BillDepositAction;
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

    static BillDepositAction currentAction = null;

    @Before
    // currentAction allways valid
    static void wizardFixPage() throws Throwable {
        if (currentAction == null) {
            if (!request.actionMethod.equalsIgnoreCase("start")) {
                Application.index();
            }
        } else {
            if (!(currentAction instanceof BillDepositAction)) {
                Application.index();
            }
            currentAction = (BillDepositAction) userAction;
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
                currentAction = new BillDepositAction((DepositUserCodeReference) formData.reference1.lov, formData.reference2, formData.currency.currency, formData);
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
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = currentAction.getFrontEndAction();
            o[1] = currentAction.getBillData();
            o[2] = Messages.get("bill_deposit." + currentAction.getMessageName());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("billData", currentAction.getBillData());
            renderArgs.put("formData", currentAction.getFormData());
            render();
        }
    }

    public static void cancel() {
        currentAction.cancelDeposit();
        mainLoop();
    }

    public static void accept() {
        currentAction.acceptDeposit();
        mainLoop();
    }

/*    public String getDepositTotal() {
        Logger.error("TOTAL : %d", currentAction.getTotal());
        return currentAction.getTotal().toString();
    }*/

    public static void finish() {
        String total = currentAction.getTotal().toString();
        FormData formData = (FormData) currentAction.getFormData();
        ModelFacade.finishAction();
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
