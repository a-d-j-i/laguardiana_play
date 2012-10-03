package controllers;

import java.util.List;
import models.ModelFacade;
import models.actions.CountingAction;
import models.lov.Currency;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import validation.FormCurrency;

public class FilterController extends Application {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        String neededAction = ModelFacade.getNeededAction();
        if (neededAction == null) {
            if (!request.actionMethod.equalsIgnoreCase("start") || ModelFacade.isLocked()) {
                Application.index();
            }
        } else {
            if (ModelFacade.getNeededController() == null
                    || !(request.controller.equalsIgnoreCase(ModelFacade.getNeededController()))) {
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
        Logger.debug("start data %s", formData);
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
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = ModelFacade.getState();
            o[1] = ModelFacade.getCurrentCounters();
            o[2] = Messages.get(ModelFacade.getActionMessage());
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("billData", ModelFacade.getCurrentCounters());
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
        ModelFacade.finishAction();
        Application.index();
    }
}
