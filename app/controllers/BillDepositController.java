package controllers;

import java.util.List;
import models.ModelFacade;
import models.actions.BillDepositAction;
import models.actions.UserAction;
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
import validation.FormCurrency;
import validation.FormDepositUserCodeReference;

public class BillDepositController extends Application {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {

        F.Tuple<UserAction, Boolean> userActionTuple = ModelFacade.getCurrentUserAction();
        if (userActionTuple._1 == null) {
            if (!request.actionMethod.equalsIgnoreCase("start") || !userActionTuple._2) {
                Application.index();
            }
        } else {
            if (!(userActionTuple._1 instanceof BillDepositAction)) {
                Application.index();
            }
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
                BillDepositAction currentAction = new BillDepositAction((DepositUserCodeReference) formData.reference1.lov, formData.reference2, formData.currency.currency, formData);
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
        BillDepositAction currentAction = getCurrentAction();
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = currentAction.getActionState();
            o[1] = currentAction.getBillData();
            o[2] = Messages.get(currentAction.getActionMessage());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("billData", currentAction.getBillData());
            renderArgs.put("formData", currentAction.getFormData());
            render();
        }
    }

    public static void cancel() {
        BillDepositAction currentAction = getCurrentAction();
        currentAction.cancelDeposit();
        mainLoop();
    }

    public static void accept() {
        BillDepositAction currentAction = getCurrentAction();
        currentAction.acceptDeposit();
        mainLoop();
    }

    public static void finish() {
        BillDepositAction currentAction = getCurrentAction();
        String total = currentAction.getTotal().toString();
        FormData formData = (FormData) currentAction.getFormData();
        currentAction.finishAction();
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
