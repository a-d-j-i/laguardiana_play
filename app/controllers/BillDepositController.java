package controllers;

import devices.DeviceFactory;
import java.util.List;
import models.Deposit;
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
                BillDepositAction currentAction =
                        new BillDepositAction((DepositUserCodeReference) formData.reference1.lov,
                        formData.reference2, formData.currency.currency, formData, defaultTimeout);
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
        mainLoop();
    }

    public static void accept() {
        ModelFacade.accept();
        mainLoop();
    }

    public static void finish() {
        Deposit deposit = ModelFacade.getDeposit();
        FormData formData = (FormData) ModelFacade.getFormData();
        if (formData != null) {
            renderArgs.put("clientCode", getProperty("client_code"));
            renderArgs.put("formData", formData);
            if (deposit != null) {
                renderArgs.put("depositTotal", deposit.getTotal());
                renderArgs.put("depositId", deposit.depositId);
            }
            try {
                // Print the ticket.
                DeviceFactory.getPrinter().print("billDeposit", renderArgs);
            } catch (Throwable ex) {
                Logger.debug(ex.getMessage());
            }
            ModelFacade.finishAction();
        } else {
            Application.index();
            return;
        }
        render();
    }
}
