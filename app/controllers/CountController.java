package controllers;

import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import java.util.List;
import models.Configuration;
import models.ModelFacade;
import models.db.LgUser;
import models.lov.Currency;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Router;
import validation.FormCurrency;

public class CountController extends ErrorController {

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
            CountData data = (CountData) status.getFormData();
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

    static public class CountData {

        transient public LgUser currentUser = Secure.getCurrentUser();

        @CheckWith(FormCurrency.Validate.class)
        public FormCurrency currency = null;

        public Currency getCurrency() {
            return currency.currency;
        }

        @Override
        public String toString() {
            return "FormData{" + "currency=" + currency + '}';
        }
    }

    public static void start(@Valid CountData formData)
            throws Throwable {
        Logger.debug("start data %s", formData);
        if (Validation.hasErrors()) {
            for (play.data.validation.Error error : Validation.errors()) {
                Logger.error("Wizard : %s %s", error.getKey(), error.message());
            }
            params.flash(); // add http parameters to the flash scope
        } else {
            if (formData != null) {
                if (ModelFacade.startCountingAction(formData)) {
                    mainLoop();
                } else {
                    Application.index();
                }
                return;
            }
        }
        if (formData == null) {
            formData = new CountData();
        }
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("formData", formData);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        CountData data = (CountData) status.getFormData();
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = status.getState();
            o[1] = ModelFacade.getBillQuantities(data.currency.value);
            o[2] = Messages.get(status.getActionMessage());
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        } else {
            renderArgs.put("clientCode", Configuration.getClientDescription());
            renderArgs.put("user", Secure.getCurrentUser());
            renderArgs.put("providerCode", Configuration.getProviderDescription());
            renderArgs.put("billData", ModelFacade.getBillQuantities(data.currency.value));
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
        ModelFacade.finishAction();
        Application.index();
    }
}
