package controllers;

import java.util.List;
import models.ModelFacade;
import models.actions.CountingAction;
import models.actions.UserAction;
import models.lov.Currency;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Before;
import validation.FormCurrency;

public class CountController extends Application {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        F.Tuple<UserAction, Boolean> userActionTuple = ModelFacade.getCurrentUserAction();
        if (userActionTuple._1 == null) {
            if (!request.actionMethod.equalsIgnoreCase("start") || !userActionTuple._2) {
                Application.index();
            }
        } else {
            if (!(userActionTuple._1 instanceof CountingAction)) {
                Application.index();
            }
        }
    }

    static public class FormData {

        @CheckWith(FormCurrency.Validate.class)
        public FormCurrency currency = null;

        @Override
        public String toString() {
            return "FormData{" + "currency=" + currency + '}';
        }
    }

    public static void start(@Valid FormData formData)
            throws Throwable {
        Logger.debug("chooseCurrency data %s", formData);
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                Logger.error("Wizard : %s %s", error.getKey(), error.message());
            }
            params.flash(); // add http parameters to the flash scope
        } else {
            if (formData != null) {
                CountingAction currentAction = new CountingAction(formData.currency.currency, formData);
                ModelFacade.startAction(currentAction);
                mainLoop();
                return;
            }
        }
        if (formData == null) {
            formData = new FormData();
        }
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("formData", formData);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        CountingAction currentAction = getCurrentAction();
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = currentAction.getActionState()._1;
            o[1] = currentAction.getBillData();
            o[2] = Messages.get("counting." + currentAction.getActionState()._2.toLowerCase());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("billData", currentAction.getBillData());
            renderArgs.put("formData", currentAction.getFormData());
            render();
        }
    }

    public static void cancel() {
        CountingAction currentAction = getCurrentAction();
        currentAction.cancelDeposit();
        mainLoop();
    }

    public static void accept() {
        CountingAction currentAction = getCurrentAction();
        currentAction.acceptDeposit();
        mainLoop();
    }

    public static void finish() {
        CountingAction currentAction = getCurrentAction();
        currentAction.finishAction();
        Application.index();
    }
}
