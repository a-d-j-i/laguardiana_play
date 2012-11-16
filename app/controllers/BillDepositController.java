package controllers;

import java.util.List;
import models.BillDeposit;
import models.Configuration;
import models.ModelFacade;
import models.actions.BillDepositAction;
import models.db.LgSystemProperty;
import models.lov.Currency;
import models.lov.DepositUserCodeReference;
import play.Logger;
import play.data.validation.CheckWith;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Router;
import validation.FormCurrency;
import validation.FormDepositUserCodeReference;

public class BillDepositController extends CounterController {

    @Before
    // currentAction allways valid
    static void wizardFixPage() {
        if (request.isAjax()) {
            return;
        }
        String neededAction = ModelFacade.getNeededAction();
        String neededController = ModelFacade.getNeededController();
        if (neededAction == null || neededController == null) {
            if (!request.actionMethod.equalsIgnoreCase("start") || ModelFacade.isLocked()) {
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

    static public class FormData {

        final public Boolean showReference1 = Configuration.mustShowReference1();
        final public Boolean showReference2 = Configuration.mustShowReference2();
        @CheckWith(FormDepositUserCodeReference.Validate.class)
        public FormDepositUserCodeReference reference1 = new FormDepositUserCodeReference();
        //@Required(message = "validation.required.reference2")
        public String reference2 = null;
        @CheckWith(FormCurrency.Validate.class)
        public FormCurrency currency = new FormCurrency();

        @Override
        public String toString() {
            return "FormData{" + "reference1=" + reference1 + ", reference2=" + reference2 + ", currency=" + currency + '}';
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
                BillDepositAction currentAction = new BillDepositAction((DepositUserCodeReference) formData.reference1.lov,
                        formData.reference2, formData.currency.currency, formData);
                ModelFacade.startAction(currentAction);
                mainLoop();
                return;
            }
        }
        if (formData == null) {
            formData = new FormData();
            formData.currency.value = Configuration.getDefaultCurrency();
        }
        List<DepositUserCodeReference> referenceCodes = DepositUserCodeReference.findAll();
        List<Currency> currencies = Currency.findAll();
        renderArgs.put("formData", formData);
        renderArgs.put("referenceCodes", referenceCodes);
        renderArgs.put("currencies", currencies);
        render();
    }

    public static void mainLoop() {
        BillDeposit d = (BillDeposit) ModelFacade.getDeposit();
        Long totalSum = new Long(0);
        if (d != null) {
            totalSum = d.getTotal();
        }
        if (request.isAjax()) {
            Object[] o = new Object[4];
            o[0] = ModelFacade.getState();
            o[1] = ModelFacade.getCurrentCounters();
            o[2] = Messages.get(ModelFacade.getActionMessage());
            o[3] = totalSum;
            renderJSON(o);
        } else {
            renderArgs.put("clientCode", Configuration.getClientDescription());
            renderArgs.put("billData", ModelFacade.getCurrentCounters());
            renderArgs.put("formData", ModelFacade.getFormData());
            renderArgs.put("totalSum", totalSum);
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

    public static void cancelTimeout() {
        ModelFacade.suspendTimeout();
        renderJSON("");
    }

    public static void finish() {
        BillDeposit deposit = (BillDeposit) ModelFacade.getDeposit();
        FormData formData = (FormData) ModelFacade.getFormData();
        if (formData == null) {
            Application.index();
            return;
        }
        renderArgs.put("clientCode", Configuration.getClientDescription());
        renderArgs.put("formData", formData);
        if (deposit != null) {
            Long total = deposit.getTotal();
            renderArgs.put("canceled", (deposit.finishDate == null || total <= 0));
            renderArgs.put("depositTotal", total);
            renderArgs.put("depositId", deposit.depositId);
        }
        ModelFacade.finishAction();
        render();
    }
}
