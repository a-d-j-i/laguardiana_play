package controllers;

import static controllers.ErrorController.status;
import controllers.serializers.BillQuantitySerializer;
import controllers.serializers.BillValueSerializer;
import java.util.List;
import machines.status.MachineCountStatus;
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
        status = ModelFacade.getCurrentStatus();
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

    static public class CountData {

        transient public LgUser currentUser = Secure.getCurrentUser();

        @CheckWith(FormCurrency.Validate.class)
        public FormCurrency currency = null;

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
        List<Currency> currencies = Currency.findEnabled();
        renderArgs.put("formData", formData);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        MachineCountStatus countStatus = (MachineCountStatus) status;
        if (request.isAjax()) {
            Object[] o = new Object[3];
            o[0] = countStatus.getStateName();
            o[1] = countStatus.getBillQuantities();
            o[2] = Messages.get("message." + countStatus.getStateName());
            renderJSON(o, new BillValueSerializer(), new BillQuantitySerializer());
        } else {
            renderArgs.put("clientCode", Configuration.getClientDescription());
            renderArgs.put("user", Secure.getCurrentUser());
            renderArgs.put("providerCode", Configuration.getProviderDescription());
            renderArgs.put("billData", countStatus.getBillQuantities());
//            renderArgs.put("currentDeposit", countStatus.getCurrentDeposit());
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
        ModelFacade.confirmAction();
        Application.index();
    }
}
